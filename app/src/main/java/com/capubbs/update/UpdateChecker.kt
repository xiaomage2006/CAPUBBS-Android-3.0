package com.capubbs.update

import android.util.Log
import com.capubbs.BuildConfig
import com.capubbs.lib.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 检查新版本核心逻辑。
 *
 * 流程：
 * 1. 优先请求 BBS API（?ask=main），若返回 updatetime 字段则用它判断更新。
 * 2. BBS API 无更新信息时，自动兜底请求 GitHub Release API。
 * 3. 用 [VersionUtils.compareVersions] 比较远程版本号和本地 [BuildConfig.VERSION_NAME]。
 * 4. 远程版本更高 → 返回 [UpdateInfo]；否则返回 null。
 *
 * Java 调用入口：[checkForUpdate]（接受 [Callback] 回调，自动切换到主线程）。
 * Kotlin 调用入口：[checkForUpdateSuspend]（suspend 函数）。
 */
object UpdateChecker {

    private const val TAG = "UpdateChecker"

    /** 检查结果 */
    data class UpdateInfo(
        val version: String,       // 远程版本号，如 "v3.0.1"
        val downloadUrl: String,   // APK 下载链接或 Release 页面
        val releaseNotes: String   // 更新说明
    )

    /** Java 友好的回调接口（SAM） */
    fun interface Callback {
        fun onResult(info: UpdateInfo?)
    }

    // ──────────────────────────────────────────
    //  Java 入口
    // ──────────────────────────────────────────

    /**
     * 异步检查更新，结果通过 [callback] 在主线程返回。
     * 供 Java 代码调用：`UpdateChecker.checkForUpdate(info -> { ... })`
     */
    @JvmStatic
    fun checkForUpdate(callback: Callback) {
        CoroutineScope(Dispatchers.IO).launch {
            val info = try {
                checkForUpdateSuspend()
            } catch (e: Exception) {
                Log.e(TAG, "checkForUpdate failed", e)
                null
            }
            withContext(Dispatchers.Main) {
                callback.onResult(info)
            }
        }
    }

    // ──────────────────────────────────────────
    //  Kotlin suspend 入口
    // ──────────────────────────────────────────

    suspend fun checkForUpdateSuspend(): UpdateInfo? {
        val localVersion = BuildConfig.VERSION_NAME

        // 1. 优先 BBS API
        val bbsResult = tryBbsApi()
        if (bbsResult != null) {
            return if (VersionUtils.compareVersions(localVersion, bbsResult.version) < 0) {
                bbsResult
            } else {
                null
            }
        }

        // 2. 兜底 GitHub Release API
        val githubResult = tryGithubApi() ?: return null
        return if (VersionUtils.compareVersions(localVersion, githubResult.version) < 0) {
            githubResult
        } else {
            null
        }
    }

    // ──────────────────────────────────────────
    //  内部实现
    // ──────────────────────────────────────────

    /** 请求 BBS API，若返回 updatetime 则构建 [UpdateInfo] */
    private suspend fun tryBbsApi(): UpdateInfo? {
        return try {
            val list = ApiClient.bbsApi.getMainInfo("main")
            if (list.isNullOrEmpty()) return null

            val first = list[0]
            if (!first.hasUpdateInfo()) return null

            UpdateInfo(
                version = first.updateTime.orEmpty(),
                downloadUrl = first.updateUrl.orEmpty(),
                releaseNotes = first.updateText.orEmpty()
            )
        } catch (e: Exception) {
            Log.w(TAG, "BBS API 请求失败，将兜底 GitHub", e)
            null
        }
    }

    /** 请求 GitHub Release API，解析 tag_name / assets / body */
    private suspend fun tryGithubApi(): UpdateInfo? {
        return try {
            // 从 Constants.github_repo 解析 owner/repo
            val parts = Constants.github_repo.split("/")
            if (parts.size < 2) return null

            val release = ApiClient.githubApi.getLatestRelease(parts[0], parts[1])

            // APK 下载链接：优先 assets[0].browser_download_url，无附件则用 Release 页面
            val apkUrl = release.assets
                ?.firstOrNull()
                ?.browserDownloadUrl
                ?.takeIf { it.isNotBlank() }
                ?: release.htmlUrl.orEmpty()

            UpdateInfo(
                version = release.tagName,
                downloadUrl = apkUrl,
                releaseNotes = release.body?.takeIf { it.isNotBlank() }
                    ?: "发现新版本，点击下载更新"
            )
        } catch (e: Exception) {
            Log.w(TAG, "GitHub Release API 请求失败", e)
            null
        }
    }
}

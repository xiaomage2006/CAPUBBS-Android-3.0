package com.capubbs.update

import com.google.gson.annotations.SerializedName

/**
 * GitHub Release 附件（APK 等）。
 *
 * @param name              附件文件名，如 "CAPUBBS-3.0.1.apk"
 * @param browserDownloadUrl 浏览器下载直链
 */
data class GithubAsset(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("browser_download_url")
    val browserDownloadUrl: String = ""
)

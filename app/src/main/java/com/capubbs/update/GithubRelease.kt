package com.capubbs.update

import com.google.gson.annotations.SerializedName

/**
 * GitHub Release 最新版本信息。
 *
 * @param tagName  版本标签，如 "v3.0.0"
 * @param body     更新说明（Markdown 原文）
 * @param htmlUrl  Release 页面 URL（无 APK 附件时作为下载兜底）
 * @param assets   附件列表，assets[0] 的 browserDownloadUrl 即 APK 下载直链
 */
data class GithubRelease(
    @SerializedName("tag_name")
    val tagName: String = "",

    @SerializedName("body")
    val body: String? = null,

    @SerializedName("html_url")
    val htmlUrl: String? = null,

    @SerializedName("assets")
    val assets: List<GithubAsset>? = null
)

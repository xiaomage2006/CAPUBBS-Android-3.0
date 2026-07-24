package com.capubbs.update

import com.google.gson.annotations.SerializedName

/**
 * BBS API `?ask=main` 响应数组中单个元素。
 *
 * 当服务端配置了更新信息时，first 元素包含以下字段：
 * - [updateTime]  新版本号
 * - [updateUrl]   下载链接
 * - [updateText]  更新说明
 *
 * 若服务端未配置更新，这些字段为 null/空，需兜底走 GitHub Release。
 */
data class BbsMainResponse(
    @SerializedName("code")
    val code: Int? = null,

    @SerializedName("updatetime")
    val updateTime: String? = null,

    @SerializedName("updateurl")
    val updateUrl: String? = null,

    @SerializedName("updatetext")
    val updateText: String? = null
) {
    /** 是否包含有效的更新信息 */
    fun hasUpdateInfo(): Boolean =
        !updateTime.isNullOrBlank()
}

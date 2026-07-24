package com.capubbs.update

/**
 * 语义版本号比较工具。
 *
 * 支持 "3.0"、"3.0.1"、"v3.1"、"V3.1.0-beta" 等格式：
 * - 自动去除 v/V 前缀
 * - 截取 "-" 或 "+" 之前的主版本部分（忽略 pre-release / build metadata）
 * - 逐段数字比较，缺失段视为 0
 *
 * @return 负数 = v1 < v2，0 = 相等，正数 = v1 > v2
 */
object VersionUtils {

    @JvmStatic
    fun compareVersions(v1: String?, v2: String?): Int {
        val s1 = normalize(v1)
        val s2 = normalize(v2)

        val p1 = s1.split(".")
        val p2 = s2.split(".")
        val len = maxOf(p1.size, p2.size)

        for (i in 0 until len) {
            val n1 = p1.getOrElse(i) { "0" }.trim().toIntOrNull() ?: 0
            val n2 = p2.getOrElse(i) { "0" }.trim().toIntOrNull() ?: 0
            if (n1 != n2) return n1 - n2
        }
        return 0
    }

    /** 去除 v/V 前缀，截取 "-" 或 "+" 之前的部分 */
    private fun normalize(v: String?): String {
        if (v.isNullOrBlank()) return "0"
        return v.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")
            .substringBefore("+")
            .ifBlank { "0" }
    }
}

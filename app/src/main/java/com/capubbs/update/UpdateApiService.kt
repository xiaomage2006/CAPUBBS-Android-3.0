package com.capubbs.update

import com.capubbs.lib.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ──────────────────────────────────────────────
//  Retrofit 接口定义
// ──────────────────────────────────────────────

/**
 * BBS 站点 API —— 查询是否有更新信息。
 */
interface BbsApiService {

    @GET("client.php")
    suspend fun getMainInfo(@Query("ask") ask: String = "main"): List<BbsMainResponse>
}

/**
 * GitHub Releases API —— 查询最新 Release。
 */
interface GitHubApiService {

    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GithubRelease
}

// ──────────────────────────────────────────────
//  Retrofit 单例工厂
// ──────────────────────────────────────────────

object ApiClient {

    private fun buildOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /** BBS API — base_url + "/api/" */
    val bbsApi: BbsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.base_url + "/api/")
            .client(buildOkHttp())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BbsApiService::class.java)
    }

    /** GitHub API */
    val githubApi: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(buildOkHttp())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubApiService::class.java)
    }
}

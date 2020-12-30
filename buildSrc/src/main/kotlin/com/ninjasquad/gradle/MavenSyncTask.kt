package com.ninjasquad.gradle

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Credentials
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.time.Duration

open class MavenSyncTask : DefaultTask() {
    @Input
    lateinit var sonatypeUsername: String

    @Input
    lateinit var sonatypePassword: String

    @Input
    lateinit var bintrayUsername: String

    @Input
    lateinit var bintrayPassword: String

    @Input
    lateinit var bintrayRepoName: String

    @Input
    lateinit var bintrayPackageName: String

    @Input
    var version = project.version.toString()

    @TaskAction
    fun sync() {
        println("synchronizing with Maven central...")
        val client = OkHttpClient.Builder().readTimeout(Duration.ofMinutes(5L)).build()
        val jsonBody =
            // language=json
            """
            {
              "username": "$sonatypeUsername",
              "password": "$sonatypePassword",
              "close": "1"
            }
            """.trimIndent()
        val request: Request = Request.Builder()
            .header("Authorization", Credentials.basic(bintrayUsername, bintrayPassword))
            .url("https://api.bintray.com/maven_central_sync/$bintrayUsername/$bintrayRepoName/$bintrayPackageName/versions/$version")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw GradleException("Maven sync failed with status " + response.code + " and body " + response.body?.string())
                }
                println(response.body?.string())
            }
        } catch (e: IOException) {
            throw GradleException("Maven sync failed", e)
        }
    }
}

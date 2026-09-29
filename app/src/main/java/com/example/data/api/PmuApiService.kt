package com.example.data.api

import com.example.data.model.ParticipantsWrapper
import com.example.data.model.ProgrammeWrapper
import com.example.data.model.PronosticWrapper
import com.example.data.model.PronosticsDetaillesWrapper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface PmuApiService {

    @GET("{date}")
    suspend fun getProgramme(
        @Path("date") date: String
    ): Response<ProgrammeWrapper>

    @GET("{date}/R{reunion}/C{course}/participants")
    suspend fun getParticipants(
        @Path("date") date: String,
        @Path("reunion") reunion: Int,
        @Path("course") course: Int
    ): Response<ParticipantsWrapper>

    @GET("{date}/R{reunion}/C{course}/pronostics")
    suspend fun getPronostics(
        @Path("date") date: String,
        @Path("reunion") reunion: Int,
        @Path("course") course: Int
    ): Response<PronosticWrapper>

    @GET("{date}/R{reunion}/C{course}/pronostics-detailles")
    suspend fun getPronosticsDetailles(
        @Path("date") date: String,
        @Path("reunion") reunion: Int,
        @Path("course") course: Int
    ): Response<PronosticsDetaillesWrapper>

    companion object {
        private const val BASE_URL = "https://offline.turfinfo.api.pmu.fr/rest/client/7/programme/"

        fun create(): PmuApiService {
            val headerInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 PMUTurf/1.0")
                    .build()
                chain.proceed(request)
            }

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(PmuApiService::class.java)
        }
    }
}

package com.blanjaque.service

import com.cahstudio.rumahtentor.service.IFCMService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object FCMClientHelper {
    fun getRetrofitBasic(): IFCMService {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: IFCMService = retrofit.create(
            IFCMService::class.java)
        return  service
    }
}
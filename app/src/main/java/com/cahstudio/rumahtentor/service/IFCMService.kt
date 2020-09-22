package com.cahstudio.rumahtentor.service

import com.cahstudio.rumahtentor.utils.Utils
import com.cahstudio.rumahtentor.model.request.Request
import com.cahstudio.rumahtentor.model.response.Response
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=${Utils.KEY_CLOUD_MESSAGE}"
    )
    @POST("fcm/send")
    fun pushNotification(@Body request: Request?): Observable<Response>
}
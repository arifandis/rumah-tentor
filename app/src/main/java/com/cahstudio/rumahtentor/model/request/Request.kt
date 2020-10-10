package com.cahstudio.rumahtentor.model.request

import androidx.annotation.Keep
import com.cahstudio.rumahtentor.model.Message

@Keep
data class Request(
    val to: String?,
    val data: Message
)
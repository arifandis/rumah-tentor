package com.cahstudio.rumahtentor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Order(
    val no: Int,
    val key: String?,
    val student_uid: String?,
    val tentor_uid: String?,
    val level: String?,
    val course: String?,
    val day: String?,
    val time: String?,
    val status: String?,
    val payment_type: String?,
    val payment: String?,
    val schedule: List<Schedule>? = null
): Parcelable{
    constructor() : this(0,"","","","","",""
        ,"","","","",null)
}
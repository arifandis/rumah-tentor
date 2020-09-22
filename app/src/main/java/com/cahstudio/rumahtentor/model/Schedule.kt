package com.cahstudio.rumahtentor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Schedule(
    val id: Long,
    val date: String?,
    val status: String?
): Parcelable{
    constructor() : this(0,"","")
}
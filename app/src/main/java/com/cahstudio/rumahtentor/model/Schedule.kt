package com.cahstudio.rumahtentor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Schedule(
    val id: Long,
    var date: String?,
    var status: String?,
    var tentor: Boolean?,
    var student: Boolean?
): Parcelable{
    constructor() : this(0,"","", null, null)
}
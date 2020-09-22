package com.cahstudio.rumahtentor.model

data class Message(
    val id: Long,
    val from_name: String?,
    val from_uid: String?,
    val to_name: String?,
    val to_uid: String?,
    val image: String?,
    val message: String?
){
    constructor() : this(0,"","","","","","")
}
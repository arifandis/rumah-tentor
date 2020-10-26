package com.cahstudio.rumahtentor.model

data class UserMessage(
    val uid: String?,
    val name: String?
){
    constructor() : this("","")
}
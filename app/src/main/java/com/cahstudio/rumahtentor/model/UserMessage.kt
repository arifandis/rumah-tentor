package com.cahstudio.rumahtentor.model

data class UserMessage(
    val id: String?,
    val name: String?
){
    constructor() : this("","")
}
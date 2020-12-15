package com.cahstudio.rumahtentor.model

data class User(
    val uid: String?,
    val name: String?
){
    constructor() : this("","")
}
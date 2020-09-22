package com.cahstudio.rumahtentor.model

data class Course (
    val id: String?,
    val level: String?,
    val name: String?,
    val no: Int,
    val price: Int
){
    constructor() : this("","","",0,0)
}
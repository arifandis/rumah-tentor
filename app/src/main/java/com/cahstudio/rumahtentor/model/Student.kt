package com.cahstudio.rumahtentor.model

data class Student(
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val status: String? = "not studying",
    val current_order: String? = ""
){
    constructor() : this("","","","not studying", "")
}
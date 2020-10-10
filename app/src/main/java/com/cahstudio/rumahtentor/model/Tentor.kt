package com.cahstudio.rumahtentor.model

data class Tentor(
    val course: String?,
    val email: String?,
    val level: String?,
    val name: String?,
    val uid: String?,
    val bank: String?,
    val bank_no_rek: String?,
    val bank_account_name: String?,
    val current_order: String?,
    val status: String?
){
    constructor() : this("","","","","","","","","","")
}
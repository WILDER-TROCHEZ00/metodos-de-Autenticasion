package com.example.tarea31_metodos_autenticacion

data class LoginRequest(
    val usuario: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val token: String?,
    val message: String?
)

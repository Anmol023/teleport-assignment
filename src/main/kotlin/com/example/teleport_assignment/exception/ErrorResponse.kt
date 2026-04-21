package com.example.teleport_assignment.exception

data class ErrorResponse(
    val errorCode: String,
    val errorMessage: String,
    val details: Map<String, String>? = null
)
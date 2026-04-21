package com.example.teleport_assignment.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            errorCode = "REQ_VALIDATION_ERROR",
            errorMessage = ex.message ?: "Some error has occurred",
            details = mapOf(
                "path" to request.requestURI
            )
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val rootMessage = ex.mostSpecificCause.message ?: ex.message ?: "Some error has occurred"
        val body = ErrorResponse(
            errorCode = "MALFORMED_REQUEST",
            errorMessage = "Request is not readable",
            details = mapOf(
                "path" to request.requestURI,
                "cause" to rootMessage
            )
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorMessages = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        val body = ErrorResponse(
            errorCode = "REQ_VALIDATION_ERROR",
            errorMessage = errorMessages,
            details = mapOf(
                "path" to request.requestURI
            )
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            errorCode = "INTERNAL_SERVER_ERROR",
            errorMessage = ex.message ?: "An unexpected error has occurred",
            details = mapOf(
                "path" to request.requestURI
            )
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}
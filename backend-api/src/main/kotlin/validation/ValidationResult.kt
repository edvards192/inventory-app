package com.example.validation

data class ValidationResult(
    val isValid: Boolean,
    val error: String? = null
)
package com.example.validation

import com.example.models.requests.CreateReorderRequest

object ReorderValidator {

    fun validateCreate(request: CreateReorderRequest): ValidationResult {
        if (request.quantity <= 0) {
            return ValidationResult(false, "Quantity must be greater than 0.")
        }
        return ValidationResult(true)
    }
}

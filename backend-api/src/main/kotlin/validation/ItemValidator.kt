package com.example.validation

import com.example.models.requests.CreateItemRequest

object ItemValidator {

    fun validateCreate(request: CreateItemRequest): ValidationResult {
        return validate(request)
    }
    fun validateUpdate(request: CreateItemRequest): ValidationResult {
        return validate(request)
    }

    private fun validate(request: CreateItemRequest): ValidationResult {
        if (request.title.isBlank()) {
            return ValidationResult(false, "Title cannot be empty.")
        }

        if (request.title.length > 255) {
            return ValidationResult(false, "Title cannot be longer than 255 characters.")
        }

        if (request.ean.isBlank()) {
            return ValidationResult(false, "EAN cannot be empty.")
        }

        if (request.ean.length != 13) {
            return ValidationResult(false, "EAN must be exactly 13 characters.")
        }

        if (!request.ean.all { it.isDigit() }) {
            return ValidationResult(false, "EAN must contain only digits.")
        }

        if (request.price != null && request.price < 0) {
            return ValidationResult(false, "Price cannot be negative.")
        }

        if (request.reorderPoint < 0) {
            return ValidationResult(false, "Reorder point cannot be negative.")
        }
        return ValidationResult(true)
    }
}

package com.example.validation

import com.example.models.requests.CreateItemRequest

object ItemValidator {

    fun validateCreate(request: CreateItemRequest): ValidationResult {
        if (request.title.isBlank()) {
            return ValidationResult(false, "Title cannot be empty")
        }

        if (request.ean.isBlank()) {
            return ValidationResult(false, "EAN cannot be empty")
        }

        if (request.ean.length != 13) {
            return ValidationResult(false, "EAN must be exactly 13 characters")
        }
        return ValidationResult(true)
    }
    fun validateUpdate(request: CreateItemRequest): ValidationResult {
        if (request.title.isBlank()) {
            return ValidationResult(false, "Title cannot be empty")
        }

        if (request.ean.isBlank()) {
            return ValidationResult(false, "EAN cannot be empty")
        }

        if (request.ean.length != 13) {
            return ValidationResult(false, "EAN must be exactly 13 characters")
        }
        return ValidationResult(true)
    }
}
package com.example.validation

object UserValidator {
    fun validate(name: String, surname: String, email: String, password: String? = null): String? = when {
        name.isBlank() || name.length > 100 -> "Name must contain between 1 and 100 characters"
        surname.isBlank() || surname.length > 100 -> "Surname must contain between 1 and 100 characters"
        email.length > 255 || !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email) -> "Enter a valid email address"
        password != null && (password.isBlank() || password.toByteArray(Charsets.UTF_8).size > 72) ->
            "Password is required and must not exceed 72 UTF-8 bytes"
        else -> null
    }
}

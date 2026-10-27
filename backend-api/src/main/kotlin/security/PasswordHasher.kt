package com.example.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordHasher {

    fun verify(password: String, hash: String): Boolean {
        return BCrypt.verifyer()
            .verify(password.toCharArray(), hash)
            .verified
    }
}
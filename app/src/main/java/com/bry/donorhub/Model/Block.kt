package com.bry.donorhub.Model

import java.lang.Long
import java.security.MessageDigest
import java.util.*

class Block(var data: String, var previousHash: String) {
    var timestamp = Calendar.getInstance().timeInMillis

    fun calculateHash(): String{
        var string = previousHash+timestamp.toString()+data
        return hashString(string, "SHA-256")
    }

    private fun hashString(input: String, algorithm: String): String {
        return MessageDigest
                .getInstance(algorithm)
                .digest(input.toByteArray())
                .fold("", { str, it -> str + "%02x".format(it) })
    }


}
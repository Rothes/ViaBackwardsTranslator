package io.github.rothes.viabackwardstranslator.utils

import java.io.File


object FileUtils {

    fun readFile(file: File): String {
        val builder = StringBuilder()
        file.reader(Charsets.UTF_8).use {
            for (line in it.readLines()) {
                builder.append(line).append("\n")
            }
        }
        return builder.toString()
    }

    fun isExist(file: File): Boolean {
        if (!file.exists()) {
            println("ERROR: File doesn't exist: ${file.absolutePath}")
            return false
        }
        return true
    }

}
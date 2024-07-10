package com.example.commands

import java.io.File
import java.util.*

fun cachePath(filePath: File?): File? {
    val uniqueStoragePath = filePath?.let { File(it, UUID.randomUUID().toString()) }
    return uniqueStoragePath
}
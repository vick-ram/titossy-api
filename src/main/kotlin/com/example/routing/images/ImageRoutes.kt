package com.example.routing.images

import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

fun Route.imageRoutes(routePath: String, directoryPath: String, vararg extensions: String) {
    staticFiles(routePath, File(directoryPath)) {
        extensions(*extensions)
    }
}
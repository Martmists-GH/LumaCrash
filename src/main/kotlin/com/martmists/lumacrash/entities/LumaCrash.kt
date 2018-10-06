package com.martmists.lumacrash.entities

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import io.ktor.request.isMultipart
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.*
import java.io.File
import java.util.*
import kotlin.math.pow

class LumaCrash(port: Int) {
    val random = Random()

    val app = embeddedServer(Netty, port=port) {
        install(DefaultHeaders)
        install(CallLogging)

        routing {
            get("/{file}.dump"){
                val fn = call.parameters["file"]!!

                try {
                    call.respond(File("dumps/$fn.dump").readBytes())
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.NotFound, "$fn.dump was not found on the server!")
                }
            }

            post("/upload"){
                if (!call.request.isMultipart())
                    return@post call.respond(HttpStatusCode.BadRequest, "Not a multipart body!")

                val mp = call.receiveMultipart()

                val part = mp.readPart()!!
                when (part){
                    is PartData.FileItem -> {
                        val bytes = part.streamProvider().readBytes()

                        val fn = randomFileName()

                        File(fn).writeBytes(bytes)

                        call.respond(fn)
                    }
                    else -> call.respond(HttpStatusCode.BadRequest, "No file attached!")
                }
            }
        }
    }

    fun randomFileName(): String {
        val hex = random.nextInt(16F.pow(8).toInt()).toString(16)
        return "dumps/${hex.padStart(8, '0')}.dump"
    }

    fun start(){
        app.start(true)
    }

    companion object {
        fun fromConfigFile(path: String): LumaCrash {
            return LumaCrash(8080)
        }
    }
}
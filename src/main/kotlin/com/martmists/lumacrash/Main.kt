package com.martmists.lumacrash

import com.martmists.lumacrash.entities.LumaCrash

fun main(args: Array<String>){
    val srv = LumaCrash.fromConfigFile("config.json")
    srv.start()
}
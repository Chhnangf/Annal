package com.example.navhost

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
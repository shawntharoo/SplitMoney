package com.cherrye.splitmoney

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
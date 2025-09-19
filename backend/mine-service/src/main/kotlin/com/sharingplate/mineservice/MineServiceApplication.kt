package com.sharingplate.mineservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MineServiceApplication

fun main(args: Array<String>) {
    runApplication<MineServiceApplication>(*args)
}

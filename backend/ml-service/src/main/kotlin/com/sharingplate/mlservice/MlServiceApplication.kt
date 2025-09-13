package com.sharingplate.mlservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MlServiceApplication

fun main(args: Array<String>) {
    runApplication<MlServiceApplication>(*args)
}

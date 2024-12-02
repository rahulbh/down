// backend/src/main/kotlin/com/downdetector/DowndetectorApplication.kt
package com.downdetector

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class DowndetectorApplication

fun main(args: Array<String>) {
    runApplication<DowndetectorApplication>(*args)
}

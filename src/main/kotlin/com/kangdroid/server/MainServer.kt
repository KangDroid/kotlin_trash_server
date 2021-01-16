package com.kangdroid.server

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class MainServer

fun main(args: Array<String>) {
    SpringApplication.run(MainServer::class.java, *args)
}
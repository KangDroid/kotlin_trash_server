package com.kangdroid.server.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct

@ConfigurationProperties("kdr")
@Component
class Settings {
    lateinit var trashPath: String
    var lowMemoryOption: Boolean? = null
    val serverVersion: String = "V1.0.0"

    @PostConstruct
    fun initPostConstruct() {
        if (System.getProperty("kdr.isTesting") == "test") {
            trashPath = File(System.getProperty("java.io.tmpdir"), "kdr_testing").absolutePath
            return
        }
    }
}
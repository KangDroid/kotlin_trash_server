package com.kangdroid.server.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("kdr")
@Component
class Settings {
    lateinit var root: String
}
package com.kangdroid.server.controller

import com.kangdroid.server.dto.SettingsResponseDto
import com.kangdroid.server.settings.Settings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SettingsAPIController {
    @Autowired
    private lateinit var settings: Settings

    @GetMapping("/api/settings/get")
    fun getSettingsValue(): SettingsResponseDto {
        return SettingsResponseDto(settings.trashPath, settings.serverVersion)
    }
}
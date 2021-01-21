package com.kangdroid.server.controller

import com.kangdroid.server.dto.SettingsRequestDto
import com.kangdroid.server.dto.SettingsResponseDto
import com.kangdroid.server.settings.Settings
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SettingsAPIController {
    val serverSettings: Settings = Settings

    // Get All Settings
    @GetMapping("/api/settings/get")
    fun getTrashCanPath(): SettingsResponseDto {
        return SettingsResponseDto()
    }

    // Null-Variable will be ignored, only SOMETHING NOT-NULL will be set.
    @PutMapping("/api/settings/set")
    fun setTrashCanPath(@RequestBody request: SettingsRequestDto): SettingsResponseDto {
        serverSettings.trashCanPath = request.trashCanPath ?: serverSettings.trashCanPath
        return SettingsResponseDto()
    }
}
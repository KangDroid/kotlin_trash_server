package com.kangdroid.server.dto

import com.kangdroid.server.settings.Settings

class SettingsResponseDto {
    var trashCanPath: String = Settings.trashCanPath
    var serverVersion: String = Settings.serverVersion
}
package com.kangdroid.server.dto

import com.kangdroid.server.domain.TrashData

class TrashDataSaveRequestDto(
    var cwdLocation: String = "",
    var originalFileDirectory: String = "", ) {

    fun toEntity(): TrashData {
        return TrashData(cwdLocation = cwdLocation,
            originalFileDirectory = originalFileDirectory,
            trashFileDirectory = ""
        )
    }
}
package com.kangdroid.server.dto

import com.kangdroid.server.domain.TrashData

class TrashDataSaveRequestDto(
    var cwdLocation: String = "",
    var originalFileDirectory: String = "",
) {

    var trashFileDirectory: String? = null

    var id: Long = 0;

    fun toEntity(): TrashData {
        return TrashData(
            id = this.id,
            cwdLocation = cwdLocation,
            originalFileDirectory = originalFileDirectory,
            trashFileDirectory = trashFileDirectory ?: ""
        )
    }
}
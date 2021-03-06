package com.kangdroid.server.dto

import com.kangdroid.server.domain.TrashData

class TrashDataSaveRequestDto(
    var id: Long = 0,
    var cwdLocation: String = "",
    var originalFileDirectory: String = "",
    var trashFileDirectory: String? = null
) {

    constructor(responseDto: TrashDataResponseDto): this(
        id = responseDto.id,
        cwdLocation = responseDto.cwdLocation,
        originalFileDirectory = responseDto.originalFileDirectory,
        trashFileDirectory = responseDto.trashFileDirectory
    )

    fun toEntity(): TrashData {
        return TrashData(
            id = this.id,
            cwdLocation = cwdLocation,
            originalFileDirectory = originalFileDirectory,
            trashFileDirectory = trashFileDirectory ?: ""
        )
    }
}
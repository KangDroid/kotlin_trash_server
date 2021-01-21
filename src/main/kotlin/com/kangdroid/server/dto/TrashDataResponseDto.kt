package com.kangdroid.server.dto

import com.kangdroid.server.domain.TrashData

class TrashDataResponseDto(
    var id: Long = 0,
    var cwdLocation: String,
    var originalFileDirectory: String,
    var trashFileDirectory: String
) {

    constructor(entity: TrashData) : this(
        entity.id,
        entity.cwdLocation,
        entity.originalFileDirectory,
        entity .trashFileDirectory
    )
}
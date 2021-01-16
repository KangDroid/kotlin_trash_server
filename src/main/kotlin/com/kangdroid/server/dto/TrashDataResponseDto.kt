package com.kangdroid.server.dto

import com.kangdroid.server.domain.TrashData

class TrashDataResponseDto(entity: TrashData) {
    var id: Long = entity.id
    var cwdLocation: String = entity.cwdLocation
    var originalFileDirectory: String = entity.originalFileDirectory
    var trashFileDirectory: String = entity.trashFileDirectory
}
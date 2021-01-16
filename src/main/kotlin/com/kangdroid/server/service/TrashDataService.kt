package com.kangdroid.server.service

import com.kangdroid.server.domain.TrashData
import com.kangdroid.server.domain.TrashDataRepository
import com.kangdroid.server.dto.TrashDataResponseDto
import org.springframework.stereotype.Service
import java.util.*

@Service
class TrashDataService (val trashDataRepository: TrashDataRepository) {
    fun findById(id: Long): TrashDataResponseDto {
        val entityOptional: Optional<TrashData> = trashDataRepository.findById(id) ?: throw IllegalArgumentException("No such ID: $id")

        return TrashDataResponseDto(entityOptional.get())
    }
}
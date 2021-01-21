package com.kangdroid.server.service

import com.kangdroid.server.domain.TrashData
import com.kangdroid.server.domain.TrashDataRepository
import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Collectors

@Service
class TrashDataService(val trashDataRepository: TrashDataRepository) {
    fun findById(id: Long): TrashDataResponseDto {
        val entityOptional: Optional<TrashData> = trashDataRepository.findById(id) ?: throw IllegalArgumentException("No such ID: $id")

        return TrashDataResponseDto(entityOptional.get())
    }

    fun save(trashDataSaveRequestDto: TrashDataSaveRequestDto): Long {
        return trashDataRepository.save(trashDataSaveRequestDto.toEntity()).id
    }

    @Transactional(readOnly = true)
    fun findAllDesc(): List<TrashDataResponseDto> {
        return trashDataRepository.findAllDesc().stream()
            .map { TrashDataResponseDto(it) }
            .collect(Collectors.toList())
    }
}
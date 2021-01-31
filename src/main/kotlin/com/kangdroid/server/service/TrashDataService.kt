package com.kangdroid.server.service

import com.kangdroid.server.domain.TrashData
import com.kangdroid.server.domain.TrashDataRepository
import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.remover.RemoverService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Collectors

@Service
class TrashDataService(val trashDataRepository: TrashDataRepository) {

    fun findById(id: Long): TrashDataResponseDto {
        val entityOptional: Optional<TrashData> =
            trashDataRepository.findById(id) ?: throw IllegalArgumentException("No such ID: $id")

        return TrashDataResponseDto(entityOptional.get())
    }

    fun save(trashDataSaveRequestDto: TrashDataSaveRequestDto): Long {
        return trashDataRepository.save(trashDataSaveRequestDto.toEntity()).id
    }

    fun findAllDesc(removerService: RemoverService): List<TrashDataResponseDto> {
        return removerService.trashList.toList().map {
            TrashDataResponseDto(
                id = it.second.id,
                cwdLocation = it.second.cwdLocation,
                originalFileDirectory = it.second.originalFileDirectory,
                trashFileDirectory = it.second.trashFileDirectory ?: "Unknown"
            )
        }
    }

    @Transactional(readOnly = true)
    fun findAllDescDb(): List<TrashDataResponseDto> {
        return trashDataRepository.findAllDesc().stream()
            .map { TrashDataResponseDto(it) }
            .collect(Collectors.toList())
    }

    @Transactional(readOnly = true)
    fun findByTrashFileDirectory(input: String): List<TrashDataResponseDto> {
        return trashDataRepository.findByTrashFileDirectoryEquals(input).stream()
            .map { TrashDataResponseDto(it) }
            .collect(Collectors.toList())
    }

    fun removeByEntity(entity: TrashData) {
        trashDataRepository.delete(entity)
    }

    fun deleteAll() {
        trashDataRepository.deleteAll()
    }
}
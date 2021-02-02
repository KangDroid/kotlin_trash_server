package com.kangdroid.server.service

import com.kangdroid.server.domain.TrashData
import com.kangdroid.server.domain.TrashDataRepository
import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.remover.RemoverService
import com.kangdroid.server.settings.Settings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

@Service
class TrashDataService {
    @Autowired
    lateinit var trashDataRepository: TrashDataRepository

    @Autowired
    lateinit var settings: Settings

    // The hashmap[if setting is enabled]
    val trashList: ConcurrentHashMap<String, TrashDataSaveRequestDto> = ConcurrentHashMap()

    fun findById(id: Long): TrashDataResponseDto {
        val entityOptional: Optional<TrashData> =
            trashDataRepository.findById(id) ?: throw IllegalArgumentException("No such ID: $id")

        return TrashDataResponseDto(entityOptional.get())
    }

    /**
     * Global Abstract one from here
     * The functions below should implement trashList[concurrentHashMap] function as well.
     */
    fun findTargetByTrashFile(input: String): TrashDataResponseDto? {
        val returnTrashDataResponseDto = trashDataRepository.findByTrashFileDirectoryEquals(input)
        return returnTrashDataResponseDto?.let { TrashDataResponseDto(it) }
    }

    fun save(trashDataSaveRequestDto: TrashDataSaveRequestDto): Long {
        return trashDataRepository.save(trashDataSaveRequestDto.toEntity()).id
    }

    fun removeData(input: String) {
        trashDataRepository.deleteByTrashFileDirectory(input)
    }

    fun deleteAll() {
        trashDataRepository.deleteAll()
    }

    fun size(): Int {
        return trashDataRepository.count().toInt()
    }

    @Transactional(readOnly = true)
    fun findAllDescDb(): List<TrashDataResponseDto> {
        return trashDataRepository.findAllDesc().stream()
            .map { TrashDataResponseDto(it) }
            .collect(Collectors.toList())
    }
}
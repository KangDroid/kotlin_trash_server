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
import java.util.concurrent.ConcurrentMap
import java.util.stream.Collectors

@Service
class TrashDataService {
    @Autowired
    lateinit var trashDataRepository: TrashDataRepository

    @Autowired
    lateinit var settings: Settings

    // The hashmap[if setting is enabled]
    val trashList: ConcurrentMap<String, TrashDataSaveRequestDto> = ConcurrentHashMap()

    fun findById(id: Long): TrashDataResponseDto {
        val entityOptional: Optional<TrashData> =
            trashDataRepository.findById(id) ?: throw IllegalArgumentException("No such ID: $id")

        return TrashDataResponseDto(entityOptional.get())
    }

    fun deleteById(id: Long) {
        trashDataRepository.deleteById(id)
    }

    /**
     * Global Abstract one from here
     * The functions below should implement trashList[concurrentHashMap] function as well.
     */
    fun findTargetByTrashFile(input: String): TrashDataResponseDto? {
        return if (settings.lowMemoryOption == true) {
            val returnTrashDataResponseDto = trashDataRepository.findByTrashFileDirectoryEquals(input)
            returnTrashDataResponseDto?.let { TrashDataResponseDto(it) }
        } else {
            if (trashList.contains(input)) {
                TrashDataResponseDto(trashList[input]!!.toEntity())
            } else {
                null
            }
        }
    }

    fun save(trashDataSaveRequestDto: TrashDataSaveRequestDto): Long {
        return if (settings.lowMemoryOption == true) {
            trashDataRepository.save(trashDataSaveRequestDto.toEntity()).id
        } else {
            trashList[trashDataSaveRequestDto.trashFileDirectory!!] = trashDataSaveRequestDto
            10
        }
    }

    fun removeData(input: String) {
        if (settings.lowMemoryOption == true) {
            trashDataRepository.deleteByTrashFileDirectory(input)
        } else {
            trashList.remove(input)
        }
    }

    fun deleteAll() {
        if (settings.lowMemoryOption == true) {
            trashDataRepository.deleteAll()
        } else {
            trashList.clear()
        }
    }

    fun size(): Int {
        return if (settings.lowMemoryOption == true) {
            trashDataRepository.count().toInt()
        } else {
            trashList.size
        }
    }

    @Transactional(readOnly = true)
    fun findAllDescDb(): List<TrashDataResponseDto> {
        return if (settings.lowMemoryOption == true) {
            trashDataRepository.findAllDesc().stream()
                .map { TrashDataResponseDto(it) }
                .collect(Collectors.toList())
        } else {
            trashList.map {
                TrashDataResponseDto(it.value.toEntity())
            }.stream().collect(Collectors.toList())
        }
    }
}
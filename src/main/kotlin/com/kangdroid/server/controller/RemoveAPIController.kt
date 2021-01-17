package com.kangdroid.server.controller

import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.remover.RemoverService
import com.kangdroid.server.service.TrashDataService
import org.springframework.web.bind.annotation.*

@RestController
class RemoveAPIController(val trashDataService: TrashDataService) {

    val removerService: RemoverService = RemoverService()

    @GetMapping("/api/alive")
    fun serverAlive(): String {
        return "Server is Running!"
    }

    @GetMapping("/api/trash/data/{id}")
    fun getEachData(@PathVariable id: Long): TrashDataResponseDto {
        return trashDataService.findById(id)
    }

    // This means erase files.
    @PostMapping("/api/trash/data")
    fun postTrashData(@RequestBody trashDataSaveRequestDto: TrashDataSaveRequestDto): Long {
        trashDataSaveRequestDto.trashFileDirectory = removerService.checkTrashCan(trashDataSaveRequestDto.originalFileDirectory)
        removerService.remove(trashDataSaveRequestDto)
        return trashDataService.save(trashDataSaveRequestDto)
    }
}
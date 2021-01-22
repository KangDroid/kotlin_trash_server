package com.kangdroid.server.controller

import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.remover.RemoverService
import com.kangdroid.server.service.TrashDataService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class RemoveAPIController(val trashDataService: TrashDataService) {

    @Autowired
    private lateinit var removerService: RemoverService

    @GetMapping("/api/alive")
    fun serverAlive(): String {
        return "Server is Running!"
    }

    @GetMapping("/api/trash/data/{id}")
    fun getEachData(@PathVariable id: Long): TrashDataResponseDto {
        return trashDataService.findById(id)
    }

    @GetMapping("/api/trash/data/all")
    fun getAllData(): List<TrashDataResponseDto> {
        return trashDataService.findAllDesc()
    }

    // This means erase files.
    // TODO: Return path
    @PostMapping("/api/trash/data")
    fun postTrashData(@RequestBody trashDataSaveRequestDto: TrashDataSaveRequestDto): String {
        trashDataSaveRequestDto.trashFileDirectory = removerService.checkTrashCan(trashDataSaveRequestDto.originalFileDirectory)
        removerService.remove(trashDataSaveRequestDto)
        trashDataService.save(trashDataSaveRequestDto)
        removerService.restartService()
        return trashDataSaveRequestDto.trashFileDirectory!!
    }
}
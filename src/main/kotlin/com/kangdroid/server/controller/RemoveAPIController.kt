package com.kangdroid.server.controller

import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.service.TrashDataService
import org.springframework.web.bind.annotation.*

@RestController
class RemoveAPIController(val trashDataService: TrashDataService) {
    @GetMapping("/api/trash/data/{id}")
    fun getEachData(@PathVariable id: Long): TrashDataResponseDto {
        return trashDataService.findById(id)
    }

    // This means erase files.
    @PostMapping("/api/trash/data")
    fun postTrashData(@RequestBody trashDataSaveRequestDto: TrashDataSaveRequestDto): Long {
         return trashDataService.save(trashDataSaveRequestDto)
    }
}
package com.kangdroid.server.controller

import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.service.TrashDataService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class RemoveAPIController(val trashDataService: TrashDataService) {
    @GetMapping("/api/trash/data/{id}")
    fun getEachData(@PathVariable id: Long): TrashDataResponseDto {
        return trashDataService.findById(id)
    }
}
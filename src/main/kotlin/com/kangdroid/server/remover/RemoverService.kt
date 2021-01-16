package com.kangdroid.server.remover

import com.kangdroid.server.dto.TrashDataSaveRequestDto
import java.io.File
import java.time.LocalDateTime
import java.util.*

class RemoverService {
    private val trashCanDirectory: String = "/Users/kangdroid/Desktop/test_trashcan"
    private lateinit var trashList: Vector<String>

    init {
        initData()
    }

    private fun initData() {
        // Make a vector array
        File(trashCanDirectory).list()?.forEach {
            trashList.add(it)
        }
    }

    // Target: to delete, return final name
    fun checkTrashCan(target: String): String {
        return if (target in trashList) {
            // change name
            val changedString: String = target + "_${LocalDateTime.now()}"
            File(trashCanDirectory, changedString).absolutePath.toString()
        } else {
            File(trashCanDirectory, target).absolutePath.toString()
        }
    }

    fun remove(trashDataSaveRequestDto: TrashDataSaveRequestDto) {
        val fileOriginal: File = File(trashDataSaveRequestDto.originalFileDirectory)
        fileOriginal.renameTo(File(trashDataSaveRequestDto.trashFileDirectory))
        trashList.add(trashDataSaveRequestDto.trashFileDirectory)
    }
}
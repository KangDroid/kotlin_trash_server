package com.kangdroid.server.remover

import com.kangdroid.server.dto.TrashDataSaveRequestDto
import java.io.File
import java.time.LocalDateTime
import java.util.*

class RemoverService {
    private val trashCanDirectory: String = "/Users/kangdroid/Desktop/test_trashcan"
    private var trashList: HashMap<String, String> = HashMap<String, String>()

    init {
        initData()
    }

    private fun initData() {
        // Make a vector array
        File(trashCanDirectory).list()?.forEach {
            val fileObject: File = File(it)
            trashList[fileObject.name] = fileObject.lastModified().toString()
        }
    }

    // Target: to delete, return final name
    fun checkTrashCan(target: String): String {
        val testFile: File = File(target)
        return if (trashList.containsKey(testFile.name)) {
            // change name
            val changedString: String = testFile.name + "_${LocalDateTime.now()}"
            File(trashCanDirectory, changedString).absolutePath.toString()
        } else {
            File(trashCanDirectory, testFile.name).absolutePath.toString()
        }
    }

    fun remove(trashDataSaveRequestDto: TrashDataSaveRequestDto) {
        val fileOriginal: File = File(trashDataSaveRequestDto.originalFileDirectory)
        fileOriginal.renameTo(File(trashDataSaveRequestDto.trashFileDirectory))
        trashList[fileOriginal.name] = fileOriginal.lastModified().toString()
    }
}
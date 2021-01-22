package com.kangdroid.server.remover

import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.remover.watcher.InternalFileWatcher
import com.kangdroid.server.remover.watcher.JVMWatcher
import com.kangdroid.server.service.TrashDataService
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime
import java.util.*

@Component
class RemoverService(private val dataService: TrashDataService) {
    private val trashCanDirectory: String = "/Users/kangdroid/Desktop/test_trashcan"
    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private lateinit var syncJob: Job
    private var internalFileWatcher: InternalFileWatcher? = null

    init {
        initData()
        pollList()
    }

    private fun pollList() {
        internalFileWatcher = JVMWatcher(trashCanDirectory, dataService)
        syncJob = coroutineScope.launch(Dispatchers.IO) {
            internalFileWatcher?.watchFolder()
        }
    }

    private fun initData() {
        // Make a vector array
        File(trashCanDirectory).list()?.forEach {
            val fileObject: File = File(it)
            val tmpTrashDataSaveRequestDto: TrashDataSaveRequestDto = TrashDataSaveRequestDto("EXTERNAL", "EXTERNAL")
            tmpTrashDataSaveRequestDto.trashFileDirectory = fileObject.absolutePath.toString()
            dataService.save(tmpTrashDataSaveRequestDto)
        }
    }

    // Target: to delete, return final name
    fun checkTrashCan(target: String): String {
        // Close before work.
        if (syncJob.isActive) {
            internalFileWatcher?.closeWatcher()
            syncJob.cancel()
        }

        val testFile: File = File(target)
        val expectLocation: String = File(trashCanDirectory, testFile.name).absolutePath.toString()
        val listResponse: List<TrashDataResponseDto> = dataService.findByTrashFileDirectory(expectLocation)

        return if (listResponse.isNotEmpty()) {
            // Change Name
            val changedString: String = testFile.name + "_${LocalDateTime.now()}"
            File(trashCanDirectory, changedString).absolutePath.toString()
        } else {
            // Just use expect location
            expectLocation
        }
    }

    fun remove(trashDataSaveRequestDto: TrashDataSaveRequestDto) {
        val fileOriginal: File = File(trashDataSaveRequestDto.originalFileDirectory)
        val targetFile: File = File(trashDataSaveRequestDto.trashFileDirectory)
        if (!fileOriginal.renameTo(targetFile)) {
            println("Something went wrong.")
        }
    }
    fun restartService() {
        pollList()
    }
}
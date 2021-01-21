package com.kangdroid.server.remover

import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.remover.watcher.InternalFileWatcher
import com.kangdroid.server.remover.watcher.JVMWatcher
import com.kangdroid.server.remover.watcher.macOSWatcher
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDateTime
import java.util.*

class RemoverService {
    private val trashCanDirectory: String = "/Users/kangdroid/Desktop/test_trashcan"
    private var trashList: HashMap<String, String> = HashMap<String, String>()
    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private lateinit var syncJob: Job
    private var internalFileWatcher: InternalFileWatcher? = null

    init {
        initData()
        pollList()
    }

    private fun pollList() {
        internalFileWatcher = JVMWatcher(trashCanDirectory)
        syncJob = coroutineScope.launch(Dispatchers.IO) {
            internalFileWatcher?.regTrashList = trashList
            internalFileWatcher?.watchFolder()
        }
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
        // Close before work.
        if (syncJob.isActive) {
            internalFileWatcher?.closeWatcher()
            syncJob.cancel()
        }

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
        val targetFile: File = File(trashDataSaveRequestDto.trashFileDirectory)
        trashList[targetFile.name] = fileOriginal.lastModified().toString()
        if (!fileOriginal.renameTo(targetFile)) {
            println("Something went wrong.")
        }
    }
    fun restartService() {
        pollList()
    }
}
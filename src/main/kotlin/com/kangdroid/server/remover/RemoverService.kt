package com.kangdroid.server.remover

import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.remover.watcher.InternalFileWatcher
import com.kangdroid.server.remover.watcher.JVMWatcher
import com.kangdroid.server.service.TrashDataService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PreDestroy

@Component
class RemoverService(private val dataService: TrashDataService) {
    private val trashCanDirectory: String = "/Users/kangdroid/Desktop/test_trashcan"
    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private lateinit var syncJob: Job
    private var internalFileWatcher: InternalFileWatcher? = null
    val trashList: ConcurrentHashMap<String, TrashDataSaveRequestDto> = ConcurrentHashMap()

    init {
        initMap()
        initData()
        pollList()
    }

    private fun pollList() {
        internalFileWatcher = JVMWatcher(trashCanDirectory, trashList)
        syncJob = coroutineScope.launch(Dispatchers.IO) {
            internalFileWatcher?.watchFolder()
        }
    }

    private fun initData() {
        // Make a vector array
        File(trashCanDirectory).list()?.forEach {
            val fileObject: File = File(it)

            // When there is unknown files/folder --> Save it to DB[With EXTERNAL keyword]
            if (!trashList.containsKey("${trashCanDirectory}/${fileObject.name}")) {
                val tmpTrashDataSaveRequestDto: TrashDataSaveRequestDto = TrashDataSaveRequestDto(
                    cwdLocation = "EXTERNAL",
                    originalFileDirectory = "EXTERNAL",
                    trashFileDirectory = "${trashCanDirectory}/${fileObject.name}"
                )
                trashList["${trashCanDirectory}/${fileObject.name}"] = tmpTrashDataSaveRequestDto
            }
        }
    }

    @PreDestroy
    fun saveBackToDb() {
        println("Saving back!")
        dataService.deleteAll()
        trashList.forEach { (_, v) ->
            dataService.save(v)
        }
    }

    private fun initMap() {
        val dbData: List<TrashDataResponseDto> = dataService.findAllDescDb()
        for (response in dbData) {
            if (File(response.trashFileDirectory).exists()) {
                trashList[response.trashFileDirectory] = TrashDataSaveRequestDto(
                    id = response.id,
                    cwdLocation = response.cwdLocation,
                    originalFileDirectory = response.originalFileDirectory,
                    trashFileDirectory = response.trashFileDirectory
                )
            }
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

        return if (trashList.containsKey(expectLocation)) {
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

        with(trashDataSaveRequestDto) {
            trashList[trashFileDirectory!!] = TrashDataSaveRequestDto(
                cwdLocation = cwdLocation,
                originalFileDirectory = originalFileDirectory,
                trashFileDirectory = trashFileDirectory
            )
        }

        if (!fileOriginal.renameTo(targetFile)) {
            println("Something went wrong.")
        }
    }

    fun restartService() {
        pollList()
    }
}
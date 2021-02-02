package com.kangdroid.server.remover

import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataRestoreRequestDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.remover.watcher.InternalFileWatcher
import com.kangdroid.server.remover.watcher.JVMWatcher
import com.kangdroid.server.service.TrashDataService
import com.kangdroid.server.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Component
class RemoverService {
    @Autowired
    lateinit var settings: Settings

    @Autowired
    private lateinit var dataService: TrashDataService

    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private var syncJob: Job? = null
    private var internalFileWatcher: InternalFileWatcher? = null
    val RESTORE_TARGET_EXISTS: String = "Restore Target already exists!"
    val RESTORE_TARGET_NOT_ON_MAP: String = "Restore Target is not on server!"
    val RESTORE_RENAME_FAIL: String = "Renaming file failed."
    val RESTORE_FULL_SUCCESS: String = "Restore Complete."

    @PostConstruct
    fun testInit() {
        if (System.getProperty("kdr.isTesting") != "test") {
            initDB()
            initData()
            pollList()
        }
    }

    private fun pollList() {
        internalFileWatcher = JVMWatcher(settings.trashPath, dataService)
        syncJob = coroutineScope.launch(Dispatchers.IO) {
            internalFileWatcher?.watchFolder()
        }
    }

    // Local --> DB Setup
    private fun initData() {
        // Make a vector array
        File(settings.trashPath).list()?.forEach {
            val fileObject: File = File(it)

            // When there is unknown files/folder --> Save it to DB[With EXTERNAL keyword]
            if (dataService.findTargetByTrashFile("${settings.trashPath}/${fileObject.name}") == null) {
                val tmpTrashDataSaveRequestDto: TrashDataSaveRequestDto = TrashDataSaveRequestDto(
                    cwdLocation = "EXTERNAL",
                    originalFileDirectory = "EXTERNAL",
                    trashFileDirectory = "${settings.trashPath}/${fileObject.name}"
                )
                dataService.save(tmpTrashDataSaveRequestDto)
            }
        }
    }

    // DB --> Local[check invalid entry]
    fun initDB() {
        val dbList: List<TrashDataResponseDto> = dataService.findAllDescDb()

        for (target in dbList) {
            if (!File(target.trashFileDirectory).exists()) {
                dataService.deleteById(target.id)
            }
        }
    }

    // Target: to delete, return final name
    fun checkTrashCan(target: String): String {
        // Close before work.
        if (syncJob?.isActive == true) {
            internalFileWatcher?.closeWatcher()
            syncJob?.cancel()
        }

        val testFile: File = File(target)
        val expectLocation: String = File(settings.trashPath, testFile.name).absolutePath.toString()

        return if (dataService.findTargetByTrashFile(expectLocation) != null) {
            // Change Name
            val changedString: String = testFile.name + "_${LocalDateTime.now()}"
            File(settings.trashPath, changedString).absolutePath.toString()
        } else {
            // Just use expect location
            expectLocation
        }
    }

    fun remove(trashDataSaveRequestDto: TrashDataSaveRequestDto) {
        val fileOriginal: File = File(trashDataSaveRequestDto.originalFileDirectory)
        val targetFile: File = File(trashDataSaveRequestDto.trashFileDirectory)

        with(trashDataSaveRequestDto) {
            dataService.save(TrashDataSaveRequestDto(
                cwdLocation = cwdLocation,
                originalFileDirectory = originalFileDirectory,
                trashFileDirectory = trashFileDirectory
            ))
        }

        if (!fileOriginal.renameTo(targetFile)) {
            println("Something went wrong.")
        }
    }

    fun restore(trashDataRestoreRequestDto: TrashDataRestoreRequestDto): String {
        val tmpResponseDto = dataService.findTargetByTrashFile(trashDataRestoreRequestDto.trashFileDirectory)
            ?: return RESTORE_TARGET_NOT_ON_MAP
        val trashDataSaveRequestDto: TrashDataSaveRequestDto = TrashDataSaveRequestDto(
            id = tmpResponseDto.id,
            cwdLocation = tmpResponseDto.cwdLocation,
            originalFileDirectory = tmpResponseDto.originalFileDirectory,
            trashFileDirectory = tmpResponseDto.trashFileDirectory
        )
        val originalFileObject: File = File(trashDataSaveRequestDto.originalFileDirectory)
        if (originalFileObject.exists()) {
            return RESTORE_TARGET_EXISTS
        }

        // Restore!
        val trashFileObject = File(trashDataSaveRequestDto.trashFileDirectory!!)

        return if (trashFileObject.renameTo(File(trashDataSaveRequestDto.originalFileDirectory))) {
            dataService.removeData(trashDataSaveRequestDto.trashFileDirectory!!)
            RESTORE_FULL_SUCCESS
        } else {
            RESTORE_RENAME_FAIL
        }
    }

    fun emptyTrashCan(): Boolean {
        val listTrashResponse: List<TrashDataResponseDto> = dataService.findAllDescDb()
        var returnValue: Boolean = true

        for (target in listTrashResponse) {
            returnValue = File(target.trashFileDirectory).deleteRecursively()
        }

        // clear all data
        dataService.deleteAll()

        return returnValue
    }

    fun restartService() {
        pollList()
    }
}
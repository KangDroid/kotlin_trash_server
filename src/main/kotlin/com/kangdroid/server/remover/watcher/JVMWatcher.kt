package com.kangdroid.server.remover.watcher

import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.service.TrashDataService
import com.sun.nio.file.SensitivityWatchEventModifier
import java.io.File
import java.nio.file.*

/**
 * This class is for OS does not support
 * LIVE File watching from me.
 */
class JVMWatcher(trashDirectory: String, inputDataService: TrashDataService) :
    InternalFileWatcher(trashDirectory, inputDataService) {
    var watchKey: WatchKey? = null

    override fun watchFolder() {
        println("Starting")
        val targetPath: String = fileToWatch

        val realPath: Path = Paths.get(targetPath)

        val watchService: WatchService = FileSystems.getDefault().newWatchService()

        realPath.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE
        )

        realPath.register(
            watchService,
            arrayOf<WatchEvent.Kind<*>>(
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE
            ),
            SensitivityWatchEventModifier.HIGH
        )

        while (isContinue) {
            try {
                watchKey = watchService.take() // Start wait
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            val events = watchKey?.pollEvents() // Get Events

            if (events != null) {
                for (event in events) {
                    val kind = event.kind()
                    val paths: Path = event.context() as Path
                    val directory = paths.toAbsolutePath().toString()
                    val fileObject: File = File(directory)

                    when (kind) {
                        StandardWatchEventKinds.ENTRY_CREATE -> {
                            val expectedLocation: String = File(fileToWatch, fileObject.name).absolutePath.toString()
                            val listResponse: List<TrashDataResponseDto> =
                                dataService.findByTrashFileDirectory(expectedLocation)
                            if (listResponse.isEmpty()) {
                                val tmpTrashDataSaveRequestDto: TrashDataSaveRequestDto =
                                    TrashDataSaveRequestDto(cwdLocation = "EXTERNAL", originalFileDirectory = "EXTERNAL", trashFileDirectory = expectedLocation)
                                dataService.save(tmpTrashDataSaveRequestDto)
                                println("Created: ${fileObject.name}")
                            }
                        }
                        StandardWatchEventKinds.ENTRY_DELETE -> {
                            val expectedLocation: String = File(fileToWatch, fileObject.name).absolutePath.toString()
                            val listResponse: List<TrashDataResponseDto> =
                                dataService.findByTrashFileDirectory(expectedLocation)

                            if (listResponse.isNotEmpty()) {
                                dataService.removeByEntity(listResponse[0].toEntity())
                                println("Removed: $directory")
                            }
                        }
                    }
                }
            }
            watchKey?.reset()
        }
    }

    override fun closeWatcher() {
        println("Canceled")
        isContinue = false
        watchKey?.cancel()
    }
}
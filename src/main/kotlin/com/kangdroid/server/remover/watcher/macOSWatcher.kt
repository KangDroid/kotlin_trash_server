package com.kangdroid.server.remover.watcher

import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.service.TrashDataService
import java.util.concurrent.ConcurrentHashMap

class macOSWatcher(trashDirectory: String, trashList: ConcurrentHashMap<String, TrashDataSaveRequestDto>) :
    InternalFileWatcher(trashDirectory, trashList) {
    var process: Process? = null

    protected fun finalize() {
        closeWatcher()
    }

    override fun watchFolder() {
        // TODO: Live-Compile or object in jar file.
        val targetObject: String = "/Users/kangdroid/Desktop/a.out $fileToWatch"
        while (isContinue) {
            try {
                process = Runtime.getRuntime().exec(targetObject)
                process?.waitFor();

                // Update logic goes here.
                println("Finished!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun closeWatcher() {
        synchronized(this) {
            isContinue = false
        }
        process?.destroy()
        val targetString: String = "kill -9 ${process?.pid()}"
        println(targetString)
        try {
            Runtime.getRuntime().exec(targetString).waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
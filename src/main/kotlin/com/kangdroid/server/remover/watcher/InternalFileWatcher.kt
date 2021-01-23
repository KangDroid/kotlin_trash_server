package com.kangdroid.server.remover.watcher

import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.service.TrashDataService
import java.util.concurrent.ConcurrentHashMap

abstract class InternalFileWatcher(val fileToWatch: String = "/tmp", val trashList: ConcurrentHashMap<String, TrashDataSaveRequestDto>) {
    var isContinue: Boolean = true

    //    lateinit var regTrashList: HashMap<String, String>
    abstract fun watchFolder()
    abstract fun closeWatcher()
}
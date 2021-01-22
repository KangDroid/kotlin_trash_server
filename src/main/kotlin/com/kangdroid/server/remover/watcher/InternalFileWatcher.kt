package com.kangdroid.server.remover.watcher

import com.kangdroid.server.service.TrashDataService

abstract class InternalFileWatcher(val fileToWatch: String = "/tmp", val dataService: TrashDataService) {
    var isContinue: Boolean = true
//    lateinit var regTrashList: HashMap<String, String>
    abstract fun watchFolder()
    abstract fun closeWatcher()
}
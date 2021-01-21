package com.kangdroid.server.remover.watcher

abstract class InternalFileWatcher(val fileToWatch: String = "/tmp") {
    var isContinue: Boolean = true
    lateinit var regTrashList: HashMap<String, String>
    abstract fun watchFolder()
    abstract fun closeWatcher()
}
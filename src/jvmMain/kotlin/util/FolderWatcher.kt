package util

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.Closeable
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.CoroutineContext

class FolderWatcher(
    ctx: CoroutineContext,
    val folder: File,
    val onFileUpdate: suspend (File, ChangeType) -> Unit
): Closeable {

    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(ctx + supervisorJob)

    private val mapMutex = Mutex()
    private val fileLastUpdate = HashMap<String, Long>()

    init {
        scope.launch(Dispatchers.IO) {
            watchFolder()
        }
    }

    enum class ChangeType {
        created,
        updated,
        deleted
    }

    private suspend fun watchFolder() {
        while(true) {
            walkFolder(folder)
            scanDeleted()
            delay(1000)
        }
    }

    private suspend fun walkFolder(folder: File) {
        if(folder.isFile) {
            foundFile(folder)
        } else if(folder.isDirectory) {
            folder.listFiles()?.map({
                scope.launch {
                    walkFolder(it)
                }
            })?.joinAll()
        }
    }

    private suspend fun foundFile(file: File) {
        val storedUpdate = mapMutex.withLock {
            fileLastUpdate[file.absolutePath]
        }

        val fileUpdate = file.lastModified()

        if(storedUpdate == null || storedUpdate < fileUpdate) {
            mapMutex.withLock {
                fileLastUpdate[file.absolutePath] = fileUpdate
            }
            onFileUpdate(file, if(storedUpdate == null) ChangeType.created else ChangeType.updated)
        }
    }

    private suspend fun scanDeleted() {
        fileLastUpdate.keys.toList()
            .map({
                scope.launch {
                    val file = File(it)
                    if(!file.exists()) {
                        mapMutex.withLock {
                            fileLastUpdate.remove(file.absolutePath)
                        }
                        onFileUpdate(file, ChangeType.deleted)
                    }
                }
            }).forEach({ it.join() })
    }

    override fun close() {
        runBlocking {
            supervisorJob.cancelAndJoin()
        }
    }
}
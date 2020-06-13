package dev.matsem.ala.tools.live

import com.sun.nio.file.SensitivityWatchEventModifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent

class FileWatcher {

    private val watcher = FileSystems.getDefault().newWatchService()

    fun watchFile(file: File, onChange: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val path = file.parentFile.toPath()
            path.register(
                watcher,
                arrayOf(StandardWatchEventKinds.ENTRY_MODIFY),
                SensitivityWatchEventModifier.HIGH
            )

            while (true) {
                val watchKey = watcher.poll()
                watchKey?.pollEvents()?.forEach {
                    val eventKind = it.kind()
                    if (eventKind == StandardWatchEventKinds.OVERFLOW) {
                        return@forEach
                    }

                    val p = (it as WatchEvent<Path>).context()
                    if (p.fileName.toString() != file.name) {
                        println("🚧 Invalid file")
                        return@forEach
                    }

                    onChange()
                }

                if (watchKey?.reset() == false) {
                    throw CancellationException("WatchKey invalidated")
                }
            }
        }
    }
}
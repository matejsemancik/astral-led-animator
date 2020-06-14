package dev.matsem.ala.tools.live

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import processing.core.PApplet
import java.io.File
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class ScriptLoader {

    val engine: ScriptEngine =
        ScriptEngineManager(Thread.currentThread().contextClassLoader).getEngineByExtension("kts")

    suspend inline fun <reified T> loadScript(scriptFile: File): T {
        check(scriptFile.exists()) { "Script file does not exist" }
        val sourceCode = withContext(Dispatchers.IO) { scriptFile.readText() }

        PApplet.println("[${Thread.currentThread().name}] Loading script...")
        val start = System.currentTimeMillis()
        val loadedFunction = engine.eval(sourceCode).castOrError<T>()
        val end = System.currentTimeMillis()
        PApplet.println("Done. Script loaded in ${end - start} ms.")
        return loadedFunction
    }

    inline fun <reified T> Any?.castOrError() = takeIf { it is T }?.let { it as T }
        ?: throw IllegalArgumentException("Cannot cast $this to expected type ${T::class}")
}
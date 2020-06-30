package dev.matsem.ala.tools.live

import dev.matsem.ala.generators.BaseLiveGenerator
import java.io.File

class GeneratorLiveScript(private val file: File) {

    suspend fun reload() {
        val scriptLoader = ScriptLoader()
        scriptLoader.loadScript<BaseLiveGenerator>(file)
    }
}
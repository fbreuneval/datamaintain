package datamaintain.core.report

import datamaintain.core.script.ExecutedScript
import datamaintain.core.script.ScriptWithContent
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Report @JvmOverloads constructor(
        val scannedScripts: List<ScriptWithContent> = listOf(),
        val filteredScripts: List<ScriptWithContent> = listOf(),
        val prunedScripts: List<ScriptWithContent> = listOf(),
        val executedScripts: List<ExecutedScript> = listOf(),
        val scriptInError: ExecutedScript? = null
) {
    fun print(verbose: Boolean) {
        logger.info { "Summary => " }
        logger.info { "- ${scannedScripts.size} files scanned" }
        if (verbose) {
            scannedScripts.forEach {logger.info { " -> ${it.name}" }}
        }
        logger.info { "- ${filteredScripts.size} files filtered" }
        if (verbose) {
            filteredScripts.forEach {logger.info { " -> ${it.name}" }}
        }
        logger.info { "- ${prunedScripts.size} files pruned" }
        if (verbose) {
            prunedScripts.forEach {logger.info { " -> ${it.name}" }}
        }
        logger.info { "- ${executedScripts.size} files executed" }
        executedScripts.forEach {logger.info { " -> ${it.name}" }}
        if (scriptInError != null) {
            logger.info { "- but last executed script is in error : ${scriptInError.name}" }
        }
    }
}


class ReportBuilder @JvmOverloads constructor(
        private val scannedScripts: MutableList<ScriptWithContent> = mutableListOf(),
        private val filteredScripts: MutableList<ScriptWithContent> = mutableListOf(),
        private val prunedScripts: MutableList<ScriptWithContent> = mutableListOf(),
        private val executedScripts: MutableList<ExecutedScript> = mutableListOf(),
        private var scriptInError: ExecutedScript? = null
) {

    fun addScannedScript(script: ScriptWithContent) {
        scannedScripts.add(script)
    }

    fun addFilteredScript(script: ScriptWithContent) {
        filteredScripts.add(script)
    }

    fun addPrunedScript(script: ScriptWithContent) {
        prunedScripts.add(script)
    }

    fun addExecutedScript(script: ExecutedScript) {
        executedScripts.add(script)
    }

    fun inError(script: ExecutedScript) {
        scriptInError = script
    }

    fun toReport() = Report(
            scannedScripts,
            filteredScripts,
            prunedScripts,
            executedScripts,
            scriptInError
    )
}
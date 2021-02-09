package datamaintain.core.step

import datamaintain.core.Context
import datamaintain.core.exception.DatamaintainBaseException
import datamaintain.core.exception.DatamaintainException
import datamaintain.core.script.ScriptWithContent
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Pruner(private val context: Context) {
    fun prune(scripts: List<ScriptWithContent>): List<ScriptWithContent> {
        try {
            logger.info { "Prune scripts..." }
            val executedChecksums = context.dbDriver.listExecutedScripts()
                    .map { executedScript -> executedScript.checksum }
                    .toList()
            val prunedScripts = scripts
                    .filterNot { script ->
                        val skipped = executedChecksums.contains(script.checksum) &&
                                script.tags.intersect(context.config.tagsToPlayAgain).isEmpty()
                        if (context.config.verbose && skipped) {
                            logger.info { "${script.name} is skipped because it was already executed " +
                                    "and it does not have a tag to play again." }
                        }
                        skipped
                    }
                    .onEach { context.reportBuilder.addPrunedScript(it) }

            logger.info { "${prunedScripts.size} scripts pruned (${executedChecksums.size} skipped)" }
            logger.info { "" }
            return prunedScripts
        } catch (datamaintainException: DatamaintainBaseException) {
            throw DatamaintainException(
                datamaintainException.message,
                Step.PRUNE,
                context.reportBuilder,
                datamaintainException.resolutionMessage
            )
        }
    }
}

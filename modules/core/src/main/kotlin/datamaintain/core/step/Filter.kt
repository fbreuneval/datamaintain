package datamaintain.core.step

import datamaintain.core.Context
import datamaintain.core.script.ScriptWithContent
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Filter(private val context: Context) {
    fun filter(scripts: List<ScriptWithContent>): List<ScriptWithContent> {
        logger.info { "Filter scripts..." }
        var filteredScripts = scripts

        if (context.config.whitelistedTags.isNotEmpty()) {
            filteredScripts = filteredScripts.filter { script ->
                val kept = context.config.whitelistedTags.any { it isIncluded script }

                if (context.config.verbose && !kept) {
                    logger.info { "${script.name} is skipped because not whitelisted" }
                }

                kept
            }
        }

        if (context.config.blacklistedTags.isNotEmpty()) {
            filteredScripts = filteredScripts.filterNot { script ->
                val skipped = context.config.blacklistedTags.any { it isIncluded script }
                if (context.config.verbose && skipped) {
                    logger.info { "${script.name} is skipped because blacklisted" }
                }
                skipped
            }
        }

        filteredScripts = filteredScripts.onEach { context.reportBuilder.addFilteredScript(it) }

        logger.info { "${filteredScripts.size} scripts filtered (${scripts.size - filteredScripts.size} skipped)" }
        logger.info { "" }
        return filteredScripts
    }
}

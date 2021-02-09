package datamaintain.core.step.executor

import datamaintain.core.Context
import datamaintain.core.exception.DatamaintainBaseException
import datamaintain.core.exception.DatamaintainException
import datamaintain.core.report.Report
import datamaintain.core.script.ExecutedScript
import datamaintain.core.script.ExecutionStatus
import datamaintain.core.script.ScriptWithContent
import datamaintain.core.step.Step
import mu.KotlinLogging
import java.time.Clock
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class Executor(private val context: Context) {

    fun execute(scripts: List<ScriptWithContent>): Report {
        try {
            logger.info { "Executes scripts.." }
            for (script in scripts) {
                val executedScript = when (context.config.executionMode) {
                    ExecutionMode.NORMAL -> {
                        var execution = Execution(ExecutionStatus.SHOULD_BE_EXECUTED)
                        val executionDurationInMillis = measureTimeMillis {
                            execution = context.dbDriver.executeScript(script)
                        }
                        ExecutedScript.build(script, execution, executionDurationInMillis)
                    }
                    ExecutionMode.FORCE_MARK_AS_EXECUTED -> ExecutedScript.forceMarkAsExecuted(script)
                    ExecutionMode.DRY -> ExecutedScript.shouldBeExecuted(script)
                }

                context.reportBuilder.addExecutedScript(executedScript)

                when (executedScript.executionStatus) {
                    ExecutionStatus.OK -> {
                        markAsExecuted(executedScript)
                        logger.info { "${executedScript.name} executed" }
                    }
                    ExecutionStatus.FORCE_MARKED_AS_EXECUTED -> {
                        markAsExecuted(executedScript)
                        logger.info { "${executedScript.name} only marked (not really executed)" }
                    }
                    ExecutionStatus.KO -> {
                        logger.info { "${executedScript.name} has not been correctly executed" }
                        // TODO handle interactive shell
                        return context.reportBuilder.toReport()
                    }
                    else -> logger.info { "${executedScript.name} should be executed (dry run)" }
                }
            }
            logger.info { "" }
            return context.reportBuilder.toReport()
        } catch (datamaintainException: DatamaintainBaseException) {
            throw DatamaintainException(
                datamaintainException.message,
                Step.EXECUTE,
                context.reportBuilder,
                datamaintainException.resolutionMessage
            )
        }
    }

    private fun markAsExecuted(it: ExecutedScript) {
        try {
            context.dbDriver.markAsExecuted(it)
        } catch (e: Exception) {
            logger.error { "error during mark execution of ${it.name} " }
            throw e
            // TODO handle interactive shell
        }
    }
}

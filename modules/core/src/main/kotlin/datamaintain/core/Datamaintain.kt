package datamaintain.core

import datamaintain.core.config.DatamaintainConfig
import datamaintain.core.report.Report
import datamaintain.core.step.Filter
import datamaintain.core.step.Pruner
import datamaintain.core.step.Scanner
import datamaintain.core.step.executor.Executor
import datamaintain.core.step.sort.Sorter
import mu.KotlinLogging
import java.time.Clock

private val logger = KotlinLogging.logger {}

class Datamaintain(config: DatamaintainConfig) {

    init {
        if (config.verbose) {
            config.log()
            config.driverConfig.log()
        }
    }

    val context = Context(
            config,
            config.driverConfig.toDriver()
    )

    fun updateDatabase(): Report {
        return Scanner(context).scan()
                .let { scripts -> Filter(context).filter(scripts) }
                .let { scripts -> Sorter(context).sort(scripts) }
                .let { scripts -> Pruner(context).prune(scripts) }
                .let { scripts -> Executor(context).execute(scripts) }
    }

    fun listExecutedScripts() = context.dbDriver.listExecutedScripts()

}


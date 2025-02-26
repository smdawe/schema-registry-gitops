package dev.domnikl.schema_registry_gitops

import dev.domnikl.schema_registry_gitops.cli.Apply
import dev.domnikl.schema_registry_gitops.cli.Dump
import dev.domnikl.schema_registry_gitops.cli.Plan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.io.InputStreamReader
import java.util.concurrent.Callable
import ch.qos.logback.classic.Logger as LogbackClassicLogger

@CommandLine.Command(
    name = "schema-registry-gitops",
    mixinStandardHelpOptions = true,
    versionProvider = CLI.Companion::class,
    description = ["Manages schema registries through Infrastructure as Code"]
)
class CLI : Callable<Int> {
    @CommandLine.Spec
    lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.Option(
        names = ["--properties"],
        description = ["a Java Properties file for client configuration (optional)"],
        scope = CommandLine.ScopeType.INHERIT
    )
    var propertiesFilePath: String? = null

    @CommandLine.Option(
        names = ["-r", "--registry"],
        description = ["schema registry endpoint, overwrites 'schema.registry.url' from properties, can also be a list of urls separated by comma"],
        scope = CommandLine.ScopeType.INHERIT
    )
    var baseUrl: String? = null

    @CommandLine.Option(
        names = ["-v", "--verbose"],
        description = ["enable verbose logging"],
        scope = CommandLine.ScopeType.INHERIT
    )
    fun setVerbose(verbose: Boolean) {
        if (verbose) {
            val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as LogbackClassicLogger
            rootLogger.level = ch.qos.logback.classic.Level.DEBUG
        }
    }

    override fun call(): Int {
        spec.commandLine().usage(System.out)

        return 0
    }

    companion object : CommandLine.IVersionProvider {
        fun commandLine(factory: Factory, logger: Logger = LoggerFactory.getLogger(CLI::class.java)): CommandLine {
            return CommandLine(CLI())
                .addSubcommand(CommandLine.HelpCommand::class.java)
                .addSubcommand(Plan(factory, logger))
                .addSubcommand(Apply(factory, logger))
                .addSubcommand(Dump(factory))
        }

        override fun getVersion(): Array<String> {
            val version = InputStreamReader(object {}::class.java.classLoader.getResourceAsStream("version.txt")!!).readText()

            return arrayOf("schema-registry-gitops $version")
        }
    }
}

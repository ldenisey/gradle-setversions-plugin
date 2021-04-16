package com.github.ldenisey.setversions.parser

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.*
import java.io.File

class KotlinBuildFileParser(file: File) : BuildFileParser(file) {

    override val versionExpression: String by lazy {
        versionKtBinaryExpression.text
    }

    override val versionDefinition: String by lazy {
        versionKtBinaryExpression.lastChild.text.replace(Regex("""["']"""), "")
    }

    private val psi: KtFile by lazy {
        val compilerConf = CompilerConfiguration()
        compilerConf.put(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false)
        )
        val disposable = Disposer.newDisposable()
        try {
            return@lazy KtPsiFactory(
                KotlinCoreEnvironment.createForProduction(
                    disposable, compilerConf, EnvironmentConfigFiles.JVM_CONFIG_FILES
                ).project
            ).createFile(file.name, content)
        } finally {
            disposable.dispose()
        }
    }

    private val versionKtBinaryExpression: KtBinaryExpression by lazy {
        val statements = psi.findChildByClass(KtScript::class.java)?.blockExpression?.statements
        if (statements != null) {
            for (statement: KtExpression in statements) {
                if (statement is KtScriptInitializer) {
                    val element: PsiElement = statement.firstChild
                    if (element is KtBinaryExpression && element.firstChild.text == "version") {
                        return@lazy element as KtBinaryExpression
                    }
                }
            }
        }
        throw VersionNotFound("Could not locate version binary expression in file ${file.path}")
    }
}
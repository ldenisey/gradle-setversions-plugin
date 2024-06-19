package com.github.ldenisey.setversions.parser

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.gradle.internal.impldep.org.apache.commons.lang.ArrayUtils
import java.io.File

class GroovyBuildFileParser(file: File) : BuildFileParser(file) {

    override val versionExpression: String by lazy {
        val newLine: String = if (Regex("\r\n").containsMatchIn(content)) "\r\n" else "\n"
        val contentLines = content.lines()
        var result = ""
        for (i in versionASTExpression.lineNumber until versionASTExpression.lastLineNumber + 1) {
            val startPos = if (i == versionASTExpression.lineNumber) versionASTExpression.columnNumber - 1 else 0
            result += if (i == versionASTExpression.lastLineNumber) {
                contentLines[i - 1].substring(
                    startPos,
                    versionASTExpression.lastColumnNumber - 1
                )
            } else {
                contentLines[i - 1].substring(startPos) + newLine
            }
        }
        return@lazy result
    }

    override val versionDefinition: String by lazy {
        return@lazy when (versionASTExpression) {
            is MethodCallExpression -> ((versionASTExpression as MethodCallExpression).arguments as ArgumentListExpression).expressions[0].text
            is BinaryExpression -> (versionASTExpression as BinaryExpression).rightExpression.text
            else -> throw VersionNotFound("Could not locate version definition in file ${file.path}")
        }
    }

    private val astNodes: List<ASTNode> by lazy {
        var nodes :List<ASTNode> = arrayListOf(ASTNode())
        if (content.isNotEmpty()) {
            nodes = AstBuilder().buildFromString(content)
        }

        return@lazy nodes
    }

    private val versionASTExpression: Expression by lazy {
        val blockStatement: BlockStatement = astNodes[0] as BlockStatement
        loop@ for (statement in blockStatement.statements) {
            val expression: Expression = when (statement) {
                is ExpressionStatement -> statement.expression
                is ReturnStatement -> statement.expression
                else -> continue@loop
            }

            if ((expression is MethodCallExpression && expression.method.text == "version")
                || (expression is BinaryExpression && expression.leftExpression.text == "version")
            ) {
                return@lazy expression
            }
        }
        throw VersionNotFound("Could not locate version declaration in file ${file.path}")
    }
}
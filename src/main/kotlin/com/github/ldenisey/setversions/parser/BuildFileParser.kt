package com.github.ldenisey.setversions.parser

import java.io.File

abstract class BuildFileParser(file: File) {

    val file: File = file

    val content: String = file.readText()

    /**
     * Get the project version expression, exactly as defined in the gradle build file.
     * @return Project version expression, i.e. 'version=project.findProperty("projectVersion").toString()',
     * 'version "1.0.0"' ...
     */
    abstract val versionExpression: String


    /**
     * Get the project version definition, after java class generation.
     * @return Project version definition.
     */
    abstract val versionDefinition: String
}
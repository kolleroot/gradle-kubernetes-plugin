package org.detoeuf

import io.swagger.codegen.DefaultGenerator

/**
 *
 MIT License

 Copyright (c) 2017 Jean Detoeuf

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
import io.swagger.codegen.config.CodegenConfigurator
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class SwaggerCodeGenTask extends DefaultTask {

    boolean cleanOutputDir = true

    private CodegenConfigurator codegenConfigurator

    @InputFile
    File getSwaggerFile() {
        project.file(getConfigurator().getInputSpec())
    }

    @InputDirectory
    File getTemplateDirectory() {
        project.file(getConfigurator().getTemplateDir())
    }

    @OutputDirectory
    File getOutputDirectory() {
        project.file(getConfigurator().getOutputDir())
    }

    @Internal
    CodegenConfigurator getConfigurator() {
        if (codegenConfigurator == null) {
            return project.extensions.findByName('swagger') as CodegenConfigurator
        } else {
            return codegenConfigurator
        }
    }

    void setConfigurator(CodegenConfigurator configurator) {
        this.codegenConfigurator = configurator
    }

    @OutputDirectory
    File outputDir() {
        return project.file(configurator.outputDir)
    }

    private deleteFileValidator(File against) {
        if (outputDir() == against) {
            throw new GradleException("You probably don't want to overwrite this directory: $against")
        }
    }

    @TaskAction
    void swaggerCodeGen() {
        deleteFileValidator(project.projectDir)
        deleteFileValidator(project.rootProject.projectDir)
        CodegenConfigurator config = configurator

        // anyway .. delete any existing files for a clean build
        if (cleanOutputDir) {
            project.delete(outputDir())
        }

        new DefaultGenerator()
                .opts(config.toClientOptInput())
                .generate()
    }
}
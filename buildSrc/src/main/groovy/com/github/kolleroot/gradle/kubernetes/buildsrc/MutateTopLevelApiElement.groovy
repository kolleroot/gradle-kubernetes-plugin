package com.github.kolleroot.gradle.kubernetes.buildsrc

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

/**
 * Mark all the top level api objects with the {@code TopLevelApiObject} interface
 */
class MutateTopLevelApiElement extends SourceTask {

    protected File destinationDir

    @Input
    Set<String> ignoreClasses = new HashSet<>()

    @OutputDirectory
    File getDestinationDir() {
        return destinationDir
    }

    void setDestinationDir(Object destinationDir) {
        this.destinationDir = project.file(destinationDir)
    }

    @TaskAction
    void processFiles() {
        getSource().visit { details ->
            if (details.isDirectory()) {
                return
            }

            if (ignoreClasses.contains(details.name) ||
                    !(details.file.text =~ /getApiVersion\(\)/) ||
                    !(details.file.text =~ /getKind\(\)/)) {
                details.copyTo(details.relativePath.getFile(destinationDir))
            } else {
                details.relativePath.getFile(destinationDir).withWriter { w ->
                    w << details.file.text.replaceAll(/(?<=public interface )([a-zA-Z1-9]+)\s(?=\{)/, /$1 extends TopLevelApiObject /)
                }
            }
        }
    }
}

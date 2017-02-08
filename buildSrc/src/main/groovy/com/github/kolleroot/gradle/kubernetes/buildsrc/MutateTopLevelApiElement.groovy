package com.github.kolleroot.gradle.kubernetes.buildsrc

import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

/**
 * Mark all the top level api objects with the {@code TopLevelApiObject} interface
 */
class MutateTopLevelApiElement extends SourceTask {

    @PathSensitive(PathSensitivity.NAME_ONLY)
    List<String> ignoreClasses = new ArrayList<>()

    @TaskAction
    void processFiles() {
        getSource().each { source ->
            if (ignoreClasses.contains(source.name)) {
                return
            }

            // get all objects containing an apiVersion and a kind
            if (!(source.text =~ /getApiVersion\(\)/) || !(source.text =~ /getKind\(\)/)) {
                return
            }

            String content = source.text
            source.withWriter { w ->
                w << content.replaceAll(/(?<=public interface )([a-zA-Z1-9]+)\s(?=\{)/, /$1 extends TopLevelApiObject /)
            }
        }
    }
}

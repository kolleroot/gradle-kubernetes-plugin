/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kolleroot.gradle.kubernetes.task

import com.github.kolleroot.gradle.kubernetes.model.api.TopLevelApiObject
import com.github.kolleroot.gradle.kubernetes.model.serializer.GradleGenson
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Generate json from a kubernetes object
 */
class KubernetesModelSerializerTask<T extends TopLevelApiObject> extends DefaultTask {
    protected T object
    protected boolean indent = true
    protected File jsonFile

    KubernetesModelSerializerTask() {
        outputs.upToDateWhen { false }
    }

    @Internal
    T getObject() {
        object
    }

    void setObject(T object) {
        this.object = object
    }

    @Input
    boolean isIndent() {
        indent
    }

    void setIndent(boolean indent) {
        this.indent = indent
    }

    @OutputFile
    File getJsonFile() {
        jsonFile
    }

    void setJsonFile(Object jsonFile) {
        this.jsonFile = project.file(jsonFile)
    }

    @TaskAction
    void generate() {
        GradleGenson genson = new GradleGenson(indent)
        jsonFile.withWriter { w ->
            genson.serialize(object, w)
        }
    }
}

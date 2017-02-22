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
package com.github.kolleroot.gradle.kubernetes.rule

import com.github.kolleroot.gradle.kubernetes.KubernetesPlugin
import org.gradle.api.Task
import org.gradle.model.Defaults
import org.gradle.model.ModelMap
import org.gradle.model.RuleSource

/**
 * This rule source creates all the default tasks required for the kubernetes plugin.
 */
class DefaultTasks extends RuleSource {
    @SuppressWarnings('GrMethodMayBeStatic')
    @Defaults
    void addDefaultDockerfileTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_DOCKERFILES_TASK, {
            group = KubernetesPlugin.DOCKER_GROUP
            description = 'Create all Dockerfiles for the images'
        }
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @Defaults
    void addDefaultBuildDockerImagesTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_DOCKER_BUILD_IMAGES_TASK, {
            group = KubernetesPlugin.DOCKER_GROUP
            description = 'Build all docker images'
        }
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @Defaults
    void addDefaultTagDockerImagesTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_DOCKER_TAG_IMAGES_TASK, {
            group = KubernetesPlugin.DOCKER_GROUP
            description = 'Tag all docker images'
        }
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @Defaults
    void addDefaultPushDockerImagesTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_DOCKER_PUSH_IMAGES_TASK, {
            group = KubernetesPlugin.DOCKER_GROUP
            description = 'Push all docker images'
        }
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @Defaults
    void addDefaultGenerateKubernetesObjectsTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_GENERATE_OBJECTS_TASK, {
            group = KubernetesPlugin.KUBERNETES_GROUP
            description = 'Generate all kubernetes objects'
        }
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @Defaults
    void addDefaultCreateKubernetesObjectsTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_CREATE_OBJECTS_TASK, {
            group = KubernetesPlugin.KUBERNETES_GROUP
            description = 'Create all kubernetes objects'
        }
    }
}

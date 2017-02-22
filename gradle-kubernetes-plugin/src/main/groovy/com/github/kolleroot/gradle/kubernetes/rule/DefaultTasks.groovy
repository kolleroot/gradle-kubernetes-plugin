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

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.bmuschko.gradle.docker.tasks.image.DockerTagImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.kolleroot.gradle.kubernetes.KubernetesPlugin
import com.github.kolleroot.gradle.kubernetes.task.KubernetesCreate
import com.github.kolleroot.gradle.kubernetes.task.KubernetesModelSerializerTask
import org.gradle.api.Task
import org.gradle.model.Defaults
import org.gradle.model.Finalize
import org.gradle.model.ModelMap
import org.gradle.model.RuleSource

/**
 * This rule source creates all the default tasks required for the kubernetes
 * plugin and all the dependencies for the default tasks.
 */
@SuppressWarnings(['GrMethodMayBeStatic', 'GroovyUnusedDeclaration'])
class DefaultTasks extends RuleSource {
    @Defaults
    void addDockerfilesTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_DOCKERFILES_TASK, {
            group = KubernetesPlugin.DOCKER_GROUP
            description = 'Create all Dockerfiles for the images'
        }
    }

    @Finalize
    void addDependenciesToDockerfilesTask(ModelMap<Task> tasks) {
        Task dockerfiles = tasks.get(KubernetesPlugin.KUBERNETES_DOCKERFILES_TASK)
        dockerfiles.dependsOn.addAll tasks.withType(Dockerfile)
    }

    @Defaults
    void addBuildDockerImagesTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_DOCKER_BUILD_IMAGES_TASK, {
            group = KubernetesPlugin.DOCKER_GROUP
            description = 'Build all docker images'
        }
    }

    @Finalize
    void addDependenciesToBuildDockerImagesTask(ModelMap<Task> tasks) {
        Task buildDockerFiles = tasks.get(KubernetesPlugin.KUBERNETES_DOCKER_BUILD_IMAGES_TASK)
        buildDockerFiles.dependsOn.addAll tasks.withType(DockerBuildImage)
    }

    @Defaults
    void addTagDockerImagesTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_DOCKER_TAG_IMAGES_TASK, {
            group = KubernetesPlugin.DOCKER_GROUP
            description = 'Tag all docker images'
        }
    }

    @Finalize
    void addDependenciesToTagDockerImagesTask(ModelMap<Task> tasks) {
        Task tagDockerImages = tasks.get(KubernetesPlugin.KUBERNETES_DOCKER_TAG_IMAGES_TASK)
        tagDockerImages.dependsOn.addAll tasks.withType(DockerTagImage)
    }

    @Defaults
    void addDefaultPushDockerImagesTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_DOCKER_PUSH_IMAGES_TASK, {
            group = KubernetesPlugin.DOCKER_GROUP
            description = 'Push all docker images'
        }
    }

    @Finalize
    void addDependenciesToPushDockerImagesTask(ModelMap<Task> tasks) {
        Task pushDockerImage = tasks.get(KubernetesPlugin.KUBERNETES_DOCKER_PUSH_IMAGES_TASK)
        pushDockerImage.dependsOn.addAll tasks.withType(DockerPushImage)
    }

    @Defaults
    void addDefaultGenerateKubernetesObjectsTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_GENERATE_OBJECTS_TASK, {
            group = KubernetesPlugin.KUBERNETES_GROUP
            description = 'Generate all kubernetes objects'
        }
    }

    @Finalize
    void addDependenciesToGenerateKubernetesObjectsTask(ModelMap<Task> tasks) {
        Task generateKubernetesObjects = tasks.get(KubernetesPlugin.KUBERNETES_GENERATE_OBJECTS_TASK)
        generateKubernetesObjects.dependsOn.addAll tasks.withType(KubernetesModelSerializerTask)
    }

    @Defaults
    void addCreateKubernetesObjectsTask(ModelMap<Task> tasks) {
        tasks.create KubernetesPlugin.KUBERNETES_CREATE_OBJECTS_TASK, {
            group = KubernetesPlugin.KUBERNETES_GROUP
            description = 'Create all kubernetes objects'
        }
    }

    @Finalize
    void addDependenciesToCreateKubernetesObjectsTask(ModelMap<Task> tasks) {
        Task createKubernetesObjects = tasks.get(KubernetesPlugin.KUBERNETES_CREATE_OBJECTS_TASK)
        createKubernetesObjects.dependsOn.addAll tasks.withType(KubernetesCreate)
    }
}

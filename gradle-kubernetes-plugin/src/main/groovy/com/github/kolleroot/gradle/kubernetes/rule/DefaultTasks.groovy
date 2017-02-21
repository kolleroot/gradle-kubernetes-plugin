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

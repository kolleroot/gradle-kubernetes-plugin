package com.github.kolleroot.gradle.kubernetes.helper

import com.github.kolleroot.gradle.kubernetes.model.DockerImage
import com.github.kolleroot.gradle.kubernetes.model.FileBundle
import com.github.kolleroot.gradle.kubernetes.model.api.TopLevelApiObject
import com.github.kolleroot.gradle.kubernetes.model.internal.DockerRegistryTaskName

/**
 * Some helper functions to get the task names
 */
final class TaskNameHelper {
    public static final String KUBERNETES_DOCKERFILE_BASE = 'dockerfile'
    public static final String KUBERNETES_DOCKER_BUILD_IMAGE_BASE = 'buildDockerImage'
    public static final String KUBERNETES_DOCKER_TAG_BASE = 'tagDockerImage'
    public static final String KUBERNETES_DOCKER_PUSH_BASE = 'pushDockerImage'
    public static final String KUBERNETES_OBJECT_GENERATE_BASE = 'generateKubernetesObject'
    public static final String KUBERNETES_CREATE_BASE = 'createKubernetesObject'

    private TaskNameHelper() {
    }

    static String getGenerateTaskName(TopLevelApiObject apiObject) {
        KUBERNETES_OBJECT_GENERATE_BASE + apiObject.name.capitalize()
    }

    static String getCreateTaskName(TopLevelApiObject apiObject) {
        KUBERNETES_CREATE_BASE + apiObject.name.capitalize()
    }

    static String getDockerfileTaskName(DockerImage dockerImage) {
        KUBERNETES_DOCKERFILE_BASE + dockerImage.name.capitalize()
    }

    static String getDockerfileZipTaskName(DockerImage dockerImage, FileBundle bundle) {
        KUBERNETES_DOCKERFILE_BASE + dockerImage.name.capitalize() +
                "${bundle.bundleName.split(/\./)[0].replace('-', '').capitalize()}"
    }

    static String getBuildTaskName(DockerImage dockerImage) {
        KUBERNETES_DOCKER_BUILD_IMAGE_BASE + dockerImage.name.capitalize()
    }

    static String getTagTaskName(DockerRegistryTaskName dockerRegistry, DockerImage dockerImage) {
        KUBERNETES_DOCKER_TAG_BASE +
                dockerRegistry.name.capitalize() +
                dockerImage.name.capitalize()
    }

    static String getPushTaskName(DockerRegistryTaskName dockerRegistry, DockerImage dockerImage) {
        KUBERNETES_DOCKER_PUSH_BASE +
                dockerRegistry.name.capitalize() +
                dockerImage.name.capitalize()
    }
}

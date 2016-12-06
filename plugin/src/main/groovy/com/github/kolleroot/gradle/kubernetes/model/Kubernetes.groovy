package com.github.kolleroot.gradle.kubernetes.model

import org.gradle.model.Managed

/**
 * The specifications for the kubernetes model
 */
@Managed
interface Kubernetes {
    DockerImageContainer getDockerImages()
}

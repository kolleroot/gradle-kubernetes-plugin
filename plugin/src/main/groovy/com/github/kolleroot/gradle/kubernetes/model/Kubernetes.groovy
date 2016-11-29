package com.github.kolleroot.gradle.kubernetes.model

import org.gradle.api.Action
import org.gradle.api.plugins.DeferredConfigurable
import org.gradle.internal.reflect.Instantiator

/**
 * The specifications for the kubernetes model
 */
interface Kubernetes {

    DockerImageContainer getDockerImages()

    void dockerImages(Action<? extends DockerImageContainer> configure)
}

@DeferredConfigurable
class DefaultKubernetes implements Kubernetes {
    private final DockerImageContainer dockerImages

    DefaultKubernetes(Instantiator instantiator) {
        dockerImages = instantiator.newInstance(DefaultDockerImageContainer, instantiator)
    }

    @Override
    DockerImageContainer getDockerImages() {
        return dockerImages
    }

    @Override
    void dockerImages(Action<? extends DockerImageContainer> configure) {
        configure.execute(dockerImages)
    }
}

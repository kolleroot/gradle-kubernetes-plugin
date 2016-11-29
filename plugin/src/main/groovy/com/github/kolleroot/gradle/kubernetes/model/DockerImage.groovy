package com.github.kolleroot.gradle.kubernetes.model

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.internal.reflect.Instantiator

interface DockerImage extends Named {}

abstract class DockerImageFactory<T extends DockerImage> implements NamedDomainObjectFactory<T> {
    private final Instantiator instantiator
    private final Class<T> clazz

    DockerImageFactory(Class<T> clazz, Instantiator instantiator) {
        this.clazz = clazz
        this.instantiator = instantiator
    }

    @Override
    T create(String name) {
        instantiator.newInstance(clazz, name)
    }
}

class DefaultDockerImage implements DockerImage {
    private final String name

    DefaultDockerImage(String name) {
        this.name = name
    }

    @Override
    String getName() {
        name
    }
}

class DefaultDockerImageFactory extends DockerImageFactory<DefaultDockerImage> {
    DefaultDockerImageFactory(Instantiator instantiator) {
        super(DefaultDockerImage, instantiator)
    }
}


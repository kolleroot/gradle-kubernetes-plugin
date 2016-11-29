package com.github.kolleroot.gradle.kubernetes.model

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer
import org.gradle.internal.reflect.Instantiator

interface DockerImageContainer extends ExtensiblePolymorphicDomainObjectContainer<DockerImage> {}

class DefaultDockerImageContainer extends DefaultPolymorphicDomainObjectContainer<DockerImage> implements
        DockerImageContainer {
    DefaultDockerImageContainer(Instantiator instantiator) {
        super(DockerImage.class, instantiator)

        registerFactory DefaultDockerImage, new DefaultDockerImageFactory(instantiator)
    }
}

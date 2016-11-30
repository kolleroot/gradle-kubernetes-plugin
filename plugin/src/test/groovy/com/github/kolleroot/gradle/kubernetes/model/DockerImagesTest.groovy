package com.github.kolleroot.gradle.kubernetes.model

import org.gradle.internal.reflect.DirectInstantiator
import spock.lang.Specification

/**
 * Test the docker images model
 */
class DockerImagesTest extends Specification {
    Kubernetes kubernetes

    def setup() {
        kubernetes = new DefaultKubernetes(DirectInstantiator.INSTANCE)
    }

    def "create DefaultDockerImage instance"() {
        when:
        def testImage = kubernetes.dockerImages.create 'testImage', DefaultDockerImage

        then:
        testImage != null
        testImage instanceof DefaultDockerImage
        testImage.name == 'testImage'
    }
}

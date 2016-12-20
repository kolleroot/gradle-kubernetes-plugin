package com.github.kolleroot.gradle.kubernetes.func

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.kolleroot.gradle.kubernetes.testbase.GradleSpecification
import spock.lang.Shared

/**
 * Created by stefan on 19.12.16.
 */
class CreateDockerImageSpec extends GradleSpecification {

    @Shared
    DockerClient dockerClient

    def setupSpec() {
        def config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost('tcp://localhost:2375')
                .build()

        dockerClient = DockerClientBuilder.getInstance(config)
                .build()
    }

    def cleanupSpec() {
        dockerClient.close()
    }

    def setup() {
        assert dockerClient != null
        dockerClient.pingCmd().exec()
    }

    def cleanup() {
        dockerClient.listContainersCmd().exec().each { container ->
            dockerClient.removeContainerCmd(container.id)
        }

        dockerClient.listImagesCmd().exec().each { image ->
            dockerClient.removeImageCmd(image.id)
        }
    }

    def "build a simple docker image"() {
        given: 'a Dockerfile'
        buildFile << """
        import com.github.kolleroot.gradle.kubernetes.model.DefaultDockerImage

        plugins {
            id 'com.github.kolleroot.gradle.kubernetes'
        }

        repositories {
            jcenter()
        }

        docker {
            url = "http://localhost:2375"
        }

        model {
            kubernetes {
                dockerImages {
                    simple(DefaultDockerImage) {
                        from 'openjdk:8-jdk-alpine'
                    }
                }
            }
        }
        """.stripIndent().trim()

        when: 'the docker build task ran'
        succeeds 'kubernetesDockerBuildImageSimple'

        then: 'the host has a docker image with tag simple:latest'
        dockerClient.listImagesCmd().exec().any { image ->
            image.repoTags.contains 'simple:latest'
        }
    }
}

package com.github.kolleroot.gradle.kubernetes.integ

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.kolleroot.gradle.kubernetes.testbase.DockerHelper
import com.github.kolleroot.gradle.kubernetes.testbase.GradleSpecification
import spock.lang.Shared

/**
 * Specify the docker part
 *
 * Everyone must do his own cleanup
 */
class CreateDockerImageSpec extends GradleSpecification {

    static final String IMAGE_NAME = 'simple'
    static final String IMAGE_ID = IMAGE_NAME + ':latest'

    @Shared
    DockerClient dockerClient

    def setupSpec() {
        def config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(DockerHelper.url)
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
            url = "$DockerHelper.url"
        }

        model {
            kubernetes {
                dockerImages {
                    $IMAGE_NAME(DefaultDockerImage) {
                        from 'openjdk:8-jdk-alpine'

                        maintainer 'Stefan Kollmann <kolle.root@yahoo.de>'
                        defaultCommand 'java -version'

                        exposePort 80
                    }
                }
            }
        }
        """.stripIndent().trim()

        when: 'the docker build task ran'
        succeeds 'kubernetesDockerBuildImageSimple'

        and: 'got image details'
        def imageDetails = dockerClient.inspectImageCmd('simple:latest').exec()

        then: 'the host has a docker image with tag simple:latest'
        imageDetails != null
        imageDetails.repoTags.contains IMAGE_ID

        imageDetails.author == 'Stefan Kollmann <kolle.root@yahoo.de>'
        imageDetails.config.cmd.join(' ') == 'java -version'
        imageDetails.config.exposedPorts as List == [new ExposedPort(80)]

        cleanup: 'delete the created image'
        dockerClient.removeImageCmd(IMAGE_ID).exec()
    }
}

package com.github.kolleroot.gradle.kubernetes.testbase

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.InspectImageResponse
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import org.junit.After
import org.junit.Before

/**
 * Setup a docker client for testing
 */
trait DockerTrait {

    DockerClient dockerClient

    @Before
    def dockerSetup() {
        def config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(DockerHelper.url)
                .build()

        dockerClient = DockerClientBuilder.getInstance(config)
                .build()

        assert dockerClient != null
        dockerClient.pingCmd().exec()
    }

    @After
    def dockerCleanup() {
        dockerClient.close()
    }

    void removeImage(String name) {
        dockerClient.removeImageCmd(name).exec()
    }

    InspectImageResponse inspectImage(String name) {
        dockerClient.inspectImageCmd(name).exec()
    }
}

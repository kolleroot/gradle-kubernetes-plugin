/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kolleroot.gradle.kubernetes.testbase

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.InspectImageResponse
import com.github.dockerjava.api.exception.NotFoundException
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

    void removeImageIfExists(String name) {
        try {
            dockerClient.removeImageCmd(name).exec()
        } catch(NotFoundException e) {

        }
    }

    InspectImageResponse inspectImage(String name) {
        dockerClient.inspectImageCmd(name).exec()
    }
}

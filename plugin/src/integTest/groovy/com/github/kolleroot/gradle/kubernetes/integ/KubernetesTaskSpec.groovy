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
package com.github.kolleroot.gradle.kubernetes.integ

import com.github.kolleroot.gradle.kubernetes.testbase.GradleTrait
import com.github.kolleroot.gradle.kubernetes.testbase.KubernetesTrait
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.Watcher
import org.apache.commons.lang3.StringUtils
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Specify the kubernetes tasks and test the behavior against the default kubernetes cluster
 *
 * This test will create a new namespace for testing and clean it up afterwards.
 */
class KubernetesTaskSpec extends Specification implements KubernetesTrait, GradleTrait {

    static final String TEST_POD = 'test-pod'
    static final String TEST_POD_FILE = 'test-pod.yaml'

    def "create a simple pod from a yaml file"() {
        given: 'a config file'
        buildFolder.newFile(TEST_POD_FILE) << testPodYaml

        and: 'a basic gradle project'
        buildFile << """
        import com.github.kolleroot.gradle.kubernetes.task.KubernetesCreate

        plugins {
            id 'com.github.kolleroot.gradle.kubernetes'
        }

        repositories {
            jcenter()
        }

        task createPod(type: KubernetesCreate) {
            configFile = file('$TEST_POD_FILE')
        }
        """.stripIndent().trim()

        when: 'gradle runs the task'
        succeeds 'createPod'

        then: 'a pod will be created'
        def pod = kubernetesClient.pods().inNamespace(namespace).withName(TEST_POD).get()
        pod != null
        pod.metadata.name == TEST_POD
        pod.metadata.labels == [state: 'test', app: 'simple']
        pod.spec.containers.size() == 1
        pod.spec.containers.get(0).name == 'a-container'
        pod.spec.containers.get(0).image == 'openjdk:8-jdk-alpine'
    }

    def "delete a simple pod from a yaml file"() {
        given: 'a config file'
        def testPodFile = buildFolder.newFile(TEST_POD_FILE) << testPodYaml

        and: 'a pod on the cluster'
        kubernetesClient.load(testPodFile.newInputStream()).createOrReplace()

        and: 'a watcher on the pod listening for the delete event'
        final DELETE_LATCH = new CountDownLatch(1)

        kubernetesClient.pods().inNamespace(namespace).withName(TEST_POD).watch(new Watcher<Pod>() {
            @Override
            void eventReceived(Watcher.Action action, Pod resource) {
                if (action == Watcher.Action.DELETED) {
                    DELETE_LATCH.countDown()
                }
            }

            @Override
            void onClose(KubernetesClientException cause) {

            }
        })

        and: 'a basic gradle project deleting the pod'
        buildFile << """
        import com.github.kolleroot.gradle.kubernetes.task.KubernetesDelete

        plugins {
            id 'com.github.kolleroot.gradle.kubernetes'
        }

        repositories {
            jcenter()
        }

        task deletePod(type: KubernetesDelete) {
            configFile = file('$TEST_POD_FILE')
        }
        """.stripIndent().trim()

        when: 'gradle runs the delete task'
        succeeds 'deletePod'

        then: 'wait till the pod is deleted and verify that it is realy deleted'
        DELETE_LATCH.await(10, TimeUnit.SECONDS)

        def pod = kubernetesClient.pods().inNamespace(namespace).withName(TEST_POD).get()
        pod == null
    }

    @SuppressWarnings(['DuplicateStringLiteral', 'DuplicateNumberLiteral', 'MethodSize'])
    def "forward a port from the kubernetes cluster to localhost"() {
        given: 'a pod in the kubernetes cluster'
        while (true) {
            try {
                // @formatter:off
                kubernetesClient.pods().withName(namespace).createNew()
                        .withNewMetadata()
                            .withName('echo-server')
                            .withNamespace(namespace)
                        .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName('simple-echo')
                                .withImage('gcr.io/google-containers/busybox')
                                .withCommand('nc', '-p', '8080', '-l', '-l', '-e', 'echo', 'hello world!\n')
                                .withImagePullPolicy('Always')
                                .addNewPort()
                                    .withContainerPort(8080)
                                    .withProtocol('TCP')
                                    .withName('echo')
                                .endPort()
                            .endContainer()
                        .endSpec()
                        .done()
                break
                // @formatter:on
            } catch (KubernetesClientException e) {
                if (!(e.status?.details?.retryAfterSeconds > 0)) {
                    throw new IllegalStateException('Unable to create simple-echo pod')
                }

                Thread.sleep(e.status.details.retryAfterSeconds * 1000L)
            }
        }
        and: 'a gradle file opening and closing the port'
        buildFile << """
        import com.github.kolleroot.gradle.kubernetes.task.KubernetesOpenPortForwardTask
        import com.github.kolleroot.gradle.kubernetes.task.KubernetesClosePortForwardTask

        plugins {
            id 'com.github.kolleroot.gradle.kubernetes'
        }

        repositories {
            jcenter()
        }

        task open(type: KubernetesOpenPortForwardTask) {
            forwardNamespace = '$namespace'
            forwardPod = 'echo-server'
            forwardPort = '8080:8080'
        }

        task close(type: KubernetesClosePortForwardTask) {
            forwardId = open.id
        }

        open.finalizedBy close

        task remoteReadFast(dependsOn: open) {
            doLast {
                def s = new Socket('localhost', 8080)
                s.withStreams {input, output ->
                    println input.newReader().readLine()
                    println 'read response fast'
                }
            }
            finalizedBy close
        }

        task remoteReadSlow(dependsOn: open) {
            doLast {
                def s = new Socket('localhost', 8080)
                s.withStreams {input, output ->
                    println input.newReader().readLine()
                    println 'read response slow first'
                }

                // some very expensive work
                Thread.sleep(2 * 1000L)

                s = new Socket('localhost', 8080)
                s.withStreams {input, output ->
                    println input.newReader().readLine()
                    println 'read response slow last'
                }
            }
            finalizedBy close
        }

        task remoteRead(dependsOn: [remoteReadFast, remoteReadSlow]) {
        }
        """.stripIndent().trim()

        when: 'grandle runs the task'
        succeeds 'remoteRead'

        then: 'all tasks succeed'
        buildResult.task(':open').outcome == TaskOutcome.SUCCESS
        buildResult.task(':remoteReadSlow').outcome == TaskOutcome.SUCCESS
        buildResult.task(':remoteReadFast').outcome == TaskOutcome.SUCCESS
        buildResult.task(':close').outcome == TaskOutcome.SUCCESS

        and: 'the response from the server will be in the log output'
        StringUtils.countMatches(buildResult.output, 'hello world!') == 3
        buildResult.output.contains 'read response fast'
        buildResult.output.contains 'read response slow first'
        buildResult.output.contains 'read response slow last'
    }

    private String getTestPodYaml() {
        """
        apiVersion: v1
        kind: Pod
        metadata:
            name: $TEST_POD
            namespace: $namespace
            labels:
                state: test
                app: simple
        spec:
            containers:
              - name: a-container
                image: openjdk:8-jdk-alpine
                command:
                  - "sh"
                  - "-c \\"java -version; while true; do echo -n '. '; sleep 1; done\\""
        """.stripIndent().trim()
    }
}

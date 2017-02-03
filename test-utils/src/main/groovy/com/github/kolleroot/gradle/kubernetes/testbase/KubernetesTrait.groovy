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

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.ContainerBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import org.junit.After
import org.junit.Before

import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Setup the kubernetes test environment.
 *
 * Create a new, randomly named namespace
 */
trait KubernetesTrait {
    private static final String REGISTRY_DATA_PATH = '/var/lib/registry'

    static final Random RANDOM = new SecureRandom()

    KubernetesClient kubernetesClient

    String namespace = "kubernetes-tasks-test-namespace-${RANDOM.nextLong()}"

    @Before
    def kubernetesSetup() {
        kubernetesClient = new DefaultKubernetesClient()
        kubernetesClient.namespaces().createNew()
                .withNewMetadata()
                .withName(namespace)
                .endMetadata()
                .done()
    }

    @After
    def kubernetesCleanup() {
        kubernetesClient.namespaces().withName(namespace).delete()

        kubernetesClient.close()
    }

    boolean waitTillPodIsReady(String namespace, String podName, long time, TimeUnit unit) {
        def latch = new CountDownLatch(1)
        def executor = Executors.newSingleThreadExecutor()

        executor.execute(new PodPoller(kubernetesClient, namespace, podName, latch))

        try {
            latch.await(time, unit)
        } finally {
            executor.shutdown()
        }
    }

    Container getRegistryContainer(String name, int port) {
        // @formatter:off
        new ContainerBuilder()
                .withName(name)
                .withImage('registry:2')
                .addNewPort()
                    .withName('registry')
                    .withProtocol('TCP')
                    .withContainerPort(port)
                .endPort()
                .addNewEnv()
                    .withName('REGISTRY_HTTP_ADDR')
                    .withValue(":${port}")
                .endEnv()
                .addNewEnv()
                    .withName('REGISTRY_STORAGE_FILESYSTEM_ROOTDIRECTORY')
                    .withValue(REGISTRY_DATA_PATH)
                .endEnv()
                .addNewVolumeMount()
                    .withName('cache-volume')
                    .withMountPath(REGISTRY_DATA_PATH)
                .endVolumeMount()
            .build()
        // @formatter:on
    }

    private static final class PodPoller implements Runnable {

        private final KubernetesClient kubernetesClient
        private final String namespace
        private final String podName
        private final CountDownLatch latch

        PodPoller(KubernetesClient kubernetesClient, String namespace, String podName, CountDownLatch latch) {
            this.kubernetesClient = kubernetesClient
            this.namespace = namespace
            this.podName = podName
            this.latch = latch
        }

        @Override
        void run() {
            boolean ready = false
            while (!ready) {
                def status = kubernetesClient.pods().inNamespace(namespace).withName(podName).get().status

                status.conditions.each { condition ->
                    if (condition.type == 'Ready' && condition.status == 'True') {
                        ready = true
                    }
                }
            }

            latch.countDown()
        }
    }
}

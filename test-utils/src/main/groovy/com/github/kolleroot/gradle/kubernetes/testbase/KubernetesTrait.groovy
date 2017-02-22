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
import io.fabric8.kubernetes.api.model.DoneableServiceAccount
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Namespace
import io.fabric8.kubernetes.api.model.NamespaceBuilder
import io.fabric8.kubernetes.api.model.ServiceAccount
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.Watch
import io.fabric8.kubernetes.client.Watcher
import io.fabric8.kubernetes.client.dsl.Resource
import org.junit.After
import org.junit.Before

import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
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
        Namespace ns = new NamespaceBuilder()
                .withNewMetadata()
                .withName(namespace)
                .endMetadata()
                .build()

        createAndWaitTillReady(ns, 30, TimeUnit.SECONDS)

        CountDownLatch serviceAccountReady = new CountDownLatch(1)

        Resource<ServiceAccount, DoneableServiceAccount> sacr = kubernetesClient.serviceAccounts().inNamespace(namespace).withName('default')

        Watch watch = sacr.watch(new ServiceAccountReadinessWatcher(serviceAccountReady))

        if (!sacr.get()?.secrets?.empty) {
            serviceAccountReady.countDown()
        }

        serviceAccountReady.await(30, TimeUnit.SECONDS)
        watch.close()
    }

    @After
    def kubernetesCleanup() {
        kubernetesClient.namespaces().withName(namespace).delete()

        kubernetesClient.close()
    }

    void createAndWaitTillReady(HasMetadata hasMetadata, long time, TimeUnit unit) {
        kubernetesClient.resource(hasMetadata).createOrReplaceAnd().waitUntilReady(time, unit)
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

    static class ServiceAccountReadinessWatcher implements Watcher<ServiceAccount> {

        private final CountDownLatch latch

        ServiceAccountReadinessWatcher(final CountDownLatch latch) {
            this.latch = latch
        }

        @Override
        void eventReceived(Watcher.Action action, ServiceAccount resource) {
            if (!resource?.secrets?.empty) {
                latch.countDown()
            }
        }

        @Override
        void onClose(KubernetesClientException cause) {

        }
    }
}

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
package com.github.kolleroot.gradle.kubernetes.standalone

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StandaloneTest {
    static final Logger logger = LoggerFactory.getLogger(StandaloneTest.class)

    /**
     * Starts a simple test using the fabric8 kubernetes library
     * @param args
     */
    static void main(String[] args) {
        KubernetesClient client = new DefaultKubernetesClient();

        // print the config, just to be sure
        logger.info client.configuration.masterUrl
        logger.info "Accessible: " + isClusterAccessible(client)

        client.namespaces().list().items.each {
            logger.info it.metadata.name
        }

        client.pods().inNamespace("kube-system").list().items.each {
            logger.info it.metadata.name
            it.metadata.labels.each {
                logger.info "\t" + it.key + ": " + it.value
            }
        }

        // client.namespaces().createNew().withNewMetadata().withName('asdf').endMetadata().done()
        // client.namespaces().withName('asdf').delete()
    }

    static boolean isClusterAccessible(KubernetesClient client) {
        return client.nodes().list().items.size() > 0
    }
}

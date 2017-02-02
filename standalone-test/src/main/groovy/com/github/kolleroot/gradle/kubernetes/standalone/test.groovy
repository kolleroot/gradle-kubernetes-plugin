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

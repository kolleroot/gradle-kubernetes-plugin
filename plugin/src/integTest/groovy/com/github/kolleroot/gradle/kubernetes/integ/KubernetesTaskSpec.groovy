package com.github.kolleroot.gradle.kubernetes.integ

import com.github.kolleroot.gradle.kubernetes.testbase.GradleSpecification
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared

/**
 * Specify the kubernetes tasks and test the behavior against the default kubernetes cluster
 *
 * This test will create a new namespace for testing and clean it up afterwards.
 */
class KubernetesTaskSpec extends GradleSpecification {
    static final String NAMESPACE = 'kubernetes-tasks-test-namespace'

    static final String TEST_POD = 'test-pod'

    @Rule
    TemporaryFolder tmpFolder = new TemporaryFolder()

    @Shared
    KubernetesClient kubernetesClient

    def setupSpec() {
        kubernetesClient = new DefaultKubernetesClient()
    }

    def setup() {
        kubernetesClient.namespaces().createNew()
                .withNewMetadata()
                .withName(NAMESPACE)
                .endMetadata()
                .done()
    }

    def cleanup() {
        kubernetesClient.namespaces().withName(NAMESPACE).delete()
    }

    def cleanupSpec() {
        kubernetesClient.close()
    }

    def "create a simple pod from a yaml file"() {
        given: 'a config file'
        buildFolder.newFile('test-pod.yaml') << """
        apiVersion: v1
        kind: Pod
        metadata:
            name: $TEST_POD
            namespace: $NAMESPACE
            labels:
                state: test
                app: simple
        spec:
            containers:
              - name: a-container
                image: openjdk:8-jdk-alpine
                command:
                  - "java -version"
        """.stripIndent().trim()

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
            configFile = file('test-pod.yaml')
        }
        """.stripIndent().trim()

        when: 'gradle runs the task'
        succeeds 'createPod'

        then: 'a pod will be created'
        def pod = kubernetesClient.pods().inNamespace(NAMESPACE).withName(TEST_POD).get()
        pod != null
        pod.metadata.name == TEST_POD
        pod.metadata.labels == [state: 'test', app: 'simple']
        pod.spec.containers.size() == 1
        pod.spec.containers.get(0).name == 'a-container'
        pod.spec.containers.get(0).image == 'openjdk:8-jdk-alpine'
    }
}

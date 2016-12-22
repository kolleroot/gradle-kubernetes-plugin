package com.github.kolleroot.gradle.kubernetes.integ

import com.github.kolleroot.gradle.kubernetes.testbase.GradleSpecification
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.Watcher
import spock.lang.Shared

import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Specify the kubernetes tasks and test the behavior against the default kubernetes cluster
 *
 * This test will create a new namespace for testing and clean it up afterwards.
 */
class KubernetesTaskSpec extends GradleSpecification {

    static final String TEST_POD = 'test-pod'
    static final String TEST_POD_FILE = 'test-pod.yaml'

    static final Random RANDOM = new SecureRandom()

    @Shared
    KubernetesClient kubernetesClient

    String namespace = "kubernetes-tasks-test-namespace-${RANDOM.nextLong()}"

    def setupSpec() {
        kubernetesClient = new DefaultKubernetesClient()
    }

    def setup() {
        kubernetesClient.namespaces().createNew()
                .withNewMetadata()
                .withName(namespace)
                .endMetadata()
                .done()
    }

    def cleanup() {
        kubernetesClient.namespaces().withName(namespace).delete()
    }

    def cleanupSpec() {
        kubernetesClient.close()
    }

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

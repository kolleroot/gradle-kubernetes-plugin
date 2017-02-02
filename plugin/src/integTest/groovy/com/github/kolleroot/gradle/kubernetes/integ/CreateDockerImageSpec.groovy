package com.github.kolleroot.gradle.kubernetes.integ

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.core.command.PullImageResultCallback
import com.github.kolleroot.gradle.kubernetes.task.KubectlPortForwarder
import com.github.kolleroot.gradle.kubernetes.testbase.DockerHelper
import com.github.kolleroot.gradle.kubernetes.testbase.DockerTrait
import com.github.kolleroot.gradle.kubernetes.testbase.GradleTrait
import com.github.kolleroot.gradle.kubernetes.testbase.KubernetesTrait
import spock.lang.Specification

import java.util.concurrent.TimeUnit

/**
 * Specify the docker part
 *
 * Everyone must do his own cleanup
 */
class CreateDockerImageSpec extends Specification implements GradleTrait, DockerTrait, KubernetesTrait {

    static final String IMAGE_NAME = 'simple'
    static final String IMAGE_ID = IMAGE_NAME + ':latest'

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
        def imageDetails = inspectImage 'simple:latest'

        then: 'the host has a docker image with tag simple:latest'
        imageDetails != null
        imageDetails.repoTags.contains IMAGE_ID

        imageDetails.author == 'Stefan Kollmann <kolle.root@yahoo.de>'
        imageDetails.config.cmd.join(' ') == 'java -version'
        imageDetails.config.exposedPorts as List == [new ExposedPort(80)]

        cleanup: 'delete the created image'
        dockerClient.removeImageCmd(IMAGE_ID).exec()
    }

    @SuppressWarnings(['DuplicateStringLiteral'])
    def 'build, tag and push a docker image to a repository in the cluster'() {
        given: 'a registry in the cluster'
        // @formatter:off
        kubernetesClient.pods()
                .createNew()
                    .withNewMetadata()
                        .withName('registry')
                        .withNamespace(namespace)
                    .endMetadata()
                    .withNewSpec()
                        .addNewContainerLike(getRegistryContainer('registry', 5050)).endContainer()
                        .addNewVolume()
                            .withName('cache-volume')
                            .withNewEmptyDir()
                            .endEmptyDir()
                        .endVolume()
                    .endSpec()
                .done()
        // @formatter:on

        and: 'the registry gets ready'
        waitTillPodIsReady(namespace, 'registry', 30, TimeUnit.SECONDS)

        and: 'a gradle file with a model'
        buildFile << """
        import com.github.kolleroot.gradle.kubernetes.model.DefaultDockerImage
        import com.github.kolleroot.gradle.kubernetes.model.KubernetesLocalDockerRegistry

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
                    }
                }
                dockerRegistries {
                    'localhost:5050'(KubernetesLocalDockerRegistry) {
                        namespace = '$namespace'
                        pod = 'registry'
                        port = '5050:5050'
                    }
                }
            }
        }
        """.stripIndent().trim()

        when: 'the gradle build succeeded'
        succeeds 'kubernetesDockerPushLocalhost5050Simple'

        and: 'the image id is available'
        def imageId = inspectImage('simple:latest').id

        and: 'the local files are removed'
        removeImage 'simple:latest'
        removeImage 'localhost:5050/simple:latest'

        and: 'there is a image in the repository'
        new KubectlPortForwarder(namespace, 'registry', '5050').withCloseable {
            dockerClient.pullImageCmd('localhost:5050/simple:latest').exec(new PullImageResultCallback()).awaitSuccess()
        }

        then:
        inspectImage('localhost:5050/simple:latest').id == imageId

        cleanup: 'the local images'
        // removeImage 'simple:latest' // the image is already gone
        removeImage 'localhost:5050/simple:latest'
    }
}

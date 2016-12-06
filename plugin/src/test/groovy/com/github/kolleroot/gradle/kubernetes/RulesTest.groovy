package com.github.kolleroot.gradle.kubernetes

import com.github.kolleroot.gradle.kubernetes.model.DefaultDockerImage
import com.github.kolleroot.gradle.kubernetes.model.DockerImage
import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Test the KubernetesPlugin rule source
 */
class RulesTest extends Specification {

    Project project

    void setup() {
        project = ProjectBuilder.builder().build()
    }

    private Kubernetes kubernetesFromModel() {
        project.modelRegistry.find('kubernetes', Kubernetes)
    }

    def 'no default docker images'() {
        given: 'the default model'
        project.allprojects {
            apply plugin: KubernetesPlugin
        }

        when:
        def kubernetes = kubernetesFromModel()

        then:
        kubernetes.dockerImages.size() == 0
    }

    def 'a simple docker image'() {
        given:
        project.allprojects {
            apply plugin: KubernetesPlugin

            model {
                kubernetes {
                    dockerImages {
                        simpleImage(DefaultDockerImage)
                    }
                }
            }
        }
        project
        when:
        def kubernetes = kubernetesFromModel()
        def dockerImage = kubernetes.dockerImages['simpleImage']

        then:
        kubernetes.dockerImages.size() == 1
        dockerImage != null
    }

    def 'docker image with basic instructions'() {
        given:
        project.allprojects {
            apply plugin: KubernetesPlugin

            model {
                kubernetes {
                    dockerImages {
                        simpleImage(DefaultDockerImage) {
                            from 'openjdk'
                            maintainer 'Stefan Kollmann <kolle.root@yahoo.de>'
                            runCommand 'cp a.class b.class'
                            defaultCommand 'java a'
                        }
                    }
                }
            }
        }
        project
        when:
        def kubernetes = kubernetesFromModel()
        DockerImage dockerImage = kubernetes.dockerImages['simpleImage']

        then:
        kubernetes.dockerImages.size() == 1
        dockerImage != null

        dockerImage.instructions[0] == 'FROM openjdk'
        dockerImage.instructions[1] == 'MAINTAINER Stefan Kollmann <kolle.root@yahoo.de>'
        dockerImage.instructions[2] == 'RUN cp a.class b.class'
        dockerImage.instructions[3] == 'CMD ["java a"]'
    }
}

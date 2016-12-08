package com.github.kolleroot.gradle.kubernetes

import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.kolleroot.gradle.kubernetes.model.DefaultDockerImage
import com.github.kolleroot.gradle.kubernetes.model.DockerImage
import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.model.internal.core.ModelRuleExecutionException
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

    private TaskContainer tasksFromModel() {
        project.modelRegistry.find('tasks', TaskContainer)
    }

    def 'docker image no default'() {
        given: 'the default model'
        project.allprojects {
            apply plugin: KubernetesPlugin
        }

        when:
        def kubernetes = kubernetesFromModel()

        then:
        kubernetes.dockerImages.size() == 0
    }

    def 'docker image simpleImage'() {
        given:
        project.allprojects {
            apply plugin: KubernetesPlugin

            model {
                kubernetes {
                    dockerImages {
                        simpleImage(DefaultDockerImage) {
                            from 'nothing'
                        }
                    }
                }
            }
        }

        when:
        def kubernetes = kubernetesFromModel()
        def dockerImage = kubernetes.dockerImages['simpleImage']

        then:
        kubernetes.dockerImages.size() == 1
        dockerImage != null
    }

    def 'docker image with basic instructions (most of the instructions)'() {
        given:
        project.allprojects {
            apply plugin: KubernetesPlugin

            model {
                kubernetes {
                    dockerImages {
                        simpleImage(DefaultDockerImage) {
                            from 'openjdk'
                            maintainer 'Stefan Kollmann <kolle.root@yahoo.de>'
                            user 'admin'
                            workingDir '/root'
                            runCommand 'cp a.class b.class'
                            entryPoint 'java', 'a'
                            defaultCommand '--my-flag'
                            exposePort 80, 443, 8080, 9990
                            environmentVariable 'MY_PARAM', 'abc123'
                            volume '/var/lib/myApp'
                        }
                    }
                }
            }
        }

        when:
        def kubernetes = kubernetesFromModel()
        DockerImage dockerImage = kubernetes.dockerImages['simpleImage']

        then:
        kubernetes.dockerImages.size() == 1
        dockerImage != null

        dockerImage.instructions.join('\n') == """
            FROM openjdk
            MAINTAINER Stefan Kollmann <kolle.root@yahoo.de>
            USER admin
            WORKDIR /root
            RUN cp a.class b.class
            ENTRYPOINT ["java", "a"]
            CMD ["--my-flag"]
            EXPOSE 80 443 8080 9990
            ENV MY_PARAM abc123
            VOLUME ["/var/lib/myApp"]
""".stripIndent().trim()
    }

    def 'docker image verify not empty instruction'() {
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

        when:
        kubernetesFromModel()

        then:
        ModelRuleExecutionException e = thrown()

        e.cause.message.contains 'The list of instructions MUST not be empty. ' +
                'There must be at least one FROM instruction.'
    }

    def 'docker image verify FROM first'() {
        given:
        project.allprojects {
            apply plugin: KubernetesPlugin

            model {
                kubernetes {
                    dockerImages {
                        simpleImage(DefaultDockerImage) {
                            maintainer 'Stefan Kollmann <kolle.root@yahoo.de>'
                        }
                    }
                }
            }
        }

        when:
        kubernetesFromModel()

        then:
        ModelRuleExecutionException e = thrown()

        e.cause.message.contains 'The list of instructions must start with an FROM instruction.'
    }

    def 'docker image Dockerfile summary task'() {
        given:
        project.allprojects {
            apply plugin: KubernetesPlugin
        }

        when:
        def tasks = tasksFromModel()

        then:
        def summaryTask = tasks.getByName('kubernetesDockerfiles')
        summaryTask != null
        summaryTask.group == 'Kubernetes'
        summaryTask.description == 'Create all Dockerfiles for the images'
    }

    def 'docker image Dockerfile task per image'() {
        given:
        project.allprojects {
            apply plugin: KubernetesPlugin

            model {
                kubernetes {
                    dockerImages {
                        simpleImage(DefaultDockerImage) {
                            from 'nothing'
                        }
                    }
                }
            }
        }

        when:
        def tasks = tasksFromModel()

        then:
        def task = tasks.findByName('kubernetesDockerfileSimpleImage')
        task instanceof Dockerfile

        task.description == 'Create the Dockerfile for the image simpleImage'
        task.group == 'Kubernetes'

        (task as Dockerfile).instructions.size() > 0

        def summaryTask = tasks.getByName('kubernetesDockerfiles')
        summaryTask.dependsOn.contains task
    }

    def 'docker image add file spec'() {
        given:
        project.allprojects {
            apply plugin: KubernetesPlugin

            model {
                kubernetes {
                    dockerImages {
                        simpleImage(DefaultDockerImage) {
                            from 'nothing'
                            addFiles '/home/something/', {
                                from 'test.txt'
                            }
                            addFiles '/home/something/', {
                                from 'another-file.txt'
                            }
                        }
                    }
                }
            }
        }

        when:
        def kubernetes = kubernetesFromModel()
        DockerImage dockerImage = kubernetes.dockerImages['simpleImage'] as DockerImage

        then:
        dockerImage.instructions.contains 'ADD something-0.zip /home/something/'
        dockerImage.bundles.size() == 2
        dockerImage.bundles[0].bundleName == 'something-0.zip'
        dockerImage.bundles[1].bundleName == 'something-1.zip'
    }
}

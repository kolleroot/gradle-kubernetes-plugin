package com.github.kolleroot.gradle.kubernetes

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.kolleroot.gradle.kubernetes.model.DefaultDockerImage
import com.github.kolleroot.gradle.kubernetes.model.DockerImage
import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import com.github.kolleroot.gradle.kubernetes.testbase.GradleProjectTrait
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Zip
import org.gradle.model.internal.core.ModelRuleExecutionException
import spock.lang.Specification

/**
 * Test the KubernetesPlugin rule source
 */
class RulesTest extends Specification implements GradleProjectTrait {

    private Kubernetes kubernetesFromModel() {
        getFromModel('kubernetes', Kubernetes)
    }

    private TaskContainer tasksFromModel() {
        getFromModel('tasks', TaskContainer)
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
        DockerImage dockerImage = kubernetes.dockerImages['simpleImage'] as DockerImage

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

    def 'docker image create zip file tasks'() {
        given:
        def baseCounter = 0

        and:
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
        def tasks = tasksFromModel()

        then:
        Task something0 = tasks.findByName("kubernetesDockerfileSimpleImageSomething${baseCounter}")
        Task something1 = tasks.findByName("kubernetesDockerfileSimpleImageSomething${baseCounter + 1}")

        something0 instanceof Zip
        something1 instanceof Zip

        (something0 as Zip).archiveName == "something-${baseCounter}.zip".toString()
        (something1 as Zip).archiveName == "something-${baseCounter + 1}.zip".toString()
    }

    def 'docker image build depends on docker file and zip files'() {
        given:
        def baseCounter = 0

        and:
        project.allprojects {
            apply plugin: KubernetesPlugin

            model {
                kubernetes {
                    dockerImages {
                        simpleImage(DefaultDockerImage) {
                            from 'nothing'

                            addFiles '/', {
                                from 'some-file.txt'
                            }

                            addFiles '/home', {
                                from '.bashrc'
                                into 'a'
                            }
                        }
                    }
                }
            }
        }

        when:
        def tasks = tasksFromModel()

        then:
        Dockerfile dockerfileSimpleImage = tasks.findByName('kubernetesDockerfileSimpleImage') as Dockerfile
        Zip dockerfileSimpleImageRoot0 = tasks.findByName("kubernetesDockerfileSimpleImageRoot${baseCounter}") as Zip
        Zip dockerfileSimpleImageHome1 =
                tasks.findByName("kubernetesDockerfileSimpleImageHome${baseCounter + 1}") as Zip
        Task dockerBuildImageTask = tasks.findByName('kubernetesDockerBuildImageSimpleImage')
        Task dockerBuildImagesTask = tasks.findByName('kubernetesDockerBuildImages')

        dockerBuildImageTask instanceof DockerBuildImage

        dockerBuildImageTask.inputs.files.toList() ==
                [
                        dockerfileSimpleImage.destFile,
                        dockerfileSimpleImageRoot0.archivePath,
                        dockerfileSimpleImageHome1.archivePath,
                ]

        // depend on collective build images task
        dockerBuildImagesTask.dependsOn.contains dockerBuildImageTask
    }
}

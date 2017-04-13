package com.github.kolleroot.gradle.kubernetes.integ

import com.github.dockerjava.api.model.ExposedPort
import com.github.kolleroot.gradle.kubernetes.testbase.DockerHelper
import com.github.kolleroot.gradle.kubernetes.testbase.DockerTrait
import com.github.kolleroot.gradle.kubernetes.testbase.GradleTrait
import spock.lang.Specification

/**
 * Specify the creation of the docker images
 */
class CreateDockerImageSpec extends Specification implements GradleTrait, DockerTrait {

    static final String IMAGE_NAME = 'simple:latest'

    def "verify the image metadata"() {
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
                    '$IMAGE_NAME'(DefaultDockerImage) {
                        from 'openjdk:8-jre-alpine'

                        maintainer 'Stefan Kollmann <kolle.root@yahoo.de>'
                        defaultCommand 'java -version'

                        exposePort 80
                    }
                }
            }
        }
        """.stripIndent().trim()

        when: 'the docker build task ran'
        succeeds 'buildDockerImageSimple'

        and: 'got image details'
        def imageDetails = inspectImage 'simple:latest'

        then: 'the host has a docker image with tag simple:latest'
        imageDetails != null
        imageDetails.repoTags.contains IMAGE_NAME

        imageDetails.author == 'Stefan Kollmann <kolle.root@yahoo.de>'
        imageDetails.config.cmd.join(' ') == 'java -version'
        imageDetails.config.exposedPorts as List == [new ExposedPort(80)]

        cleanup: 'delete the created image'
        removeImageIfExists(IMAGE_NAME)
    }

    def "validate, that the files are copied into the docker image"() {
        given: 'some files'
        buildFolder.newFile('user-a-home.txt')
        buildFolder.newFile('user-b-home.txt')

        and: 'a Dockerfile'
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
                    '$IMAGE_NAME'(DefaultDockerImage) {
                        from 'bash'
                        
                        addFiles '/home', {
                            from('user-a-home.txt') {
                                into('a')
                            }
                            
                            from('user-b-home.txt') {
                                into('b')
                            }
                        }
                    }
                }
            }
        }
        """.stripIndent().trim()

        when: 'the docker build task ran'
        succeeds 'buildDockerImageSimple'

        and: 'get the files in the image'
        def lsResponse = runCommandInsideImage(IMAGE_NAME, 'find /home -type f')

        then: 'the image contains all the added files'
        lsResponse == '/home/a/user-a-home.txt\n/home/b/user-b-home.txt\n'

        cleanup: 'delete the created image'
        removeImageIfExists(IMAGE_NAME)
    }
}

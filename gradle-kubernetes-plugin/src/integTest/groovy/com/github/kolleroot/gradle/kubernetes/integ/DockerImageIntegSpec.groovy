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
package com.github.kolleroot.gradle.kubernetes.integ

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import com.github.kolleroot.gradle.kubernetes.testbase.GradleTrait
import com.github.kolleroot.gradle.kubernetes.testbase.ArchiveHelper
import spock.lang.Specification

/**
 * Some integration testing for docker images
 */
class DockerImageIntegSpec extends Specification implements GradleTrait {

    private static final String TEST_TEXT_FILE = '''
        Hello World!
        I'm a very complicated file.
        '''.stripIndent().trim()

    private static final String USER_A_HOME = '''
        Some file in user As home
        '''.stripIndent().trim()

    private static final String USER_B_HOME = '''
        Another file in user Bs home
        '''.stripIndent().trim()

    def 'Dockerfile task creates the Dockerfile'() {
        given:
        buildFile << """
        import com.github.kolleroot.gradle.kubernetes.model.DefaultDockerImage

        plugins {
            id 'com.github.kolleroot.gradle.kubernetes'
        }

        model {
            kubernetes {
                dockerImages {
                    simpleImage(DefaultDockerImage) {
                        from 'nothing'
                        addFiles '/', {
                            from 'src/main/docker/simpleImage/test-file.txt'
                        }
                    }
                }
            }
        }
        """.stripIndent().trim()

        when:
        succeeds 'dockerfiles'

        then:
        buildResult.task(':dockerfileSimpleImage').outcome == SUCCESS
        def dockerfile = new File(buildFolder.root, 'build/kubernetes/dockerimages/simpleImage/Dockerfile')

        dockerfile.exists()
        dockerfile.readLines().join('\n') == """
        FROM nothing
        ADD root-0.tar /
        """.stripIndent().trim()
    }

    def 'Defined TarTasks create the right tar files'() {
        given:
        buildFile << """
        import com.github.kolleroot.gradle.kubernetes.model.DefaultDockerImage

        plugins {
            id 'com.github.kolleroot.gradle.kubernetes'
        }

        model {
            kubernetes {
                dockerImages {
                    simpleImage(DefaultDockerImage) {
                        from 'nothing'
                        addFiles '/', {
                            from 'src/main/docker/simpleImage/test-file.txt'
                        }

                        addFiles '/home', {
                            into 'user-a', {
                                from 'user-a-home.txt'
                            }
                            into 'user-b', {
                                from 'user-b-home.txt'
                            }
                        }
                    }
                }
            }
        }
        """.stripIndent().trim()

        buildFolder.newFolder('src/main/docker/simpleImage'.split(/\//))
        buildFolder.newFile('src/main/docker/simpleImage/test-file.txt') << TEST_TEXT_FILE
        buildFolder.newFile('user-a-home.txt') << USER_A_HOME
        buildFolder.newFile('user-b-home.txt') << USER_B_HOME

        and:
        def resultRootTarMap = ['test-file.txt': TEST_TEXT_FILE]
        def resultHomeTarMap = ['user-a/user-a-home.txt': USER_A_HOME, 'user-b/user-b-home.txt': USER_B_HOME]

        when:
        succeeds 'dockerfileSimpleImageRoot0', 'dockerfileSimpleImageHome1'

        then:
        def rootTar0 = new File(buildFolder.root, 'build/kubernetes/dockerimages/simpleImage/root-0.tar')
        rootTar0.exists()
        ArchiveHelper.toMap(rootTar0) == resultRootTarMap

        def homeTar1 = new File(buildFolder.root, 'build/kubernetes/dockerimages/simpleImage/home-1.tar')
        homeTar1.exists()
        ArchiveHelper.toMap(homeTar1) == resultHomeTarMap
    }
}

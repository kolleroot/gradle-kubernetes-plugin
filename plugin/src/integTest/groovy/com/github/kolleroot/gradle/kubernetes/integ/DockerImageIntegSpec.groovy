package com.github.kolleroot.gradle.kubernetes.integ

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import com.github.kolleroot.gradle.kubernetes.testbase.ZipFileHelper
import com.github.kolleroot.gradle.kubernetes.testbase.GradleTrait

/**
 * Some integration testing for docker images
 */
class DockerImageIntegSpec implements GradleTrait {

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
        succeeds 'kubernetesDockerfiles'

        then:
        buildResult.task(':kubernetesDockerfileSimpleImage').outcome == SUCCESS
        def dockerfile = new File(buildFolder.root, 'build/kubernetes/dockerimages/simpleImage/Dockerfile')

        dockerfile.exists()
        dockerfile.readLines().join('\n') == """
        FROM nothing
        ADD root-0.zip /
        """.stripIndent().trim()
    }

    def 'Defined ZipTasks create the right zip files'() {
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
        def resultRootZipMap = ['test-file.txt': TEST_TEXT_FILE]
        def resultHomeZipMap = ['user-a/user-a-home.txt': USER_A_HOME, 'user-b/user-b-home.txt': USER_B_HOME]

        when:
        succeeds 'kubernetesDockerfileSimpleImageRoot0', 'kubernetesDockerfileSimpleImageHome1'

        then:
        def rootZip0 = new File(buildFolder.root, 'build/kubernetes/dockerimages/simpleImage/root-0.zip')
        rootZip0.exists()
        ZipFileHelper.toMap(rootZip0) == resultRootZipMap

        def homeZip1 = new File(buildFolder.root, 'build/kubernetes/dockerimages/simpleImage/home-1.zip')
        homeZip1.exists()
        ZipFileHelper.toMap(homeZip1) == resultHomeZipMap
    }
}

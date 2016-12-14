package com.github.kolleroot.gradle.kubernetes.integ

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import com.github.kolleroot.gradle.kubernetes.integ.helper.ZipFileHelper
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Some integration testing for docker images
 */
class DockerImageIntegTest extends Specification {

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

    @Rule
    TemporaryFolder buildFolder = new TemporaryFolder()

    File buildFile

    BuildResult buildResult

    private void succeeds(String... tasks) {
        def args = tasks as List<String>
        args << '--stacktrace'
        buildResult = GradleRunner.create()
                .withProjectDir(buildFolder.root)
                .withDebug(true)
                .withPluginClasspath()
                .withArguments(tasks)
                .build()
    }

    def setup() {
        buildFile = buildFolder.newFile('build.gradle')
    }

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

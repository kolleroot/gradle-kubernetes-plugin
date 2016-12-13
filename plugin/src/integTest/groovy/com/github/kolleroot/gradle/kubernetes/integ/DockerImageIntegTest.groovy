package com.github.kolleroot.gradle.kubernetes.integ

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.zip.ZipFile

/**
 * Some integration testing for docker images
 */
class DockerImageIntegTest extends Specification {

    private static final String TEST_TEXT_FILE = """
        Hello World!
        I'm a very complicated file.
        """.stripIndent().trim()

    @Rule
    TemporaryFolder buildFolder = new TemporaryFolder()

    File buildFile

    BuildResult buildResult

    private void succeeds(String task) {
        buildResult = GradleRunner.create()
                .withProjectDir(buildFolder.root)
                .withDebug(true)
                .withPluginClasspath()
                .withArguments(task, '--stacktrace')
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

        buildFolder.newFolder('src/main/docker/simpleImage'.split(/\//))
        buildFolder.newFile('src/main/docker/simpleImage/test-file.txt') << TEST_TEXT_FILE

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

        def rootZip0 = new File(buildFolder.root, 'build/kubernetes/dockerimages/simpleImage/root-0.zip')
        rootZip0.exists()
        Set<File> zipFiles = new ZipFile(rootZip0).findAll()

        rootZip0.readLines().join('\n') == TEST_TEXT_FILE
    }
}

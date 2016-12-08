package com.github.kolleroot.gradle.kubernetes.integ

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Some integration testing for docker images
 */
class DockerImageIntegTest extends Specification {
    @Rule
    TemporaryFolder buildFolder = new TemporaryFolder()

    File buildFile

    BuildResult buildResult

    private void succeeds(String task) {
        buildResult = GradleRunner.create()
                .withProjectDir(buildFolder.root)
                .withPluginClasspath()
                .withArguments(task)
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
        """.stripIndent().trim()
    }
}

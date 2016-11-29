package com.github.kolleroot.gradle.kubernetes

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Test the KubernetesPlugin rule source
 */
class RulesTest extends Specification {

    @Rule
    TemporaryFolder buildFolder = new TemporaryFolder()

    File buildFile

    BuildResult result

    void setup() {
        buildFile = buildFolder.newFile("build.gradle")
    }

    private void succeeds(String arguments) {
        result = GradleRunner
                .create()
                .withDebug(true)
                .withProjectDir(buildFolder.root)
                .withPluginClasspath()
                .withArguments(arguments)
                .build()
    }

    def "register the kubernetes extension"() {
        given:
        buildFile << """
plugins {
    id 'com.gradle.kolleroot.gradle.kubernetes'
}

task test(dependsOn: model) {
    doLast {
        if(kubernetes != null) {
            println 'Success'
        }
    }
}
"""
        when:
        succeeds 'test'

        then:
        result.output.contains 'Success'
    }

    def "create docker image"() {
        given:
        buildFile << """
import com.github.kolleroot.gradle.kubernetes.model.DefaultDockerImage

plugins {
    id 'com.gradle.kolleroot.gradle.kubernetes'
}

kubernetes {
    dockerImages {
        test(DefaultDockerImage)
    }
}

task test(dependsOn: model) {
    doLast {
        if(kubernetes.dockerImages.test != null) {
            println 'Success'
        }
    }
}
"""
        when:
        succeeds 'test'

        then:
        result.output.contains 'Success'
    }
}

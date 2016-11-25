package com.github.kolleroot.gradle.kubernetes

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

    void setup() {
        buildFile = buildFolder.newFile("build.gradle")
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
        def result = GradleRunner
                .create()
                .withProjectDir(buildFolder.root)
                .withPluginClasspath()
                .withArguments('test')
                .build()

        then:
        result.output.contains 'Success'
    }
}

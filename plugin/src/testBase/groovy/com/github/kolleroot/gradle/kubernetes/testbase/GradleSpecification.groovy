package com.github.kolleroot.gradle.kubernetes.testbase

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * The base setup for a gradle specification
 */
class GradleSpecification extends Specification {
    @Rule
    TemporaryFolder buildFolder = new TemporaryFolder()

    File buildFile

    BuildResult buildResult

    def setup() {
        buildFile = buildFolder.newFile('build.gradle')
    }

    protected void succeeds(String... tasks) {
        def args = [*tasks, '--stacktrace']
        buildResult = GradleRunner.create()
                .withProjectDir(buildFolder.root)
                .withDebug(true)
                .withPluginClasspath()
                .withArguments(args)
                .build()
    }
}

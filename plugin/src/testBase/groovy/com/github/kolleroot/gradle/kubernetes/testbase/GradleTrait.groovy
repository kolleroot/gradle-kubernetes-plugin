package com.github.kolleroot.gradle.kubernetes.testbase

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.After
import org.junit.Before
import org.junit.rules.TemporaryFolder

/**
 * The base setup for a gradle specification
 */
trait GradleTrait {

    /**
     * Will be created and deleted in {@link #gradleSetup()} and {@link #gradleCleanup()}.
     */
    TemporaryFolder buildFolder = new TemporaryFolder()

    File buildFile

    BuildResult buildResult

    @Before
    def gradleSetup() {
        buildFolder.create()

        buildFile = buildFolder.newFile('build.gradle')
    }

    @After
    def gradleCleanup() {
        buildFolder.delete()
    }

    void succeeds(String... tasks) {
        def args = [*tasks, '--stacktrace']
        buildResult = GradleRunner.create()
                .withProjectDir(buildFolder.root)
                .withDebug(true)
                .withPluginClasspath()
                .withArguments(args)
                .build()
    }
}

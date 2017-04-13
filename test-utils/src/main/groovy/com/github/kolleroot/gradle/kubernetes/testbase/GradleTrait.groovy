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
        List<String> args = [*tasks, '--stacktrace'] as List<String>
        buildResult = GradleRunner.create()
                .withProjectDir(buildFolder.root)
                .withDebug(true)
                .withPluginClasspath()
                .withArguments(args)
                .build()
    }

    void fails(String... tasks) {
        List<String> args = [*tasks, '--stacktrace'] as List<String>
        buildResult = GradleRunner.create()
                .withProjectDir(buildFolder.root)
                .withDebug(true)
                .withPluginClasspath()
                .withArguments(args)
                .buildAndFail()
    }
}

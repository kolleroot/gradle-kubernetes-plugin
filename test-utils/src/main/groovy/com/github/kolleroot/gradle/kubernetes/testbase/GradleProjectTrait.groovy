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

import org.gradle.api.Project
import org.gradle.model.internal.registry.ModelRegistry
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before

/**
 * Setup a simple gradle project object for testing
 */
trait GradleProjectTrait {
    Project project

    @Before
    void gradleProjectSetup() {
        project = ProjectBuilder.builder().build()
    }

    public <T> T getFromModel(String modelName, Class<T> clazz) {
        (project.modelRegistry as ModelRegistry).find(modelName, clazz) as T
    }
}
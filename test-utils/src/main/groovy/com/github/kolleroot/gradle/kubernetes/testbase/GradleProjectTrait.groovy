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
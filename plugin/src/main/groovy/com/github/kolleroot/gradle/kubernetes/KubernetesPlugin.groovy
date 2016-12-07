package com.github.kolleroot.gradle.kubernetes

import com.github.kolleroot.gradle.kubernetes.model.DockerImage
import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.model.Each
import org.gradle.model.Model
import org.gradle.model.RuleSource
import org.gradle.model.Validate

/**
 * This is the main kubernetes plugin.
 *
 * It will apply all the extensions to the project and generate the required tasks
 */
@SuppressWarnings('GroovyUnusedDeclaration')
class KubernetesPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
    }

    @SuppressWarnings('GroovyUnusedDeclaration')
    static class Rules extends RuleSource {

        @SuppressWarnings('GrMethodMayBeStatic')
        @Model
        void kubernetes(Kubernetes kubernetes) {
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Validate
        void checkDockerImageNotEmptyAndFrom(@Each DockerImage dockerImage) {
            assert !dockerImage.instructions.empty : 'The list of instructions MUST not be empty. There must be at ' +
                    'least one FROM instruction.'
            assert dockerImage.instructions.first().startsWith('FROM ') : 'The list of instructions must start with ' +
                    'an FROM instruction.'
        }
    }
}

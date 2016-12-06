package com.github.kolleroot.gradle.kubernetes

import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.model.Model
import org.gradle.model.RuleSource

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
    }
}

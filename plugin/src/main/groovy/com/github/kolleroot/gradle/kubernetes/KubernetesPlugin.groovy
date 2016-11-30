package com.github.kolleroot.gradle.kubernetes

import com.github.kolleroot.gradle.kubernetes.model.DefaultKubernetes
import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.internal.reflect.Instantiator
import org.gradle.model.Model
import org.gradle.model.RuleSource

import javax.inject.Inject

/**
 * This is the main kubernetes plugin.
 *
 * It will apply all the extensions to the project and generate the required tasks
 */
@SuppressWarnings('GroovyUnusedDeclaration')
class KubernetesPlugin implements Plugin<Project> {

    private static final String KUBERNETES_EXTENSION_NAME = 'kubernetes'

    private final Instantiator instantiator

    @Inject
    KubernetesPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        project.extensions.create KUBERNETES_EXTENSION_NAME, DefaultKubernetes, instantiator
    }

    @SuppressWarnings('GroovyUnusedDeclaration')
    static class Rules extends RuleSource {

        @SuppressWarnings('GrMethodMayBeStatic')
        @Model
        Kubernetes kubernetes(ExtensionContainer extensions) {
            extensions.findByType(Kubernetes)
        }
    }
}

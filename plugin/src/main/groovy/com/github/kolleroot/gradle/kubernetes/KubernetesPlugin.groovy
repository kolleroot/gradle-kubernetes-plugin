package com.github.kolleroot.gradle.kubernetes

import com.github.kolleroot.gradle.kubernetes.models.DefaultKubernetesSpec
import com.github.kolleroot.gradle.kubernetes.models.KubernetesSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.internal.reflect.Instantiator
import org.gradle.model.Model
import org.gradle.model.Mutate
import org.gradle.model.RuleSource

/**
 * This is the main kubernetes plugin.
 *
 * It will apply all the extensions to the project and generate the required tasks
 */
class KubernetesPlugin implements Plugin<Project> {

    private static final def KUBERNETES_EXTENSION_NAME = 'kubernetes'

    @Override
    void apply(Project project) {}

    @SuppressWarnings("GroovyUnusedDeclaration")
    static class Rules extends RuleSource {

        @Model
        KubernetesSpec kubernetesModel(Instantiator instantiator) {
            return instantiator.newInstance(DefaultKubernetesSpec)
        }

        @Mutate
        void registerKubernetesExtension(ExtensionContainer extensions, KubernetesSpec kubernetes) {
            extensions.add(KUBERNETES_EXTENSION_NAME, kubernetes)
        }
    }
}

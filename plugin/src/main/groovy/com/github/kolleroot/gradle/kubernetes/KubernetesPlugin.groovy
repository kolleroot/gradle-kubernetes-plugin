package com.github.kolleroot.gradle.kubernetes

import com.github.kolleroot.gradle.kubernetes.internal.WarProjectPredicate
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.lang.reflect.Method

/**
 * The entry point into the plugin.
 *
 * This class applies the extensions and configurations to the project.
 */
class KubernetesPlugin implements Plugin<Project> {

    private static final def KUBERNETES_EXTENSION_NAME = 'kubernetes'

    private KubernetesExtension kubernetesExtension;

    @Override
    void apply(Project project) {
        kubernetesExtension = new KubernetesExtension(project);

        project.extensions.add(KUBERNETES_EXTENSION_NAME, kubernetesExtension)

        project.afterEvaluate {
            new WarProjectPredicate().test(project)
        }
    }
}

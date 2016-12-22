package com.github.kolleroot.gradle.kubernetes.task

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.NamespaceVisitFromServerGetDeleteRecreateApplicable
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

/**
 * A base task for kubernetes tasks
 */
abstract class KubernetesTask extends DefaultTask {
    @TaskAction
    void action() {
        new DefaultKubernetesClient().withCloseable { kubernetesClient ->
            kubernetesAction(kubernetesClient)
        }
    }

    protected abstract void kubernetesAction(KubernetesClient client)
}

abstract class KubernetesLoadedTask extends KubernetesTask {
    @InputFile
    File configFile

    @Override
    protected void kubernetesAction(KubernetesClient client) {
        kubernetesLoadedAction(client.load(configFile.newInputStream()))
    }

    abstract void kubernetesLoadedAction(
            NamespaceVisitFromServerGetDeleteRecreateApplicable<List<HasMetadata>, Boolean> loaded)
}

class KubernetesCreate extends KubernetesLoadedTask {
    @Override
    void kubernetesLoadedAction(
            NamespaceVisitFromServerGetDeleteRecreateApplicable<List<HasMetadata>, Boolean> loaded) {
        loaded.createOrReplace()
    }
}

class KubernetesDelete extends KubernetesLoadedTask {
    @Override
    void kubernetesLoadedAction(
            NamespaceVisitFromServerGetDeleteRecreateApplicable<List<HasMetadata>, Boolean> loaded) {
        loaded.delete()
    }
}

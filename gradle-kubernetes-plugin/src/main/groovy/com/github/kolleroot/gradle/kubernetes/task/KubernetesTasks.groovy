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
package com.github.kolleroot.gradle.kubernetes.task

import com.github.kolleroot.gradle.kubernetes.helper.PortForwardRegistry
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
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
            NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean> loaded)
}

class KubernetesCreate extends KubernetesLoadedTask {
    @Override
    void kubernetesLoadedAction(
            NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean> loaded) {
        loaded.createOrReplace()
    }
}

class KubernetesDelete extends KubernetesLoadedTask {
    @Override
    void kubernetesLoadedAction(
            NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean> loaded) {
        loaded.delete()
    }
}

class KubernetesOpenPortForwardTask extends DefaultTask {

    public static final String ID_BASE = '%s-%s-%s'

    private String forwardNamespace
    private String forwardPod
    private String forwardPort

    @Input
    @Optional
    String getForwardNamespace() {
        forwardNamespace
    }

    void setForwardNamespace(String namespace) {
        this.forwardNamespace = namespace
    }

    @Input
    String getForwardPod() {
        forwardPod
    }

    void setForwardPod(String pod) {
        this.forwardPod = pod
    }

    @Input
    String getForwardPort() {
        forwardPort
    }

    void setForwardPort(String port) {
        this.forwardPort = port
    }

    @Internal
    String getId() {
        String.format(ID_BASE, forwardNamespace, forwardPod, forwardPort)
    }

    KubernetesOpenPortForwardTask() {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    void action() {
        KubectlPortForwarder forwarder

        if (forwardNamespace == null) {
            forwarder = new KubectlPortForwarder(forwardPod, forwardPort)
        } else {
            forwarder = new KubectlPortForwarder(forwardNamespace, forwardPod, forwardPort)
        }

        PortForwardRegistry.add(id, forwarder)
    }
}

class KubernetesClosePortForwardTask extends DefaultTask {

    private String forwardId

    @Internal
    String getForwardId() {
        this.forwardId
    }

    void setForwardId(String id) {
        this.forwardId = id
    }

    @TaskAction
    void action() {
        PortForwardRegistry.close(this.forwardId)
    }
}

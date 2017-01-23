package com.github.kolleroot.gradle.kubernetes.model

import org.gradle.api.Named
import org.gradle.model.Managed
import org.gradle.model.ModelMap

/**
 * A ModelMap of DockerRegistries
 */
@Managed
interface DockerRegistryContainer extends ModelMap<DockerRegistry> {
}

/**
 * A DockerRegistry represents a place to push docker images to
 */
@Managed
interface DockerRegistry extends Named {

}

/**
 * A DockerRegistry inside a kubernetes cluster
 */
@Managed
interface KubernetesLocalDockerRegistry extends DockerRegistry {
    String getNamespace()
    void setNamespace(String namespace)

    String getPod()
    void setPod(String pod)

    String getPort()
    void setPort(String port)
}

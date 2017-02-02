package com.github.kolleroot.gradle.kubernetes.model.internal

import org.gradle.api.Named
import org.gradle.model.Managed

/**
 * Holdes the some string constants for task creation and configuration.
 */
@Managed
interface DockerRegistryTaskName extends Named {
    String getRegistry()

    void setRegistry(String registry)

    String getOpenTaskName()

    void setOpenTaskName(String name)

    String getCloseTaskName()

    void setCloseTaskName(String name)

    String getNamespace()

    void setNamespace(String namespace)

    String getPod()

    void setPod(String pod)

    String getPort()

    void setPort(String port)
}

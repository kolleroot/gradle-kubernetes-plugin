package com.github.kolleroot.gradle.kubernetes

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 * The configuration to connect to the kubernetes master and general settings for the deployed pods.
 */
class KubernetesExtension {
    def masterUrl;
    def namespace;

    public final NamedDomainObjectContainer<KubernetesAuthExtension> auth;

    private def project;

    public KubernetesExtension(Project project) {
        this.project = project;

        auth = project.container(KubernetesAuthExtension);
    }
}

/**
 * The credentials for the kubernetes connection
 */
class KubernetesAuthExtension {
    def username;
    def password;
}

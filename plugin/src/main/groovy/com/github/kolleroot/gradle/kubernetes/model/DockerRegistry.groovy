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

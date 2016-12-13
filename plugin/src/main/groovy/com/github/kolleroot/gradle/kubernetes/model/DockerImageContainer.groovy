package com.github.kolleroot.gradle.kubernetes.model

import org.gradle.model.Managed
import org.gradle.model.ModelMap

/**
 * The main docker image container holding all the docker images
 */
@Managed
interface DockerImageContainer extends ModelMap<DockerImage> {
}

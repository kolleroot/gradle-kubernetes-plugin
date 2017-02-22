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
package com.github.kolleroot.gradle.kubernetes.rule

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.bmuschko.gradle.docker.tasks.image.DockerTagImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.kolleroot.gradle.kubernetes.KubernetesPlugin
import com.github.kolleroot.gradle.kubernetes.model.DefaultDockerImage
import com.github.kolleroot.gradle.kubernetes.model.KubernetesLocalDockerRegistry
import com.github.kolleroot.gradle.kubernetes.model.api.V1Pod
import com.github.kolleroot.gradle.kubernetes.task.KubernetesCreate
import com.github.kolleroot.gradle.kubernetes.task.KubernetesModelSerializerTask
import com.github.kolleroot.gradle.kubernetes.testbase.GradleProjectTrait
import org.gradle.api.tasks.TaskContainer
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by stefan on 22.02.17.
 */
class DefaultTaskSpec extends Specification implements GradleProjectTrait {

    private TaskContainer getTasksFromModel() {
        getFromModel('tasks', TaskContainer)
    }

    @Unroll
    def "check if the default task #name depend on all tasks of type #type.simpleName"() {
        given: 'a gradle project with the plugin'
        project.allprojects {
            apply plugin: KubernetesPlugin

            repositories {
                jcenter()
            }

            model {
                kubernetes {
                    dockerImages {
                        for (int i = 0; i < 5; i++) {
                            create 'simpleDockerImage' + i, DefaultDockerImage, {
                                from 'alpine:3.5'
                            }
                        }
                    }

                    dockerRegistries {
                        for (int i = 0; i < 3; i++) {
                            create 'dockerRegistry' + i, KubernetesLocalDockerRegistry
                        }
                    }

                    kubernetesObjects {
                        for (int i = 0; i < 5; i++) {
                            create 'kubeObject' + i, V1Pod
                        }
                    }
                }
            }
        }

        when: 'the configuration gets evaluated'
        def tasks = tasksFromModel
        def defaultTask = tasks.getByName(name)

        then: 'the default tasks depend on all their tasks'
        defaultTask.dependsOn == [*(tasks.withType(type) as List), defaultTask.inputs.files] as Set

        where:
        name                                                 | type
        KubernetesPlugin.KUBERNETES_DOCKERFILES_TASK         | Dockerfile
        KubernetesPlugin.KUBERNETES_DOCKER_BUILD_IMAGES_TASK | DockerBuildImage
        KubernetesPlugin.KUBERNETES_DOCKER_TAG_IMAGES_TASK   | DockerTagImage
        KubernetesPlugin.KUBERNETES_DOCKER_PUSH_IMAGES_TASK  | DockerPushImage
        KubernetesPlugin.KUBERNETES_GENERATE_OBJECTS_TASK    | KubernetesModelSerializerTask
        KubernetesPlugin.KUBERNETES_CREATE_OBJECTS_TASK      | KubernetesCreate
    }
}

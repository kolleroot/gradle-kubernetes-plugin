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
package com.github.kolleroot.gradle.kubernetes

import com.bmuschko.gradle.docker.DockerRemoteApiPlugin
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.bmuschko.gradle.docker.tasks.image.DockerTagImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.kolleroot.gradle.kubernetes.helper.DockerImageFileBundleCounter
import com.github.kolleroot.gradle.kubernetes.model.DockerImage
import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import com.github.kolleroot.gradle.kubernetes.model.KubernetesLocalDockerRegistry
import com.github.kolleroot.gradle.kubernetes.model.KubernetesObjectContainer
import com.github.kolleroot.gradle.kubernetes.model.internal.DockerRegistryTaskName
import com.github.kolleroot.gradle.kubernetes.task.KubernetesClosePortForwardTask
import com.github.kolleroot.gradle.kubernetes.task.KubernetesModelSerializerTask
import com.github.kolleroot.gradle.kubernetes.task.KubernetesOpenPortForwardTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Zip
import org.gradle.model.Defaults
import org.gradle.model.Each
import org.gradle.model.Finalize
import org.gradle.model.Model
import org.gradle.model.ModelMap
import org.gradle.model.Mutate
import org.gradle.model.Path
import org.gradle.model.RuleSource
import org.gradle.model.Validate
import org.gradle.model.internal.core.Hidden

import java.nio.file.Paths

/**
 * This is the main kubernetes plugin.
 *
 * It will apply all the extensions to the project and generate the required tasks
 */
@SuppressWarnings('GroovyUnusedDeclaration')
class KubernetesPlugin implements Plugin<Project> {

    static final String KUBERNETES_GROUP = 'Kubernetes'
    static final String DOCKER_GROUP = 'Docker'

    static final String KUBERNETES_DOCKERFILES_TASK = 'dockerfiles'
    static final String KUBERNETES_DOCKER_BUILD_IMAGES_TASK = 'buildDockerImages'
    static final String KUBERNETES_DOCKER_TAG_IMAGES_TASK = 'tagDockerImages'
    static final String KUBERNETES_DOCKER_PUSH_IMAGES_TASK = 'pushDockerImages'
    static final String KUBERNETES_GENERATE_OBJECTS_TASK = 'generateKubernetesObjects'

    static final String KUBERNETES_DOCKERFILE_BASE = 'dockerfile'
    static final String KUBERNETES_DOCKER_BUILD_IMAGE_BASE = 'buildDockerImage'
    static final String KUBERNETES_DOCKER_TAG_BASE = 'tagDockerImage'
    static final String KUBERNETES_DOCKER_PUSH_BASE = 'pushDockerImage'
    static final String KUBERNETES_OBJECT_GENERATE_BASE = 'generateKubernetesObject'

    @Override
    void apply(Project project) {
        project.apply plugin: DockerRemoteApiPlugin

        // this is probably realy not the best way to do it, but ...
        DockerImageFileBundleCounter.COUNTER.set(0)
    }

    @SuppressWarnings('GroovyUnusedDeclaration')
    static class PluginRules extends RuleSource {

        @SuppressWarnings('GrMethodMayBeStatic')
        @Model
        void kubernetes(Kubernetes kubernetes) {
        }

        @Model
        @Hidden
        void kubernetesDockerRegistryTaskNames(ModelMap<DockerRegistryTaskName> map) {
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Defaults
        void addDefaultDockerfileTask(ModelMap<Task> tasks) {
            tasks.create KUBERNETES_DOCKERFILES_TASK, {
                group = DOCKER_GROUP
                description = 'Create all Dockerfiles for the images'
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Defaults
        void addDefaultBuildDockerImagesTask(ModelMap<Task> tasks) {
            tasks.create KUBERNETES_DOCKER_BUILD_IMAGES_TASK, {
                group = DOCKER_GROUP
                description = 'Build all docker images'
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Defaults
        void addDefaultTagDockerImagesTask(ModelMap<Task> tasks) {
            tasks.create KUBERNETES_DOCKER_TAG_IMAGES_TASK, {
                group = DOCKER_GROUP
                description = 'Tag all docker images'
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Defaults
        void addDefaultPushDockerImagesTask(ModelMap<Task> tasks) {
            tasks.create KUBERNETES_DOCKER_PUSH_IMAGES_TASK, {
                group = DOCKER_GROUP
                description = 'Push all docker images'
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Defaults
        void addDefaultKubernetesObjectsTask(ModelMap<Task> tasks) {
            tasks.create KUBERNETES_GENERATE_OBJECTS_TASK, {
                group = KUBERNETES_GROUP
                description = 'Generate all kubernetes objects'
            }
        }

        @Mutate
        void addRegistryTaskNames(ModelMap<DockerRegistryTaskName> kubernetesDockerRegistryTaskNames,
                                  @Path('kubernetes.dockerRegistries') ModelMap<KubernetesLocalDockerRegistry>
                                          dockerRegistries) {
            dockerRegistries.each { dockerRegistry ->
                String name = "${dockerRegistry.name.replace(':', '')}"

                kubernetesDockerRegistryTaskNames.create name,
                        DockerRegistryTaskName, {
                    openTaskName = 'openKubeLocalDockerRegistry' + name.capitalize()
                    closeTaskName = 'closeKubeLocalDockerRegistry' + name.capitalize()

                    registry = dockerRegistry.name
                    namespace = dockerRegistry.namespace
                    pod = dockerRegistry.pod
                    port = dockerRegistry.port
                }
            }
        }

        @SuppressWarnings('SpaceAroundOperator')
        @Mutate
        void addRegistryTasks(ModelMap<Task> tasks,
                              ModelMap<DockerRegistryTaskName>
                                      registries) {
            registries.each { registry ->

                tasks.create registry.openTaskName, KubernetesOpenPortForwardTask, {
                    // group = KUBERNETES_GROUP // don't show this task in the task list
                    description = 'Starts forwarding a local port to the registry inside the kubernetes cluster'

                    forwardNamespace = registry.namespace
                    forwardPod = registry.pod
                    forwardPort = registry.port
                }

                KubernetesOpenPortForwardTask openTask = tasks.get(registry.openTaskName) as
                        KubernetesOpenPortForwardTask

                tasks.create registry.closeTaskName, KubernetesClosePortForwardTask, {
                    // group = KUBERNETES_GROUP // don't show this task in the task list
                    description = 'Stops forwarding a local port'

                    forwardId = openTask.id
                }
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Mutate
        void addDockerFileTask(ModelMap<Task> tasks,
                               @Path('tasks.dockerfiles') Task dockerfilesTask,
                               @Path('tasks.buildDockerImages') Task buildDockerImagesTask,
                               @Path('buildDir') File buildDir,
                               @Path('kubernetes.dockerImages') ModelMap<DockerImage> dockerImages) {
            dockerImages.each { dockerImage ->
                String relativeDockerImagePath = "kubernetes/dockerimages/${dockerImage.name}"

                List<Task> zipTasks = []
                dockerImage.bundles.each { bundle ->
                    String zipTaskName = KUBERNETES_DOCKERFILE_BASE + dockerImage.name.capitalize() +
                            "${bundle.bundleName.split(/\./)[0].replace('-', '').capitalize()}"
                    tasks.create zipTaskName, Zip, {
                        archiveName bundle.bundleName
                        destinationDir Paths.get(buildDir.toString(), relativeDockerImagePath).toFile()
                    }

                    // apply the spec from the model to the zipTask
                    Zip zipTask = tasks.get(zipTaskName) as Zip
                    bundle.spec.delegate = zipTask
                    bundle.spec.resolveStrategy = Closure.DELEGATE_FIRST
                    bundle.spec.call()

                    zipTasks << zipTask
                }

                String dockerfileTaskName = KUBERNETES_DOCKERFILE_BASE + dockerImage.name.capitalize()
                tasks.create dockerfileTaskName, Dockerfile, {
                    group = DOCKER_GROUP
                    description = "Create the Dockerfile for the image ${dockerImage.name}"

                    destFile = Paths.get(buildDir.toString(), relativeDockerImagePath, 'Dockerfile').toFile()
                    dockerImage.instructions.each {
                        instructions << new Dockerfile.GenericInstruction(it)
                    }
                }

                Dockerfile dockerfileTask = tasks.get(dockerfileTaskName) as Dockerfile
                dockerfilesTask.dependsOn dockerfileTask

                String dockerBuildImageTaskName = KUBERNETES_DOCKER_BUILD_IMAGE_BASE + dockerImage.name.capitalize()
                tasks.create dockerBuildImageTaskName, DockerBuildImage, {
                    group = DOCKER_GROUP
                    description = "Create the docker image from the Dockerfile for the image ${dockerImage.name}"

                    dockerFile = dockerfileTask.destFile
                    inputDir = Paths.get(buildDir.toString(), relativeDockerImagePath).toFile()

                    tag = dockerImage.name

                    inputs.files dockerfileTask, zipTasks
                }

                DockerBuildImage buildImageTask = tasks.get(dockerBuildImageTaskName) as DockerBuildImage
                buildDockerImagesTask.dependsOn buildImageTask
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Mutate
        void addDockerTagPushTasks(ModelMap<Task> tasks,
                                   @Path('tasks.tagDockerImages') tagDockerImagesTask,
                                   @Path('tasks.pushDockerImages') pushDockerImagesTask,
                                   ModelMap<DockerRegistryTaskName> dockerRegistries,
                                   @Path('kubernetes.dockerImages') ModelMap<DockerImage> dockerImages) {
            dockerRegistries.each { dockerRegistry ->
                dockerImages.each { dockerImage ->
                    String tagTaskName = KUBERNETES_DOCKER_TAG_BASE +
                            dockerRegistry.name.capitalize() +
                            dockerImage.name.capitalize()

                    tasks.create tagTaskName,
                            DockerTagImage, {
                        group = DOCKER_GROUP
                        description = "Tag the image ${dockerImage.name} with ${dockerRegistry.registry}/${dockerImage.name}"

                        repository = dockerRegistry.registry + '/' + dockerImage.name
                        targetImageId { dockerImage.name }
                        tag = 'latest'

                    }

                    Task tagTask = tasks.get(tagTaskName)
                    tagDockerImagesTask.dependsOn tagTask

                    String pushTaskName = KUBERNETES_DOCKER_PUSH_BASE +
                            dockerRegistry.name.capitalize() +
                            dockerImage.name.capitalize()

                    tasks.create pushTaskName,
                            DockerPushImage, {
                        group = DOCKER_GROUP
                        description = "Push the image ${dockerRegistry.registry}/${dockerImage.name}"

                        imageName = dockerRegistry.registry + '/' + dockerImage.name

                        dependsOn tagTask
                    }

                    Task pushTask = tasks.get(pushTaskName)
                    pushDockerImagesTask.dependsOn pushTask
                }
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Mutate
        void addKubernetesObjectGeneratorTasks(
                ModelMap<Task> tasks,
                @Path('kubernetes.kubernetesObjects') KubernetesObjectContainer kubernetesObjects,
                @Path('buildDir') File buildDir,
                @Path('tasks.generateKubernetesObjects') Task kubernetesObjectsTask) {
            kubernetesObjects.each { kubernetesObject ->
                String taskName = KUBERNETES_OBJECT_GENERATE_BASE + kubernetesObject.name.capitalize()
                tasks.create taskName, KubernetesModelSerializerTask, {
                    group = KUBERNETES_GROUP
                    description = "Serialize the kubernetes object ${kubernetesObject.name}"

                    object = kubernetesObject
                    jsonFile = "$buildDir/kubernetes/objects/${kubernetesObject.name}.json"
                }

                Task kubernetesObjectTask = tasks.get(taskName)
                kubernetesObjectsTask.dependsOn kubernetesObjectTask
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Finalize
        void connectBuildToTag(ModelMap<Task> tasks,
                               ModelMap<DockerRegistryTaskName> dockerRegistries,
                               @Path('kubernetes.dockerImages') ModelMap<DockerImage> dockerImages) {
            dockerRegistries.each { dockerRegistry ->
                dockerImages.each { dockerImage ->
                    String dockerBuildImageTaskName = KUBERNETES_DOCKER_BUILD_IMAGE_BASE + dockerImage.name.capitalize()

                    String tagTaskName = KUBERNETES_DOCKER_TAG_BASE +
                            dockerRegistry.name.capitalize() +
                            dockerImage.name.capitalize()

                    Task tagTask = tasks.get(tagTaskName)
                    Task buildTask = tasks.get(dockerBuildImageTaskName)

                    tagTask.dependsOn buildTask
                }
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Finalize
        void connectPushWithOpenAndClose(ModelMap<Task> tasks,
                                         ModelMap<DockerRegistryTaskName> dockerRegistries,
                                         @Path('kubernetes.dockerImages') ModelMap<DockerImage> dockerImages) {
            dockerRegistries.each { dockerRegistry ->
                dockerImages.each { dockerImage ->
                    String pushTaskName = KUBERNETES_DOCKER_PUSH_BASE +
                            dockerRegistry.name.capitalize() +
                            dockerImage.name.capitalize()

                    Task push = tasks.get(pushTaskName)
                    Task open = tasks.get(dockerRegistry.openTaskName)
                    Task close = tasks.get(dockerRegistry.closeTaskName)

                    push.dependsOn open
                    push.finalizedBy close
                }
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Validate
        void checkDockerImageNotEmptyAndFrom(@Each DockerImage dockerImage) {
            assert !dockerImage.instructions.empty: 'The list of instructions MUST not be empty. There must be at' +
                    ' least one FROM instruction.'
            assert dockerImage.instructions.first().startsWith('FROM '): 'The list of instructions must start ' +
                    'with an FROM instruction.'
        }
    }
}

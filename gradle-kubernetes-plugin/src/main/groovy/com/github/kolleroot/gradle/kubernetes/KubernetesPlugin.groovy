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
import com.github.kolleroot.gradle.kubernetes.helper.TaskNameHelper
import com.github.kolleroot.gradle.kubernetes.model.DockerImage
import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import com.github.kolleroot.gradle.kubernetes.model.KubernetesLocalDockerRegistry
import com.github.kolleroot.gradle.kubernetes.model.KubernetesObjectContainer
import com.github.kolleroot.gradle.kubernetes.model.internal.DockerRegistryTaskName
import com.github.kolleroot.gradle.kubernetes.rule.DefaultTasks
import com.github.kolleroot.gradle.kubernetes.task.KubernetesClosePortForwardTask
import com.github.kolleroot.gradle.kubernetes.task.KubernetesCreate
import com.github.kolleroot.gradle.kubernetes.task.KubernetesModelSerializerTask
import com.github.kolleroot.gradle.kubernetes.task.KubernetesOpenPortForwardTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Zip
import org.gradle.model.Each
import org.gradle.model.Finalize
import org.gradle.model.Model
import org.gradle.model.ModelMap
import org.gradle.model.Mutate
import org.gradle.model.Path
import org.gradle.model.RuleSource
import org.gradle.model.Rules
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
    static final String KUBERNETES_CREATE_OBJECTS_TASK = 'createKubernetesObjects'


    @Override
    void apply(Project project) {
        project.apply plugin: DockerRemoteApiPlugin

        // this is probably not the best way to do it, but ...
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

        @Rules
        void addDefaultTasks(DefaultTasks defaultRules, ModelMap<Task> tasks) {
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
                    String zipTaskName = TaskNameHelper.getDockerfileZipTaskName(dockerImage, bundle)
                    tasks.create zipTaskName, Zip, {
                        archiveName bundle.bundleName
                        destinationDir Paths.get(buildDir.toString(), relativeDockerImagePath).toFile()
                    }

                    // apply the spec from the model to the zipTask
                    Zip zipTask = tasks.get(zipTaskName) as Zip
                    bundle.spec.delegate = zipTask
                    //noinspection UnnecessaryQualifiedReference
                    bundle.spec.resolveStrategy = Closure.DELEGATE_FIRST
                    bundle.spec.call()

                    zipTasks << zipTask
                }

                String dockerfileTaskName = TaskNameHelper.getDockerfileTaskName(dockerImage)
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

                String dockerBuildImageTaskName = TaskNameHelper.getBuildTaskName(dockerImage)
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
                    String tagTaskName = TaskNameHelper.getTagTaskName(dockerRegistry, dockerImage)

                    tasks.create tagTaskName,
                            DockerTagImage, {
                        group = DOCKER_GROUP
                        description =
                                "Tag the image ${dockerImage.name} with ${dockerRegistry.registry}/${dockerImage.name}"

                        repository = dockerRegistry.registry + '/' + dockerImage.name
                        targetImageId { dockerImage.name }
                        tag = 'latest'

                    }

                    Task tagTask = tasks.get(tagTaskName)
                    tagDockerImagesTask.dependsOn tagTask

                    String pushTaskName = TaskNameHelper.getPushTaskName(dockerRegistry, dockerImage)

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
                String taskName = TaskNameHelper.getGenerateTaskName(kubernetesObject)

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
        @Mutate
        void addKubernetesCreateTask(
                ModelMap<Task> tasks,
                @Path('tasks.createKubernetesObjects') Task createKubernetesObjectsTask,
                @Path('kubernetes.kubernetesObjects') KubernetesObjectContainer kubernetesObjects,
                @Path('buildDir') File buildDir
        ) {
            kubernetesObjects.each { kubernetesObject ->
                String createTaskName = TaskNameHelper.getCreateTaskName(kubernetesObject)

                tasks.create createTaskName, KubernetesCreate, {
                    group = KUBERNETES_GROUP
                    description = "Create the kubernetes object ${kubernetesObject.name} in the cluster"

                    configFile = "$buildDir/kubernetes/objects/${kubernetesObject.name}.json"
                }

                Task createTask = tasks.get(createTaskName)
                createKubernetesObjectsTask.dependsOn createTask
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Finalize
        void connectBuildToTag(ModelMap<Task> tasks,
                               ModelMap<DockerRegistryTaskName> dockerRegistries,
                               @Path('kubernetes.dockerImages') ModelMap<DockerImage> dockerImages) {
            dockerRegistries.each { dockerRegistry ->
                dockerImages.each { dockerImage ->
                    String dockerBuildImageTaskName = TaskNameHelper.getBuildTaskName(dockerImage)

                    String tagTaskName = TaskNameHelper.getTagTaskName(dockerRegistry, dockerImage)

                    Task tagTask = tasks.get(tagTaskName)
                    Task buildTask = tasks.get(dockerBuildImageTaskName)

                    tagTask.dependsOn buildTask
                }
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Finalize
        void connectPushWithOpenAndClose(ModelMap<Task> tasks,
                                         @Path('tasks.pushDockerImages') pushDockerImagesTask,
                                         ModelMap<DockerRegistryTaskName> dockerRegistries,
                                         @Path('kubernetes.dockerImages') ModelMap<DockerImage> dockerImages) {
            dockerRegistries.each { dockerRegistry ->
                dockerImages.each { dockerImage ->
                    String pushTaskName = TaskNameHelper.getPushTaskName(dockerRegistry, dockerImage)

                    Task push = tasks.get(pushTaskName)
                    Task open = tasks.get(dockerRegistry.openTaskName)
                    Task close = tasks.get(dockerRegistry.closeTaskName)

                    push.dependsOn open
                    push.finalizedBy close

                    pushDockerImagesTask.dependsOn close
                }
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Finalize
        void connectCreateObjectWithGenerateObject(ModelMap<Task> tasks,
                                                   @Path('kubernetes.kubernetesObjects') KubernetesObjectContainer kubernetesObjects) {
            kubernetesObjects.each { kubernetesObject ->
                Task generateTask = tasks.get(TaskNameHelper.getGenerateTaskName(kubernetesObject))
                Task createTask = tasks.get(TaskNameHelper.getCreateTaskName(kubernetesObject))

                createTask.dependsOn generateTask
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Finalize
        void connectCreateObjectWithPush(ModelMap<Task> tasks,
                                         @Path('kubernetes.kubernetesObjects') KubernetesObjectContainer kubernetesObjects,
                                         ModelMap<DockerRegistryTaskName> dockerRegistries,
                                         @Path('kubernetes.dockerImages') ModelMap<DockerImage> dockerImages) {
            kubernetesObjects.each { kubernetesObject ->
                Task createTask = tasks.get(TaskNameHelper.getCreateTaskName(kubernetesObject))

                dockerRegistries.each { dockerRegistry ->
                    dockerImages.each { dockerImage ->
                        String pushTaskName = TaskNameHelper.getPushTaskName(dockerRegistry, dockerImage)

                        Task pushTask = tasks.get(pushTaskName)
                        createTask.dependsOn pushTask
                    }
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

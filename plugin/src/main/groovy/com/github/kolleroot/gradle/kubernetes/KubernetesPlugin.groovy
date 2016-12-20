package com.github.kolleroot.gradle.kubernetes

import com.bmuschko.gradle.docker.DockerRemoteApiPlugin
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.kolleroot.gradle.kubernetes.helper.DockerImageFileBundleCounter
import com.github.kolleroot.gradle.kubernetes.model.DockerImage
import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Zip
import org.gradle.model.Defaults
import org.gradle.model.Each
import org.gradle.model.Model
import org.gradle.model.ModelMap
import org.gradle.model.Mutate
import org.gradle.model.Path
import org.gradle.model.RuleSource
import org.gradle.model.Validate

import java.nio.file.Paths

/**
 * This is the main kubernetes plugin.
 *
 * It will apply all the extensions to the project and generate the required tasks
 */
@SuppressWarnings('GroovyUnusedDeclaration')
class KubernetesPlugin implements Plugin<Project> {

    static final String KUBERNETES_GROUP = 'Kubernetes'

    static final String KUBERNETES_DOCKERFILES_TASK = 'kubernetesDockerfiles'
    static final String KUBERNETES_DOCKER_BUILD_IMAGES_TASK = 'kubernetesDockerBuildImages'

    static final String KUBERNETES_DOCKERFILE_BASE = 'kubernetesDockerfile'
    static final String KUBERNETES_DOCKER_BUILD_IMAGE_BASE = 'kubernetesDockerBuildImage'

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

        @SuppressWarnings('GrMethodMayBeStatic')
        @Validate
        void checkDockerImageNotEmptyAndFrom(@Each DockerImage dockerImage) {
            assert !dockerImage.instructions.empty: 'The list of instructions MUST not be empty. There must be at' +
                    ' least one FROM instruction.'
            assert dockerImage.instructions.first().startsWith('FROM '): 'The list of instructions must start ' +
                    'with an FROM instruction.'
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Defaults
        void addDefaultDockerfileTask(ModelMap<Task> tasks) {
            tasks.create KUBERNETES_DOCKERFILES_TASK, {
                group = KUBERNETES_GROUP
                description = 'Create all Dockerfiles for the images'
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Defaults
        void addDefaultDockerBuildImagesTask(ModelMap<Task> tasks) {
            tasks.create KUBERNETES_DOCKER_BUILD_IMAGES_TASK, {
                group = KUBERNETES_GROUP
                description = 'Build all docker images'
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Mutate
        void addDockerFileTask(ModelMap<Task> tasks,
                               @Path('tasks.kubernetesDockerfiles') Task kubernetesDockerfiles,
                               @Path('tasks.kubernetesDockerBuildImages') Task kubernetesDockerBuildImages,
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
                    group = KUBERNETES_GROUP
                    description = "Create the Dockerfile for the image ${dockerImage.name}"

                    destFile = Paths.get(buildDir.toString(), relativeDockerImagePath, 'Dockerfile').toFile()
                    dockerImage.instructions.each {
                        instructions << new Dockerfile.GenericInstruction(it)
                    }
                }

                Dockerfile dockerfileTask = tasks.get(dockerfileTaskName) as Dockerfile
                kubernetesDockerfiles.dependsOn dockerfileTask

                String dockerBuildImageTaskName = KUBERNETES_DOCKER_BUILD_IMAGE_BASE + dockerImage.name.capitalize()
                tasks.create dockerBuildImageTaskName, DockerBuildImage, {
                    group = KUBERNETES_GROUP
                    description = "Create the docker image from the Dockerfile for the image ${dockerImage.name}"

                    dockerFile = dockerfileTask.destFile
                    inputDir = Paths.get(buildDir.toString(), relativeDockerImagePath).toFile()

                    tag = dockerImage.name

                    inputs.files dockerfileTask, zipTasks
                }

                 DockerBuildImage buildImageTask = tasks.get(dockerBuildImageTaskName) as DockerBuildImage
                 kubernetesDockerBuildImages.dependsOn buildImageTask
            }
        }
    }
}

package com.github.kolleroot.gradle.kubernetes

import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.kolleroot.gradle.kubernetes.model.DockerImage
import com.github.kolleroot.gradle.kubernetes.model.FileBundle
import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import org.codehaus.groovy.runtime.ConvertedClosure
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
import org.gradle.model.Rules
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

    @Override
    void apply(Project project) {
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
            assert !dockerImage.instructions.empty: 'The list of instructions MUST not be empty. There must be at ' +
                    'least one FROM instruction.'
            assert dockerImage.instructions.first().startsWith('FROM '): 'The list of instructions must start with an' +
                    ' FROM instruction.'
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Defaults
        void addDefaultDockerfileTask(ModelMap<Task> tasks) {
            tasks.create 'kubernetesDockerfiles', {
                group = KUBERNETES_GROUP
                description = 'Create all Dockerfiles for the images'
            }
        }

        @SuppressWarnings('GrMethodMayBeStatic')
        @Mutate
        void addDockerFileTask(ModelMap<Task> tasks,
                               @Path('tasks.kubernetesDockerfiles') Task kubernetesDockerfiles,
                               @Path('buildDir') File buildDir,
                               @Path('kubernetes.dockerImages') ModelMap<DockerImage> dockerImages) {
            dockerImages.each {
                dockerImage ->
                    String relativeDockerImagePath = "kubernetes/dockerimages/${dockerImage.name}"

                    List<Task> zipTasks = []
                    dockerImage.bundles.each { bundle ->
                        String zipTaskName = "kubernetesDockerfile${dockerImage.name.capitalize()}" +
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

                    String dockerfileTaskName = "kubernetesDockerfile${dockerImage.name.capitalize()}"
                    tasks.create dockerfileTaskName, Dockerfile, {
                        group = KUBERNETES_GROUP
                        description = "Create the Dockerfile for the image ${dockerImage.name}"

                        destFile = Paths.get(buildDir.toString(), relativeDockerImagePath, 'Dockerfile').toFile()
                        dockerImage.instructions.each {
                            instructions << new Dockerfile.GenericInstruction(it)
                        }
                    }

                    Task dockerfileTask = tasks.get(dockerfileTaskName)
                    dockerfileTask.dependsOn zipTasks

                    kubernetesDockerfiles.dependsOn dockerfileTask
            }
        }

        @Rules
        void applyDockerImageRules(DockerImageRules rules, @Each DockerImage image) {}
    }

    static class DockerImageRules extends RuleSource {
    }
}

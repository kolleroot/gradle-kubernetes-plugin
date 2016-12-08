package com.github.kolleroot.gradle.kubernetes

import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.kolleroot.gradle.kubernetes.model.DockerImage
import com.github.kolleroot.gradle.kubernetes.model.Kubernetes
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.model.Defaults
import org.gradle.model.Each
import org.gradle.model.Model
import org.gradle.model.ModelMap
import org.gradle.model.Mutate
import org.gradle.model.Path
import org.gradle.model.RuleSource
import org.gradle.model.Validate

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
                                  @Path('kubernetes.dockerImages') ModelMap<DockerImage> dockerImages) {
            dockerImages.each {
                dockerImage ->
                    String taskName = "kubernetesDockerfile${dockerImage.name.capitalize()}"
                    tasks.create taskName, Dockerfile, {
                        group = KUBERNETES_GROUP
                        description = "Create the Dockerfile for the image ${dockerImage.name}"

                        destFile = project.file(
                                "${project.buildDir}/kubernetes/dockerimages/${dockerImage.name}/Dockerfile"
                        )
                        dockerImage.instructions.each {
                            instructions << new Dockerfile.GenericInstruction(it)
                        }
                    }

                    kubernetesDockerfiles.dependsOn tasks.get(taskName)
            }
        }
    }
}

package com.github.kolleroot.gradle.kubernetes.apimodel

import com.github.kolleroot.gradle.kubernetes.model.api.V1Pod
import com.github.kolleroot.gradle.kubernetes.testbase.GradleProjectTrait
import org.gradle.model.Model
import org.gradle.model.RuleSource
import spock.lang.Specification

/**
 * Check if the generated json is right
 */
class JsonModelSpec extends Specification implements GradleProjectTrait {
    def "create a pod spec"() {
        given: 'a project with a pod model'
        project.allprojects {
            apply plugin: PluginWithPodModel
        }
        and: 'a pod model'
        project.allprojects {
            model {
                pod {
                    apiVersion = 'v1'
                    kind = 'Pod'
                    metadata {
                        name = 'test'
                    }
                    spec {
                        containers.create {
                            image = 'ubuntu:trusty'
                            command = ['echo']
                            args = ['Hello World']
                        }
                    }
                }
            }
        }
        when: 'converting the model to json'
        V1Pod pod = getFromModel('pod', V1Pod)

        then: 'it matches the template'
        pod.apiVersion == 'v1'
        pod.kind == 'Pod'
        pod.metadata.name == 'test'
        pod.spec.containers[0].image == 'ubuntu:trusty'
        pod.spec.containers[0].command == ['echo']
        pod.spec.containers[0].args == ['Hello World']
    }

    static class PluginWithPodModel extends RuleSource {
        @Model
        void pod(V1Pod pod) {

        }
    }
}

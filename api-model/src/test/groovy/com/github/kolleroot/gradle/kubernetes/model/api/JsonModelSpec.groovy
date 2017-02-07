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
package com.github.kolleroot.gradle.kubernetes.model.api

import com.github.kolleroot.gradle.kubernetes.testbase.GradleProjectTrait
import org.gradle.model.Model
import org.gradle.model.RuleSource
import spock.lang.Specification

/**
 * Check if the generated json is right
 */
class JsonModelSpec extends Specification implements GradleProjectTrait {
    def "check if a managed model was modified"() {
        given: 'a managed model'
        project.allprojects {
            apply plugin: PluginWithPodModel
        }
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

        V1Pod pod = getFromModel('pod', V1Pod)

        def stateField = pod.class.getDeclaredField('$state')
        stateField.accessible = true

        def state = stateField.get(pod)

        def declaringClassField = state.class.getDeclaredField('this$1')
        declaringClassField.accessible = true

        def declaringClass = declaringClassField.get(state)

        def propertyViewsField = declaringClass.class.getDeclaredField('propertyViews')
        propertyViewsField.accessible = true

        Map propertyViews = propertyViewsField.get(declaringClass)

        expect: 'the propertyView to be empty'

        propertyViews.size() == 0
    }

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

        /* TODO: use the new EmptyObjectRemoverGenson class and test it
        String json = new GradleGensonBuilder()
                .withBundle(GradleManagedModelBundle.INSTANCE)
                .create()
                .serialize(pod)*/

        then: 'it matches the template'
        pod.apiVersion == 'v1'
        pod.kind == 'Pod'
        pod.metadata.name == 'test'
        pod.spec.containers[0].image == 'ubuntu:trusty'
        pod.spec.containers[0].command == ['echo']
        pod.spec.containers[0].args == ['Hello World']

        // json == '{"apiVersion":"v1","kind":"Pod","metadata":{"name":"test"},' +
        //         '"spec":{"containers":[{"image":"ubuntu:trusty","command":["echo"],"args":["Hello World"]}]}}'
    }

    static class PluginWithPodModel extends RuleSource {
        @SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter'])
        @Model
        void pod(V1Pod pod) {

        }
    }
}

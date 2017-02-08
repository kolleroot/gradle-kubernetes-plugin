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

import com.github.kolleroot.gradle.kubernetes.testbase.GradleTrait
import spock.lang.Specification

/**
 * Created by stefan on 08.02.17.
 */
class KubernetesModelSerializerTaskSpec extends Specification implements GradleTrait {
    static final String BASE_SETUP = """
        import com.github.kolleroot.gradle.kubernetes.model.api.V1Pod
        import com.github.kolleroot.gradle.kubernetes.task.KubernetesModelSerializerTask
        import org.gradle.model.Model
        import org.gradle.model.Mutate
        import org.gradle.model.RuleSource

        plugins {
            id 'com.github.kolleroot.gradle.kubernetes'
        }

        repositories {
            jcenter()
        }

        class SimpleTestRule extends RuleSource {
            @Model
            void pod(V1Pod pod) {
            }

            @Mutate
            void createTask(ModelMap<Task> tasks, V1Pod pod) {
                tasks.create 'generateJson', KubernetesModelSerializerTask, {
                    object = pod
                    jsonFile = "build/pod.json"
                }
            }
        }

        apply plugin: SimpleTestRule

        """.stripIndent()

    def setup() {
        buildFile.text = BASE_SETUP
    }

    def "serialize an empty pod"() {
        given: 'a gradle project with an empty model and task creating rule'

        when: 'the build succeeds'
        succeeds(':generateJson')
        def jsonFile = new File(buildFolder.root, 'build/pod.json')

        then: 'there will be a json file'
        jsonFile.exists()
        jsonFile.text == ''
    }

    def "serialize a simple pod"() {
        given: 'a gradle project with a simple model and task creating rule'
        buildFile.text += """
        model {
            pod {
                apiVersion = 'v1'
                kind = 'Pod'
                metadata {
                    name = 'test'
                }
            }
        }
        """.stripIndent().trim()

        when: 'the build succeeds'
        succeeds(':generateJson')
        def jsonFile = new File(buildFolder.root, 'build/pod.json')

        then: 'there will be a json file'
        jsonFile.exists()
        jsonFile.text == '{"apiVersion":"v1","kind":"Pod","metadata":{"name":"test"}}'
    }
}

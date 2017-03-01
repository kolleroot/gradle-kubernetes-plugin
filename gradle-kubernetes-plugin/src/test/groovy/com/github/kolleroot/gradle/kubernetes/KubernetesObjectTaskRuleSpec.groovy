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
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

/**
 * Specify the rules for task generation
 */
class KubernetesObjectTaskRuleSpec extends Specification implements GradleTrait {
    def "create a kubernetes deployment using the plugin"() {
        given: 'a build with the kubernetes plugin and an kubernetes object'
        buildFile << """
        import com.github.kolleroot.gradle.kubernetes.model.api.generated.V1beta1Deployment

        plugins {
            id 'com.github.kolleroot.gradle.kubernetes'
        }

        repositories {
            jcenter()
        }

        model {
            kubernetes {
                kubernetesObjects {
                    myApp(V1beta1Deployment) {
                        apiVersion = "v1beta1"
                        kind = "Deployment"
                        metadata {
                            name = "MyApp"
                            namespace = "test"
                        }
                        spec {
                            replicas = 1
                            template {
                                spec {
                                    containers.create {
                                        name = "main"
                                        image = "ubuntu:yakety"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        """.stripIndent().trim()

        when: 'the task :kubernetesGenerateObjectMyApp succeeds'
        succeeds(':generateKubernetesObjectMyApp')

        then: 'there exists a file in the build folder'

        File json = new File(buildFolder.root, '/build/kubernetes/objects/myApp.json')
        json.text ==
                '{\n' +
                '  "apiVersion":"v1beta1",\n' +
                '  "kind":"Deployment",\n' +
                '  "metadata":{\n' +
                '    "name":"MyApp",\n' +
                '    "namespace":"test"\n' +
                '  },\n' +
                '  "spec":{\n' +
                '    "replicas":1,\n' +
                '    "template":{\n' +
                '      "spec":{\n' +
                '        "containers":[\n' +
                '          {\n' +
                '            "image":"ubuntu:yakety",\n' +
                '            "name":"main"\n' +
                '          }\n' +
                '        ]\n' +
                '      }\n' +
                '    }\n' +
                '  }\n' +
                '}'
    }

    def "create two minimal pods"() {
        given: 'a build with the kubernetes plugin and an kubernetes object'
        buildFile << """
        import com.github.kolleroot.gradle.kubernetes.model.api.generated.V1Pod

        plugins {
            id 'com.github.kolleroot.gradle.kubernetes'
        }

        repositories {
            jcenter()
        }

        model {
            kubernetes {
                kubernetesObjects {
                    pod1(V1Pod) {
                        apiVersion = "v1"
                        kind = "Pod"
                        metadata {
                            name = "Pod1"
                        }
                        spec {
                            containers.create {
                                name = "main"
                                image = "ubuntu:yakety"
                            }
                        }
                    }
                    pod2(V1Pod) {
                        apiVersion = "v1"
                        kind = "Pod"
                        metadata {
                            name = "Pod2"
                        }
                        spec {
                            containers.create {
                                name = "main"
                                image = "ubuntu:yakety"
                            }
                        }
                    }
                }
            }
        }
        """.stripIndent().trim()

        when: 'the task :kubernetesObjects succeeds'
        succeeds(':generateKubernetesObjects')

        then: 'both generate tasks are executed successfuly'
        buildResult.task(':generateKubernetesObjectPod1').outcome == TaskOutcome.SUCCESS
        buildResult.task(':generateKubernetesObjectPod2').outcome == TaskOutcome.SUCCESS

        and: 'there exist files in the build folder'
        final String POD_N =
                        '{\n' +
                        '  "apiVersion":"v1",\n' +
                        '  "kind":"Pod",\n' +
                        '  "metadata":{\n' +
                        '    "name":"Pod%d"\n' +
                        '  },\n' +
                        '  "spec":{\n' +
                        '    "containers":[\n' +
                        '      {\n' +
                        '        "image":"ubuntu:yakety",\n' +
                        '        "name":"main"\n' +
                        '      }\n' +
                        '    ]\n' +
                        '  }\n' +
                        '}'

        File pod1File = new File(buildFolder.root, '/build/kubernetes/objects/pod1.json')
        pod1File.text == String.format(POD_N, 1)

        File pod2File = new File(buildFolder.root, '/build/kubernetes/objects/pod2.json')
        pod2File.text == String.format(POD_N, 2)
    }
}

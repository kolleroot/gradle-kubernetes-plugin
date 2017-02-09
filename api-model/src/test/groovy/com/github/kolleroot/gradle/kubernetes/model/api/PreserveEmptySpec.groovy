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
import org.gradle.model.Managed
import org.gradle.model.Model
import org.gradle.model.ModelSet
import org.gradle.model.RuleSource
import spock.lang.Specification

/**
 * Created by stefan on 09.02.17.
 */
class PreserveEmptySpec extends Specification implements GradleProjectTrait {
    GradleGenson serializer

    void setup() {
        serializer = new GradleGenson()

        project.allprojects {
            apply plugin: SimpleRuleSource
        }
    }

    SimpleObject getSimpleObject() {
        getFromModel('simple', SimpleObject)
    }

    ObjectWithProperty getObjectWithProperty() {
        getFromModel('objectWithProperty', ObjectWithProperty)
    }

    NormalParent getNormalParent() {
        getFromModel('normalParent', NormalParent)
    }

    ModelSet<SimpleObject> getPreserveSet() {
        getFromModel('preserveSet', ModelSet)
    }

    def "serialize empty"() {
        given: 'an empty object'

        when: 'serialized'
        String json = serializer.serialize(simpleObject)

        then: 'the serialized string is empty'

        json == ''
    }

    def "serialize empty preserved"() {
        given: 'an empty object with preserve equals true'
        project.allprojects {
            model {
                simple {
                    preserve = true
                }
            }
        }

        when: 'serialized'
        String json = serializer.serialize(simpleObject)

        then: 'the serialized string is an empty object'

        json == '{}'
    }

    def "serialize not empty object"() {
        given: 'an object with one property'
        project.allprojects {
            model {
                objectWithProperty {
                    property = 'Hello'
                }
            }
        }

        when: 'serialized'
        String json = serializer.serialize(objectWithProperty)

        then: 'the serialized string is an empty object'

        json == '{"property":"Hello"}'
    }

    def "serialize not empty object with preserve"() {
        given: 'an empty object with preserve equals true'
        project.allprojects {
            model {
                objectWithProperty {
                    preserve = true
                    property = 'Hello'
                }
            }
        }

        when: 'serialized'
        String json = serializer.serialize(objectWithProperty)

        then: 'the serialized string is an empty object'

        json == '{"property":"Hello"}'
    }

    def "serialize a parent with a preserved child"() {
        given: 'an empty object with preserve equals true'
        project.allprojects {
            model {
                normalParent {
                    child {
                        preserve = true
                    }
                }
            }
        }

        when: 'serialized'
        String json = serializer.serialize(normalParent)

        then: 'the serialized string is an empty object'

        json == '{"child":{}}'
    }

    def "serialize a model set with no preserve objects"() {
        given:
        project.allprojects {
            model {
                preserveSet.create {
                }
            }
        }

        when: 'serialized'
        String json = serializer.serialize(preserveSet)

        then: 'the serialized string is empty'

        json == ''
    }

    def "serialize a model set with one preserve object"() {
        given:
        project.allprojects {
            model {
                preserveSet {
                    create {
                        preserve = true
                    }
                }
            }
        }

        when: 'serialized'
        String json = serializer.serialize(preserveSet)

        then:

        json == '[{}]'
    }

    def "serialize a model set with multiple of both"() {
        given:
        project.allprojects {
            model {
                preserveSet {
                    create {
                        preserve = true
                    }
                    create {
                    }
                    create {
                    }
                    create {
                        preserve = true
                    }
                    create {
                    }
                }
            }
        }

        when: 'serialized'
        String json = serializer.serialize(preserveSet)

        then: 'a list with two empty objects'

        json == '[{},{}]'
    }

    @SuppressWarnings('GroovyUnusedDeclaration')
    static class SimpleRuleSource extends RuleSource {
        @SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter'])
        @Model
        void simple(SimpleObject simple) {
        }

        @SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter'])
        @Model
        void objectWithProperty(ObjectWithProperty objectWithProperty) {
        }

        @SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter'])
        @Model
        void normalParent(NormalParent normalParent) {
        }

        @SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter'])
        @Model
        void preserveSet(ModelSet<SimpleObject> simpleObjects) {
        }
    }

    @Managed
    interface SimpleObject extends PreserveOnEmptyAware {
    }

    @SuppressWarnings('GroovyUnusedDeclaration')
    @Managed
    interface ObjectWithProperty extends PreserveOnEmptyAware {
        String getProperty()

        void setProperty(String property)
    }

    @SuppressWarnings('GroovyUnusedDeclaration')
    @Managed
    interface NormalParent {
        SimpleObject getChild()
    }
}

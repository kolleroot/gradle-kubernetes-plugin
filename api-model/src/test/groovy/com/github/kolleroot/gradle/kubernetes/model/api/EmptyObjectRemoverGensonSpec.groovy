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
import com.owlike.genson.Context
import com.owlike.genson.Genson
import com.owlike.genson.stream.ObjectWriter
import org.gradle.model.Managed
import org.gradle.model.Model
import org.gradle.model.ModelSet
import org.gradle.model.RuleSource
import spock.lang.Specification

/**
 * Specify the {@link EmptyObjectRemoverGenson}
 */
class EmptyObjectRemoverGensonSpec extends Specification implements GradleProjectTrait {

    Genson genson

    void setup() {
        genson = new GradleGensonBuilder().withBundle(GradleManagedModelBundle.INSTANCE).create()

        project.allprojects {
            apply plugin: JsonTestRuleSource
        }
    }

    String serialize(Object o) {
        StringWriter sw = new StringWriter()
        ObjectWriter ow = new EmptyObjectRemoverGenson(sw, true, false, false)
        genson.serialize(o, o.getClass(), ow, new Context(genson))

        sw.toString()
    }

    OuterObject getOuterObject() {
        getFromModel('outer', OuterObject)
    }

    def "serialize empty"() {
        given: 'no configuration'

        when: 'serialized'
        String json = serialize(outerObject)

        then: 'the json matches an empty string'
        json == ''
    }

    def "serializes a simple object with an nested object"() {
        given: 'an object'
        project.allprojects {
            model {
                outer {
                    stringProperty = 'Hallo'
                    intProperty = 10
                    longProperty = 20L
                    shortProperty = 1
                    innerProperty {
                        stringProperty = 'Welt'
                    }
                }
            }
        }

        when: 'serialized'
        String json = serialize(outerObject)

        then: 'the json matches'
        json == '{"innerProperty":{"stringProperty":"Welt"},"intProperty":10,"longProperty":20,"shortProperty":1,' +
                '"stringProperty":"Hallo"}'
    }

    def "serializes a simple object with an empty nested object"() {
        given: 'an object'
        project.allprojects {
            model {
                outer {
                    stringProperty = 'Hallo'
                    intProperty = 10
                    longProperty = 20L
                    shortProperty = 1
                }
            }
        }

        when: 'serialized'
        String json = serialize(outerObject)

        then: 'the json matches'
        json == '{"intProperty":10,"longProperty":20,"shortProperty":1,"stringProperty":"Hallo"}'
    }

    @SuppressWarnings('NestedBlockDepth')
    def "serialize a deeply nested object"() {
        given: 'a deeply nested object structure'
        project.allprojects {
            apply plugin: JsonTestRuleSource

            model {
                outer {
                    deepNested {
                        nested {
                            nested {
                                nested {
                                    nested {
                                        stringProperty = 'Very deep inside'
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        when: 'serialized'
        String json = serialize(outerObject)

        then: 'the json matches'
        json == '{"deepNested":{"nested":{"nested":{"nested":{"nested":{"stringProperty":"Very deep inside"}}}}}}'
    }

    def "serialize a array with one element"() {
        given: 'an array in the model'
        project.allprojects {
            model {
                outer {
                    innerModels.create {
                        stringProperty = 'Pi'
                        doubleProperty = 3.14
                    }
                }
            }
        }

        when: 'serialized'
        String json = serialize(outerObject)

        then: 'the json matches'
        json == '{"innerModels":[{"doubleProperty":3.14,"stringProperty":"Pi"}]}'
    }

    def "serialize a array with three element"() {
        given: 'an array in the model'
        project.allprojects {
            model {
                outer {
                    innerModels.create {
                        stringProperty = 'Pi'
                        doubleProperty = 3.14
                    }
                    innerModels.create {
                        stringProperty = 'E'
                        doubleProperty = 2.72
                    }
                    innerModels.create {
                        stringProperty = 'sqrt 2'
                        doubleProperty = 1.41
                    }
                }
            }
        }

        when: 'serialized'
        String json = serialize(outerObject)

        then: 'the json matches'
        json == '{"innerModels":[{"doubleProperty":3.14,"stringProperty":"Pi"},{"doubleProperty":2.72,' +
                '"stringProperty":"E"},{"doubleProperty":1.41,"stringProperty":"sqrt 2"}]}'
    }

    def "serialize a list of primitive strings"() {
        given: 'a list of primitives'
        project.allprojects {
            model {
                outer {
                    stringValues = [
                            'Rabbit',
                            'Alice',
                            'Hatter',
                    ]
                }
            }
        }

        when: 'serialized'
        String json = serialize(outerObject)

        then: 'the json matches'
        json == '{"stringValues":["Rabbit","Alice","Hatter"]}'
    }

    def "serialize a list of primitive ints"() {
        given: 'a list of primitives'
        project.allprojects {
            model {
                outer {
                    integerValues = [
                            1,
                            1,
                            2,
                            3,
                            5,
                            8,
                            13,
                            21,
                            34,
                    ]
                }
            }
        }

        when: 'serialized'
        String json = serialize(outerObject)

        then: 'the json matches'
        json == '{"integerValues":[1,1,2,3,5,8,13,21,34]}'
    }

    @SuppressWarnings('GroovyUnusedDeclaration')
    static class JsonTestRuleSource extends RuleSource {
        @SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter'])
        @Model
        void outer(OuterObject outerObject) {
        }
    }

    @SuppressWarnings('GroovyUnusedDeclaration')
    interface BaseObject {
        String getStringProperty()

        void setStringProperty(String value)

        int getIntProperty()

        void setIntProperty(int value)

        long getLongProperty()

        void setLongProperty(long value)

        short getShortProperty()

        void setShortProperty(short value)

        double getDoubleProperty()

        void setDoubleProperty(double value)

        float getFloatProperty()

        void setFloatProperty(float value)

        boolean getBooleanProperty()

        void setBooleanProperty(boolean value)
    }

    @Managed
    @SuppressWarnings('GroovyUnusedDeclaration')
    interface OuterObject extends BaseObject {

        InnerObject getInnerProperty()

        DeepNestedObject1 getDeepNested()

        ModelSet<InnerObject> getInnerModels()

        List<String> getStringValues()

        void setStringValues(List<String> values)

        List<Integer> getIntegerValues()

        void setIntegerValues(List<Integer> values)
    }

    @Managed
    interface InnerObject extends BaseObject {
    }

    @Managed
    @SuppressWarnings('GroovyUnusedDeclaration')
    interface DeepNestedObject1 extends BaseObject {
        DeepNestedObject2 getNested()
    }

    @Managed
    @SuppressWarnings('GroovyUnusedDeclaration')
    interface DeepNestedObject2 extends BaseObject {
        DeepNestedObject3 getNested()
    }

    @Managed
    @SuppressWarnings('GroovyUnusedDeclaration')
    interface DeepNestedObject3 extends BaseObject {
        DeepNestedObject4 getNested()
    }

    @Managed
    @SuppressWarnings('GroovyUnusedDeclaration')
    interface DeepNestedObject4 extends BaseObject {
        DeepNestedObject5 getNested()
    }

    @Managed
    interface DeepNestedObject5 extends BaseObject {
    }
}

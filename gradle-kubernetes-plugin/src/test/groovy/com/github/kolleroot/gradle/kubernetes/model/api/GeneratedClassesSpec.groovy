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

import org.gradle.model.Managed
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import spock.lang.Shared
import spock.lang.Specification

/**
 * Verify the generated kubernetes classes
 */
class GeneratedClassesSpec extends Specification {

    @Shared
    Set<Class<?>> classes

    @Shared
    Set<Class<?>> topLevelApiObjectClases

    Set<String> baseTypes = ['IntstrIntOrString',
                             'ResourceQuantity',
                             'RuntimeRawExtension',
                             'VersionInfo',
                             'VersionedEvent',]

    def setupSpec() {
        def scanner = new Reflections(
                'com.github.kolleroot.gradle.kubernetes.model.api.generated',
                new TypeAnnotationsScanner(),
                new SubTypesScanner(false))

        classes = ReflectionUtils.forNames(scanner.allTypes)
        topLevelApiObjectClases = scanner.getSubTypesOf(TopLevelApiObject)
    }

    def "there are interfaces in the 'generated' package"() {
        expect: 'that there are classes in the package'
        !classes.empty
    }

    def "there are top level api objects"() {
        expect:
        !topLevelApiObjectClases.empty
    }

    def "#c.simpleName is marked with @Managed"() {
        expect: 'they have the managed annotation'
        c.getAnnotation(Managed) != null

        where:
        c << classes
    }

    def "the interface #c.simpleName matches the naming conventions"() {
        given:
        def simpleName = c.simpleName

        expect: 'all interface names consist of an api group and the object kind'
        if (!baseTypes.contains(simpleName)) {
            def parts = simpleName.split('(?<=[a-z0-9])(?=[A-Z])')
            assert parts.size() >= 2

            assert parts[0].matches('V[1-9](?:[a-z]+[1-9])?') || parts[0] == 'Unversioned'
        }
        where:
        c << classes
    }

    def "top level api object #c.simpleName has properties apiVersion and kind"() {
        expect: 'there are getter and setter methods for the apiVersion'
        c.getMethod('getApiVersion') != null
        c.getMethod('setApiVersion', String) != null

        c.getMethod('getKind') != null
        c.getMethod('setKind', String) != null

        where:
        c << topLevelApiObjectClases
    }
}

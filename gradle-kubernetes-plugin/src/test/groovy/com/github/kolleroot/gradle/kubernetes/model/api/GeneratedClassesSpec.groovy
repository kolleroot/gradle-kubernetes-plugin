package com.github.kolleroot.gradle.kubernetes.model.api

import org.gradle.model.Managed
import org.reflections.Reflections
import spock.lang.Specification

/**
 * Verify the generated kubernetes classes
 */
class GeneratedClassesSpec extends Specification {
    def "all the interfaces are marked with @Managed"() {
        given: 'all the generated classes'
        Set<Class<?>> classes = new Reflections('com.github.kolleroot.gradle.kubernetes.model.api.generated')

        expect: 'there exist classes in the package'
        !classes.empty

        and: 'they have the managed annotation'
        classes.each { c ->
            c.annotations.contains(Managed)
        }
    }
}

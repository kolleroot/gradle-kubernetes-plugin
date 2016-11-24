package com.github.kolleroot.gradle.kubernetes.internal

import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.plugins.ear.EarPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

/**
 * Test the predicates if the recognise their plugins
 */
/*
class ProjectPredicatesTest extends Specification {

    @Shared
    Project project;

    def setup() {
        project = ProjectBuilder.builder().build();
    }

    def "the application predicate recognises the application plugin"() {
        given:
        project.apply plugin: ApplicationPlugin

        when:
        def result = new ApplicationProjectPredicate().test(project)

        then:
        result
    }

    def "the war predicate recognises the war plugin"() {
        given:
        project.apply plugin: WarPlugin

        when:
        def result = new WarProjectPredicate().test(project)

        then:
        result
    }

    def "the ear predicate recognises the ear plugin"() {
        given:
        project.apply plugin: EarPlugin

        when:
        def result = new EarProjectPredicate().test(project)

        then:
        result
    }
}*/

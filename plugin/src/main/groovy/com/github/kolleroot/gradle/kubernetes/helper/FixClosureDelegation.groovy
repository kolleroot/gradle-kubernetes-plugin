package com.github.kolleroot.gradle.kubernetes.helper

import org.codehaus.groovy.runtime.ConvertedClosure

/**
 * Created by stefan on 12.12.16.
 */
class FixClosureDelegation {
    static void fixClosure(Object o) {
        Object originalSpec = o

        if (originalSpec.hasProperty('h')) {
            originalSpec = originalSpec.h
        }

        Closure spec
        if (originalSpec instanceof Closure) {
            spec = originalSpec as Closure
        } else if (originalSpec instanceof ConvertedClosure) {
            ConvertedClosure convertedSpec = originalSpec as ConvertedClosure
            spec = convertedSpec.delegate as Closure
        }

        if (spec != null) {
            spec.resolveStrategy = Closure.DELEGATE_FIRST
        }
    }
}

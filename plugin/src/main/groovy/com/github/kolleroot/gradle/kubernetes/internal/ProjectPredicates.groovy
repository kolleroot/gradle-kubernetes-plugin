package com.github.kolleroot.gradle.kubernetes.internal

import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.plugins.ear.EarPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Predicate

abstract class AbstractProjectPredicate implements Predicate<Project> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractProjectPredicate.class);

    protected final String name;
    protected final Class<?> pluginClass;

    protected AbstractProjectPredicate(String name, Class<?> pluginClass) {
        this.name = name;
        this.pluginClass = pluginClass;
    }

    @Override
    boolean test(Project project) {
        project.plugins.any { p ->
            if (pluginClass.isInstance(p)) {
                logger.trace "Found the ${name} plugin in ${project.name}"
                true
            } else {
                false
            }
        }
    }
}

/**
 * Check if the given project is a jar application
 */
class ApplicationProjectPredicate extends AbstractProjectPredicate {
    public ApplicationProjectPredicate() {
        super("application", ApplicationPlugin);
    }
}

/**
 * Check if the given project is a war project
 */
class WarProjectPredicate extends AbstractProjectPredicate {
    public WarProjectPredicate() {
        super("war", WarPlugin);
    }
}

/**
 * Check if the given project is a ear project
 */
class EarProjectPredicate extends AbstractProjectPredicate {
    public EarProjectPredicate() {
        super("ear", EarPlugin);
    }
}

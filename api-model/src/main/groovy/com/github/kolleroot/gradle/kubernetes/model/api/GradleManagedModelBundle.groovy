package com.github.kolleroot.gradle.kubernetes.model.api

import com.owlike.genson.GensonBuilder
import com.owlike.genson.ext.GensonBundle
import org.gradle.model.internal.core.MutableModelNode
import org.gradle.model.internal.type.ModelType

/**
 * Prepare the GensonBuilder for serializing managed gradle models
 */
class GradleManagedModelBundle extends GensonBundle {
    static final GensonBundle INSTANCE = new GradleManagedModelBundle()

    private GradleManagedModelBundle() {

    }

    @Override
    void configure(GensonBuilder builder) {
        builder.exclude(MutableModelNode)
                .exclude(ModelType)
                .useRuntimeType(true)
                .setSkipNull(true)
    }
}

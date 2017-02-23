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
package com.github.kolleroot.gradle.kubernetes.model.serializer

import com.github.kolleroot.gradle.kubernetes.model.api.PreserveOnEmptyAware
import com.owlike.genson.Context
import com.owlike.genson.Converter
import com.owlike.genson.Genson
import com.owlike.genson.GensonBuilder
import com.owlike.genson.Wrapper
import com.owlike.genson.convert.ContextualFactory
import com.owlike.genson.ext.GensonBundle
import com.owlike.genson.reflect.BeanProperty
import com.owlike.genson.reflect.RuntimePropertyFilter
import com.owlike.genson.stream.ObjectReader
import com.owlike.genson.stream.ObjectWriter
import org.gradle.api.Named
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
        builder.setSkipNull(true)
                .exclude(MutableModelNode)
                .exclude(ModelType)
        // remove the name property, if it exists inside {@link Named}
                .useRuntimePropertyFilter(NamedNamePropertyFilter.INSTANCE)
        // preserve all the empty objects, if they are marked
                .withContextualFactory(new PreserveContextualFactory())
                .useRuntimeType(true)
    }
}

/**
 * This property filter ignores the property {@code name} if the class implements the {@link Named} interface.
 *
 * {@link com.owlike.genson.annotation.JsonIgnore} doesn't seem to work, if you overwrite the property and add it to
 * the overwritten implementation.
 */
class NamedNamePropertyFilter extends Wrapper implements RuntimePropertyFilter {

    public static final NamedNamePropertyFilter INSTANCE = new NamedNamePropertyFilter()

    private NamedNamePropertyFilter() {
    }

    @Override
    boolean shouldInclude(BeanProperty property, Context ctx) {
        boolean include = true
        include &= !(property.name == 'name' && Named.isAssignableFrom(property.concreteClass))
        include
    }
}

/**
 * This contextual factory returns a new {@link PreserveConverter} if the property is {@code 'preserve'} from
 * {@link PreserveOnEmptyAware}.
 */
class PreserveContextualFactory implements ContextualFactory<Boolean> {

    @Override
    Converter<Boolean> create(BeanProperty property, Genson genson) {
        if (PreserveOnEmptyAware.isAssignableFrom(property.concreteClass) &&
                property.name == 'preserve') {
            new PreserveConverter()
        } else {
            null
        }
    }
}

/**
 * This converter realizes the current object stack if the boolean property is true.
 *
 * If the underlying {@link ObjectWriter} has not set {@code skipNull} to {@code true},
 * preserve will be written as null.
 */
class PreserveConverter implements Converter<Boolean> {

    @Override
    void serialize(Boolean object, ObjectWriter writer, Context ctx) throws Exception {
        if (object && EmptyObjectRemoverJsonWriter.isAssignableFrom(writer.class)) {
            ((EmptyObjectRemoverJsonWriter) writer).realizeAllEmptyObjects()
        }
        writer.writeNull()
    }

    @Override
    Boolean deserialize(ObjectReader reader, Context ctx) throws Exception {
        true
    }
}

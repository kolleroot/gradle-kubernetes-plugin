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

import com.owlike.genson.Context
import com.owlike.genson.Converter
import com.owlike.genson.Factory
import com.owlike.genson.Genson
import com.owlike.genson.GensonBuilder
import com.owlike.genson.convert.DefaultConverters
import com.owlike.genson.stream.ObjectWriter

/**
 * A json serializer for gradle managed models
 */
class GradleGenson {
    private final Genson genson

    GradleGenson() {
        this.genson = new GradleGensonBuilder().create()
    }

    /**
     * Serialize an object to string
     * @param o to object to serialize
     * @return the string representation
     */
    String serialize(Object o) {
        StringWriter sw = new StringWriter()
        serialize(o, sw)
        sw.toString()
    }

    /**
     * Serialize an object {@code o} to the writer {@code w}
     * @param o the object to serialize
     * @param w the writer to write to
     */
    void serialize(Object o, Writer w) {
        ObjectWriter objectWriter = new EmptyObjectRemoverJsonWriter(w, genson.skipNull, genson.htmlSafe, false)
        genson.serialize(o, o.getClass(), objectWriter, new Context(genson))
    }
}

/**
 * Override the default factories
 */
class GradleGensonBuilder extends GensonBuilder {

    GradleGensonBuilder() {
        this.withBundle(GradleManagedModelBundle.INSTANCE)
    }

    @Override
    protected void addDefaultConverterFactories(List<Factory<? extends Converter<?>>> factories) {
        factories.with {
            add(DefaultConverters.ArrayConverterFactory.instance)
            //factories.add(DefaultConverters.CollectionConverterFactory.instance)
            add(NotEmptyCollectionConverterFactory.INSTANCE)
            add(DefaultConverters.MapConverterFactory.instance)
            add(DefaultConverters.EnumConverterFactory.instance)
            add(DefaultConverters.PrimitiveConverterFactory.instance)
            add(DefaultConverters.UntypedConverterFactory.instance)
        }
    }
}

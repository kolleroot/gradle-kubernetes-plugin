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

import com.owlike.genson.Context
import com.owlike.genson.Converter
import com.owlike.genson.Factory
import com.owlike.genson.Genson
import com.owlike.genson.convert.DefaultConverters
import com.owlike.genson.stream.ObjectReader
import com.owlike.genson.stream.ObjectWriter

import java.lang.reflect.Type

/**
 * A Converter factory that wraps the default collection factory
 */
class NotEmptyCollectionConverterFactory implements Factory<Converter<Collection<?>>> {

    public final static NotEmptyCollectionConverterFactory INSTANCE = new NotEmptyCollectionConverterFactory()

    private NotEmptyCollectionConverterFactory() {
    }

    @SuppressWarnings(['rawtypes', 'unchecked'])
    @Override
    Converter<Collection<?>> create(Type forType, Genson genson) {
        new NotEmptyCollectionConverter<>(
                DefaultConverters.CollectionConverterFactory.instance.create(forType, genson)
        )
    }
}

/**
 * If the Collection has size zero, return null instead of an empty list.
 * @param < E >  the type of the collection
 */
class NotEmptyCollectionConverter<E> implements Converter<Collection<E>> {

    private final Converter<Collection<E>> delegate

    NotEmptyCollectionConverter(Converter<Collection<E>> delegate) {
        this.delegate = delegate
    }

    @Override
    void serialize(Collection<E> object, ObjectWriter writer, Context ctx) throws Exception {
        if (object.size() == 0) {
            writer.writeNull()
        } else {
            delegate.serialize(object, writer, ctx)
        }
    }

    @Override
    Collection<E> deserialize(ObjectReader reader, Context ctx) throws Exception {
        delegate.deserialize(reader, ctx)
    }
}

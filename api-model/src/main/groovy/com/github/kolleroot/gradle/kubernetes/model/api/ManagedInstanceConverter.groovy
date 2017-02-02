package com.github.kolleroot.gradle.kubernetes.model.api

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

    public final
    static NotEmptyCollectionConverterFactory instance = new NotEmptyCollectionConverterFactory();

    private NotEmptyCollectionConverterFactory() {
    }

    @SuppressWarnings(['rawtypes', 'unchecked'])
    Converter<Collection<?>> create(Type forType, Genson genson) {
        return new NotEmptyCollectionConverter<>(
                DefaultConverters.CollectionConverterFactory.instance.create(forType, genson)
        )
    }
}

/**
 * If the Collection has size zero, return null instead of an empty list.
 * @param < E >  the type of the collection
 */
class NotEmptyCollectionConverter<E> implements Converter<Collection<E>> {

    private Converter<Collection<E>> delegate

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
        return delegate.deserialize(reader, ctx)
    }
}

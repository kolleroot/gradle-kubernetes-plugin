package com.github.kolleroot.gradle.kubernetes.model.api

import com.owlike.genson.Converter
import com.owlike.genson.Factory
import com.owlike.genson.GensonBuilder
import com.owlike.genson.convert.DefaultConverters

/**
 * Override the default factories
 */
class GradleGensonBuilder extends GensonBuilder {
    @Override
    protected void addDefaultConverterFactories(List<Factory<? extends Converter<?>>> factories) {
        factories.add(DefaultConverters.ArrayConverterFactory.instance)
        //factories.add(DefaultConverters.CollectionConverterFactory.instance)
        factories.add(NotEmptyCollectionConverterFactory.instance)
        factories.add(DefaultConverters.MapConverterFactory.instance)
        factories.add(DefaultConverters.EnumConverterFactory.instance)
        factories.add(DefaultConverters.PrimitiveConverterFactory.instance)
        factories.add(DefaultConverters.UntypedConverterFactory.instance)
    }
}

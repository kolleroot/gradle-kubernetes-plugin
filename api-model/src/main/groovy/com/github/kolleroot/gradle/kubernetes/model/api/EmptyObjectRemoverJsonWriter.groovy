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

import com.owlike.genson.stream.JsonType
import com.owlike.genson.stream.JsonWriter
import com.owlike.genson.stream.ObjectWriter

/**
 * Extend the {@link JsonWriter} to remove empty objects and lists.
 *
 * This is done by tracking the current state of the writer. When the first non null value is written, all the
 * encapsulating objects and arrays are written.
 */
class EmptyObjectRemoverJsonWriter extends JsonWriter {

    /**
     * A structure to track the current state of the json
     */
    protected static class ObjectState {
        /**
         * The name of the object or {@code null}, if it has no name.
         *
         * This is the case, when the outer object is an array or there is no outer object.
         */
        private final char[] propertyName

        /**
         * The type of the inner object
         */
        private final JsonType objectType

        /**
         * Whether there were any properties written to this object.
         *
         * This property will be updated after this object is written.
         */
        private boolean empty

        ObjectState(char[] propertyName, JsonType objectType) {
            this.propertyName = propertyName
            this.objectType = objectType
            this.empty = true
        }

        char[] getPropertyName() {
            propertyName
        }

        JsonType getObjectType() {
            objectType
        }

        boolean isEmpty() {
            empty
        }

        void setEmpty(boolean empty) {
            this.empty = empty
        }
    }

    /**
     * A stack to track the current nesting state of the object.
     *
     * This should be equal to {@code _ctx} in {@link JsonWriter} but this property is private.
     */
    protected Deque<ObjectState> objectStateContext = new ArrayDeque<>(10)

    /**
     * The name of the current property in escaped {@code char[]} format.
     *
     * This should be equal to {@code _name} in {@link JsonWriter} but this property is private.
     */
    protected char[] currentName = null

    EmptyObjectRemoverJsonWriter(Writer writer, final boolean skipNull, final boolean htmlSafe,
                                 boolean indentation) {
        super(writer, skipNull, htmlSafe, indentation)

        objectStateContext.push(new ObjectState(null, JsonType.EMPTY))
    }

    protected ObjectState getCurrentObjectState() {
        objectStateContext.peek()
    }

    /*
     * Track the current state of the property name
     */

    @Override
    JsonWriter writeName(String name) {
        this.currentName = escapeString(name)

        super.writeName(name)
    }

    @Override
    ObjectWriter writeEscapedName(char[] name) {
        this.currentName = name

        super.writeEscapedName(name)
    }

    @Override
    ObjectWriter writeNull() {
        this.currentName = null

        super.writeNull()
    }

    /*
     * Track the object and array nesting
     */

    @Override
    JsonWriter beginArray() {
        objectStateContext.push(
                new ObjectState(
                        this.currentName,
                        JsonType.ARRAY
                )
        )

        // rest the current property name
        this.currentName = null

        this
    }

    @Override
    JsonWriter beginObject() {
        objectStateContext.push(
                new ObjectState(
                        this.currentName,
                        JsonType.OBJECT
                )
        )

        // rest the current property name
        this.currentName = null

        this
    }

    @Override
    JsonWriter endArray() {
        ObjectState state = objectStateContext.pop()

        if (!state.isEmpty()) {
            super.endArray()
        }

        this.currentName = null

        this
    }

    @Override
    JsonWriter endObject() {
        ObjectState state = objectStateContext.pop()

        if (!state.isEmpty()) {
            super.endObject()
        }

        this.currentName = null

        this
    }

    /**
     * Make all the encapsulating objects and arrays real.
     */
    void realizeAllEmptyObjects() {
        // break if the current object is already written
        if (!currentObjectState.empty) {
            return
        }

        // backup the property name to restore it at the end of this function
        char[] backupPropertyName = currentName
        // clear the property name in the {@link JsonWriter} class
        super.writeNull()

        // iterate over all the encapsulating objects in reverse order and, if the don't exist, make them real
        for (ObjectState state : objectStateContext.descendingIterator()) {
            if (!state.isEmpty()) {
                continue
            }

            // set the name of the current object
            if (state.propertyName != null) {
                super.writeEscapedName(state.propertyName)
            }

            // create the object
            if (state.objectType == JsonType.OBJECT) {
                super.beginObject()
            } else if (state.objectType == JsonType.ARRAY) {
                super.beginArray()
            }

            // mark it as real
            state.setEmpty(false)
        }

        // restore the property name
        super.writeEscapedName(backupPropertyName)
    }

    @Override
    JsonWriter writeValue(int value) {
        if (value == 0) {
            writeNull()
        } else {
            realizeAllEmptyObjects()
            super.writeValue(value)
        }

        this
    }

    @Override
    JsonWriter writeValue(double value) {
        if (value == 0D) {
            writeNull()
        } else {
            realizeAllEmptyObjects()
            super.writeValue(value)
        }

        this
    }

    @Override
    JsonWriter writeValue(long value) {
        if (value == 0L) {
            writeNull()
        } else {
            realizeAllEmptyObjects()
            super.writeValue(value)
        }

        this
    }

    @SuppressWarnings(['GrEqualsBetweenInconvertibleTypes', 'GroovyAssignabilityCheck'])
    @Override
    ObjectWriter writeValue(short value) {
        if (value == 0) {
            writeNull()
        } else {
            realizeAllEmptyObjects()
            super.writeValue(value)
        }

        this
    }

    @Override
    ObjectWriter writeValue(float value) {
        if (value == 0f) {
            writeNull()
        } else {
            realizeAllEmptyObjects()
            super.writeValue(value)
        }

        this
    }

    @Override
    JsonWriter writeValue(boolean value) {
        if (value) {
            realizeAllEmptyObjects()
            super.writeValue(value)
        } else {
            writeNull()
        }

        this
    }

    @Override
    JsonWriter writeValue(Number value) {
        realizeAllEmptyObjects()
        super.writeValue(value)
    }

    @Override
    ObjectWriter writeValue(byte[] value) {
        if (value.length == 0) {
            writeNull()
        } else {
            realizeAllEmptyObjects()
            super.writeValue(value)
        }

        this
    }

    @Override
    JsonWriter writeUnsafeValue(String value) {
        realizeAllEmptyObjects()
        super.writeUnsafeValue(value)
    }

    @Override
    JsonWriter writeValue(String value) {
        realizeAllEmptyObjects()
        super.writeValue(value)
    }
}

/**
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
package com.github.kolleroot.gradle.kubernetes.model.api;

import com.owlike.genson.annotation.JsonIgnore;

/**
 * If {@code preserve} is true, this object should not collapsed if empty when serialized.
 */
public interface PreserveOnEmptyAware {
    /**
     * If the object should be preserved in the event, that all the other properties are empty.
     * <p>
     * This property will not be serialized.
     *
     * @return
     */
    @JsonIgnore
    Boolean getPreserve();

    void setPreserve(Boolean preserve);
}

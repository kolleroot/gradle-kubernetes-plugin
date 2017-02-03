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
package com.github.kolleroot.gradle.kubernetes.helper

import java.util.concurrent.atomic.AtomicInteger

/**
 * A unique COUNTER for file bundles in docker images.
 *
 * This helper provides a threadsafe counter which should be used for enumerating file bundles to provide unique names.
 */
final class DockerImageFileBundleCounter {
    private DockerImageFileBundleCounter() {
    }

    private static final AtomicInteger COUNTER = new AtomicInteger()

    /**
     * This method is only used for testing.
     * @return the current counter value
     */
    static int getCurrent() {
        COUNTER.get()
    }

    /**
     * Get the next unique number
     * @return the next number
     */
    @SuppressWarnings('UnnecessaryGetter')
    static int getNextUnique() {
        COUNTER.getAndIncrement()
    }
}

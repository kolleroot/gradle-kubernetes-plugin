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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by stefan on 16.01.17.
 */
class PortForwardRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortForwardRegistry)

    private static final Map<String, Closeable> CLOSEABLES = [:]

    static void add(String id, Closeable closeable) {
        if (CLOSEABLES.containsKey(id)) {
            throw new IllegalStateException('This closeable is already in the registry')
        }

        CLOSEABLES.put(id, closeable)
    }

    static void close(String id) {
        Closeable closeable = CLOSEABLES.get(id)

        if (closeable != null) {
            closeable.close()
        } else {
            LOGGER.warn('There is no closeable with id {}', id)
        }
    }

    static boolean isEmpty() {
        CLOSEABLES.size() == 0
    }
}
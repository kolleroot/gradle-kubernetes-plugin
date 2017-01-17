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

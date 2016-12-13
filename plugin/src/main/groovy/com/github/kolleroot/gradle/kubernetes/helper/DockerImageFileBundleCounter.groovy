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

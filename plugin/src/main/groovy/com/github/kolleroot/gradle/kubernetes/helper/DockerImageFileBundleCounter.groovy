package com.github.kolleroot.gradle.kubernetes.helper

import java.util.concurrent.atomic.AtomicInteger

/**
 * A unique COUNTER for for file bundles in docker images.
 */
final class DockerImageFileBundleCounter {
    private DockerImageFileBundleCounter() {
    }

    private static final AtomicInteger COUNTER = new AtomicInteger()

    static int getCurrent() {
        COUNTER.get()
    }

    @SuppressWarnings('UnnecessaryGetter')
    static int getNextUnique() {
        COUNTER.getAndIncrement()
    }
}

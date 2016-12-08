package com.github.kolleroot.gradle.kubernetes.helper

import java.util.concurrent.atomic.AtomicInteger

/**
 * A unique counter for for file bundles in docker images.
 */
final class DockerImageFileBundleCounter {
    private DockerImageFileBundleCounter() {
    }

    private static final AtomicInteger counter = new AtomicInteger()

    static getNextUnique() {
        counter.getAndIncrement()
    }
}

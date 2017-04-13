package com.github.kolleroot.gradle.kubernetes.testbase.internal

import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.command.LogContainerResultCallback

/**
 * Convert the Docker log into a string
 *
 * From the https://github.com/docker-java/docker-java test sources licensed under Apache 2.0
 */
class LogContainerStringCallback extends LogContainerResultCallback {
    protected final StringBuffer log = new StringBuffer();

    List<Frame> collectedFrames = new ArrayList<Frame>();

    boolean collectFrames = false;

    LogContainerStringCallback() {
        this(false);
    }

    LogContainerStringCallback(boolean collectFrames) {
        this.collectFrames = collectFrames;
    }

    @Override
    void onNext(Frame frame) {
        if (collectFrames) collectedFrames.add(frame);
        log.append(new String(frame.getPayload()));
    }

    @Override
    String toString() {
        return log.toString();
    }


    List<Frame> getCollectedFrames() {
        return collectedFrames;
    }
}

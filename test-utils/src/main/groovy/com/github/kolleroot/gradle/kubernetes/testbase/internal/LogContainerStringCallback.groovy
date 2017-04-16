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

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
package com.github.kolleroot.gradle.kubernetes.task

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 *  Forward a port from a pod in the cluster to localhost.
 *
 *  This implementation uses the kubectl command, because the fabric8 library doesn't support this feature currently
 *  but there is an open issue.
 *
 * @see <a href="https://github.com/fabric8io/kubernetes-client/issues/534" target="_blank">https://github
 *  .com/fabric8io/kubernetes-client/issues/534</a>
 */
class KubectlPortForwarder implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KubectlPortForwarder)

    /**
     * The kubernetes default namespace
     */
    public static final String DEFAULT_NAMESPACE = 'default'

    /**
     * The kubectl port forward command with placeholders for namespace, pod name and port
     */
    public static final String CMD_BASE = 'kubectl port-forward --namespace=%s %s %s'

    private final String namespace
    private final String pod
    private final String port

    private final Process process

    /**
     * Forward a port via the kubectl command.
     *
     * @param pod the name of the pod
     * @param port a port in the format "local:remote"
     */
    KubectlPortForwarder(String pod, String port) {
        this(DEFAULT_NAMESPACE, pod, port)
    }

    /**
     * Forward a port via the kubectl command.
     *
     * @param namespace the namespace of the pod
     * @param pod the name of the pod
     * @param port a port in the format "local:remote"
     */
    KubectlPortForwarder(String namespace, String pod, String port) {
        this.namespace = namespace
        this.pod = pod
        this.port = port

        LOGGER.debug String.format(CMD_BASE, namespace, pod, port)

        process = new ProcessBuilder(String.format(CMD_BASE, namespace, pod, port).split())
                .start()
        waitTillReady()
    }

    /**
     * Wait until {@code kubectl port-forward} has written at least one line to stdout.
     */
    private void waitTillReady() {
        if (process.alive) {
            // CAREFUL: don't close the inputstream because kubectl will die because of SIGPIPE (exit code 141)
            process.inputStream.newReader().with { reader ->
                LOGGER.warn reader.readLine()
            }

            final CountDownLatch LATCH = new CountDownLatch(1)
            final CountDownLatch ERR = new CountDownLatch(1)

            Thread.start countdownWhenLineRead(LATCH, process.in)
            Thread.start countdownWhenLineRead(LATCH, process.err, ERR)

            LATCH.await(120, TimeUnit.SECONDS)
            ERR.await(1, TimeUnit.SECONDS)

            if (LATCH.count != 0 || ERR.count == 0 || !process.alive) {
                process.destroy()
                if(LATCH.count != 0) {
                    throw new IllegalStateException('Unable to start the the kubectl process for port forwarding: TIMEOUT')
                } else {
                    throw new IllegalStateException('Unable to start the the kubectl process for port forwarding: PROCESS ALREADY DEAD')
                }
            }
        }
    }

    private static Closure<?> countdownWhenLineRead(
            final CountDownLatch latch, final InputStream stream, final CountDownLatch otherLatch = null) {
        return {
            stream.newReader().with { reader ->
                try {
                    LOGGER.warn reader.readLine()
                    otherLatch?.countDown()
                    latch.countDown()
                } catch (IOException e) {
                    // don't cate at the moment
                    LOGGER.trace 'error while reading', e
                }
            }
        }
    }

    @Override
    void close() {
        if (process.alive) {
            process.destroy()
            // int exit = process.exitValue()
            // TODO decide if an exception should be thrown if the exit value != 0
        }
    }
}

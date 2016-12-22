package com.github.kolleroot.gradle.kubernetes.testbase

/**
 * Utility functions for docker
 */
class DockerHelper {
    private static final String UNIX_PRIFIX = 'unix://'
    private static final String UNIX_URI = '/var/run/docker.sock'

    private static final String HTTP_URL = 'http://localhost:2375'

    private static String dockerUrl

    /**
     * Figure out a way to connect to a local docker daemon
     *
     * First check, if there is a unix socket at {@code /var/run/docker.sock},
     * then check the local default HTTP port.
     *
     * @return an uri to the docker daemon or {@code null}
     */
    static String getUrl() {
        if (dockerUrl == null) {
            // check if there is an unix socket
            if (new File(UNIX_URI).exists()) {
                dockerUrl = UNIX_PRIFIX + UNIX_URI
            } else if (isPortOpen(HTTP_URL)) {
                dockerUrl = HTTP_URL
            }
        }

        dockerUrl
    }

    /**
     * Some hack to check if the url can be connected to
     *
     * @param url
     * @return if the url is accessible
     */
    private static boolean isPortOpen(String url) {
        try {
            URLConnection connection = new URL(url).openConnection()
            connection.connect()
        } catch (ignored) {
            false
        }

        true
    }
}

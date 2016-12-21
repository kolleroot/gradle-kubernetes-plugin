package com.github.kolleroot.gradle.kubernetes.testbase

import java.util.zip.ZipFile

/**
 * Some helper functions for handling zip files
 */
class ZipFileHelper {
    /**
     * Converts a zip file to a map
     *
     * Each file in the zip file in an entry in the map. The key of the map is the file name, the value is the file
     * contents.
     * @param file the file object of the zip file
     * @return the zip file as map
     */
    static Map<String, String> toMap(File file) {
        Map<String, String> map = [:]

        ZipFile zip = new ZipFile(file)

        zip.entries().each { entry ->
            if (!entry.isDirectory()) {
                map.put(entry.name, zip.getInputStream(entry).readLines().join('\n'))
            }
        }

        map
    }
}

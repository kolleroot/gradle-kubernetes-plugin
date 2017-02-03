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

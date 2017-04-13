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

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.io.IOUtils

/**
 * Some helper functions for handling zip files
 */
class ArchiveHelper {

    /**
     * Converts an archive into a map in which one entry represents a file in the archive.
     *
     * @param file the archive file
     * @return a map with all the files
     */
    static Map<String, String> toMap(File file) {
        ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(file.newInputStream())

        Map<String, String> map = [:]

        while (true) {
            ArchiveEntry entry = archiveInputStream.nextEntry
            if (entry == null) {
                break
            }

            if (!entry.directory) {
                byte[] content = new byte[entry.size]

                IOUtils.readFully(archiveInputStream, content)
                map.put(entry.name, new String(content))
            }
        }

        map
    }
}

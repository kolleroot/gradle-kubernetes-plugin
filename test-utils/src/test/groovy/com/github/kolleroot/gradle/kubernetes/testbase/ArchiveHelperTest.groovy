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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.charset.StandardCharsets

/**
 * Created by stefan on 11.04.17.
 */
class ArchiveHelperTest extends Specification {

    @Rule
    TemporaryFolder tmpFoler = new TemporaryFolder()

    private static byte[] toBytes(String s) {
        s.getBytes(StandardCharsets.UTF_8)
    }

    private static void createTarEntry(TarArchiveOutputStream os, String filename, String content) {
        def test1Data = toBytes(content)
        def archiveEntry = new TarArchiveEntry(filename)
        archiveEntry.size = test1Data.size()
        os.putArchiveEntry(archiveEntry)
        os.write(test1Data)
        os.closeArchiveEntry()
    }


    private static void createZipEntry(ZipArchiveOutputStream os, String filename, String content) {
        def test1Data = toBytes(content)
        def archiveEntry = new ZipArchiveEntry(filename)
        archiveEntry.size = test1Data.size()
        os.putArchiveEntry(archiveEntry)
        os.write(test1Data)
        os.closeArchiveEntry()
    }

    def "TarToMapFilesOnly"() {
        given: 'a tar file'
        File tarFile = tmpFoler.newFile('test.tar')

        tarFile.withOutputStream { fileOs ->
            TarArchiveOutputStream os = new TarArchiveOutputStream(fileOs)

            (1..10).each {
                createTarEntry(os, "test${it}.txt", "Lorem Ipsum ${it}")
            }

            os.close()
        }

        when: 'the the helper method created the map'
        def map = ArchiveHelper.toMap(tarFile)

        then: 'then the map contains the file contents'
        map.size() == 10

        (1..10).each {
            map["test${it}.txt"] == "Lorem Ipsum ${it}"
        }
    }

    def "TarToMap Directory"() {
        given: 'a tar file with dirs'
        File tarFile = tmpFoler.newFile('test-dir.tar')

        tarFile.withOutputStream { fileOs ->
            TarArchiveOutputStream tarArchive = new TarArchiveOutputStream(fileOs)

            createTarEntry(tarArchive, 'dir/test.txt', 'Hello World!')
            createTarEntry(tarArchive, 'dir/test2.txt', 'Hello Again')
            createTarEntry(tarArchive, 'some-other-dir/file.txt', 'Hello Again')

            tarArchive.close()
        }

        when: 'the the helper method created the map'
        def map = ArchiveHelper.toMap(tarFile)

        then: 'then the map contains the file contents'
        map.size() == 3

        map['dir/test.txt'] == 'Hello World!'
        map['dir/test2.txt'] == 'Hello Again'
        map['some-other-dir/file.txt'] == 'Hello Again'
    }
}

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

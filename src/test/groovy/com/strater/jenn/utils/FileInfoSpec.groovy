package com.strater.jenn.utils

import spock.lang.Specification

class FileInfoSpec extends Specification {

    void "test creating a file info from a long path"() {
        when:
        FileInfo info = new FileInfo('/users/me/something/file.txt')

        then:
        with(info) {
            path == '/users/me/something'
            filename == 'file'
            extension == 'txt'
            pathUpOneLevel == '/users/me'
        }
    }

    void "test result of pathUpOneLevel of file at root"() {
        when:
        FileInfo info = new FileInfo('sample.txt')

        then:
        info.pathUpOneLevel == '/'
    }

    void "test result of jar filepath"() {
        when:
        FileInfo info = new FileInfo('test/jars/groovy-all-2.4.9.jar')

        then:
        with(info) {
            path == 'test/jars'
            filename == 'groovy-all-2.4.9'
            extension == 'jar'
            pathUpOneLevel == 'test'
        }
    }
}

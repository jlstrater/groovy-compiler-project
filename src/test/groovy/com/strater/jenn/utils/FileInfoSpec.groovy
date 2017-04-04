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
            info.pathUpOneLevel == '/users/me'
        }
    }

    void "test result of pathUpOneLevel of file at root"() {
        when:
        FileInfo info = new FileInfo('sample.txt')

        then:
        info.pathUpOneLevel == '/'
    }
}

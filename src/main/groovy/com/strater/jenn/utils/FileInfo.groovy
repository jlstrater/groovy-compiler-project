package com.strater.jenn.utils

class FileInfo {
    String filename
    String path
    String extension
    String info

    FileInfo(String fileInfo) {
        this.info = fileInfo
        List tokens = fileInfo.split('/')
        String filenameAndExtension = tokens?.getAt(-1)
        List filenameTokens = filenameAndExtension.split(/\./)
        this.extension = filenameTokens[1]
        this.filename = filenameTokens[0]
        this.path = tokens.size() > 1 ? tokens[0..-2].join('/') : '/'
    }

    String getPathUpOneLevel() {
        if (!path || path == '/') {
            return '/'
        }
        path.split('/')[0..-2].join('/')
    }
}

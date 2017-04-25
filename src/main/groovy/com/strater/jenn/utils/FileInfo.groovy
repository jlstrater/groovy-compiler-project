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
        Integer extensionIndex = filenameAndExtension.contains('.') ? filenameAndExtension.lastIndexOf('.') : 0
        this.extension = filenameAndExtension[extensionIndex + 1..-1]
        this.filename = filenameAndExtension[0..<extensionIndex]
        this.path = tokens.size() > 1 ? tokens[0..-2].join('/') : '/'
    }

    String getPathUpOneLevel() {
        if (!path || path == '/') {
            return '/'
        }
        path.split('/')[0..-2].join('/')
    }
}

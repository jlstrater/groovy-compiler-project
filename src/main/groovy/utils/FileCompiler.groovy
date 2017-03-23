package utils

import groovy.util.logging.Slf4j

@Slf4j
class FileCompiler {

    private static final String SC_CONFIG = 'build/resources/main/config/sc_config.groovy'

    private static final String GROOVYC_OUTPUT_DIR = 'build/groovyc'
    private static final String INDY_OUTPUT_DIR = 'build/indy'
    private static final String SC_OUTPUT_DIR = 'build/static'

    List compileFile(FileInfo file) {

        log.info 'Starting Analysis for program: ' + file.filename

        compileWithGroovyc(file)
        String groovycBytecode = fetchByteCode(file, GROOVYC_OUTPUT_DIR)

        compileWithInvokeDynamic(file)
        String indyBytecode = fetchByteCode(file, INDY_OUTPUT_DIR)

        String errors = compileWithStaticConfig(file)
        String staticBytecode = errors ?: fetchByteCode(file, SC_OUTPUT_DIR)

        [groovycBytecode, indyBytecode, staticBytecode]
    }

    void compileWithGroovyc(FileInfo file) {
        log.info 'running groovyc for ' + file.filename
        Process process = "groovyc $file.info -d $GROOVYC_OUTPUT_DIR".execute()
        process.waitFor()
    }

    void compileWithInvokeDynamic(FileInfo file) {
        log.info 'running invoke dynamic for ' + file.filename
        Process process = "groovyc --indy $file.info -d $INDY_OUTPUT_DIR".execute()
        process.waitFor()
    }

    @SuppressWarnings('JavaIoPackageAccess')
    String compileWithStaticConfig(FileInfo file) {
        log.info 'running static compilation for ' + file.filename
        StringBuffer error = new StringBuffer()
        Process process = "groovyc --configscript $SC_CONFIG $file.info -d $SC_OUTPUT_DIR".execute()
        process.consumeProcessErrorStream(error)
        process.waitFor()
         if (error) {
            return error
        }
    }

    String fetchByteCode(FileInfo fileInfo, String outputDir) {
        String classFileName = outputDir + '/' + fileInfo.filename + '.class'
        javapOnBytecode(classFileName)
    }

    static String javapOnBytecode(String classFileInfo) {
        Process process = "javap -c $classFileInfo".execute()

        StringBuffer out = new StringBuffer()
        process.consumeProcessOutputStream(out)
        process.waitFor()
        out
    }
}

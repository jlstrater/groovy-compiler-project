import groovy.util.logging.Slf4j
import utils.FileInfo

@Slf4j
@SuppressWarnings('NoDef')
class App {
    private static final String GROOVYC_OUTPUT_DIR = 'build/groovyc'
    private static final String INDY_OUTPUT_DIR = 'build/indy'

    static void main(String[] args) throws Exception {
        if (!args) {
            log.error 'Error: No program specified.'
            return
        }

        args.each { String fileinfo ->
            FileInfo file = new FileInfo(fileinfo)
            log.info 'Starting Analysis for program: ' + file.filename

            compileWithGroovyc(file)
//            fetchByteCode(file, GROOVYC_OUTPUT_DIR)

            compileWithInvokeDynamic(file)
//            fetchByteCode(file, INDY_OUTPUT_DIR)

            //todo: compile with static compiler

        }
    }

    static void compileWithGroovyc(FileInfo file) {
        log.info 'running groovyc for ' + file.filename
        "groovyc $file.filename -d $GROOVYC_OUTPUT_DIR".execute()
    }

    static void compileWithInvokeDynamic(FileInfo file) {
        log.info 'running invoke dynamic for ' + file.filename
        "groovyc --indy $file.filename -d $INDY_OUTPUT_DIR".execute()
    }

    @SuppressWarnings('JavaIoPackageAccess')
    static String fetchByteCode(FileInfo fileInfo, String outputDir) {
        String fileText = new File(fileInfo.pathUpOneLevel + '/' + outputDir + '/' + fileInfo.filename + '.class').text
        log.info fileText
        fileText
    }
}

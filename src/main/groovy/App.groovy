import groovy.util.logging.Slf4j
import report.FileReportWriter
import report.ReportWriter
import utils.FileInfo

@Slf4j
@SuppressWarnings('NoDef')
class App {
    private static final String SC_CONFIG = 'build/resources/main/config/sc_config.groovy'

    private static final String GROOVYC_OUTPUT_DIR = 'build/groovyc'
    private static final String LOCAL_GROOVYC_OUTPUT_DIR = 'build/local/groovyc'
    private static final String INDY_OUTPUT_DIR = 'build/indy'
    private static final String LOCAL_INDY_OUTPUT_DIR = 'build/local/indy'
    private static final String SC_OUTPUT_DIR = 'build/static'
    private static final String LOCAL_SC_OUTPUT_DIR = 'build/local/static'

    static void main(String[] args) throws Exception {
        if (!args) {
            log.error 'Error: No program specified.'
            return
        }

        args.each { String fileinfo ->
            FileInfo file = new FileInfo(fileinfo)
            log.info 'Starting Analysis for program: ' + file.filename

            compileWithGroovyc(file)
            String groovycByteCode = fetchByteCode(file, GROOVYC_OUTPUT_DIR)

            compileWithInvokeDynamic(file)
            String indyByteCode = fetchByteCode(file, INDY_OUTPUT_DIR)

            compileWithStaticConfig(file)
            String staticByteCode = fetchByteCode(file, SC_OUTPUT_DIR)

            //            compileWithLocalGroovyC(file)
//            String localGroovycByteCode = fetchByteCode(file, LOCAL_GROOVYC_OUTPUT_DIR)
//
//            compileWithLocalIndy(file)
//            String localIndyByteCode = fetchByteCode(file, LOCAL_INDY_OUTPUT_DIR)

//            compileWithLocalStaticConfig(file)
//            String localStaticByteCode = fetchByteCode(file, LOCAL_SC_OUTPUT_DIR)

            //write to report
            FileReportWriter writer = new FileReportWriter()
            writer.writeReport(file.filename, [groovycByteCode, indyByteCode, staticByteCode])

            ReportWriter report = new ReportWriter()
            report.write()
        }
    }

    static void compileWithGroovyc(FileInfo file) {
        log.info 'running groovyc for ' + file.filename
        "groovyc $file.info -d $GROOVYC_OUTPUT_DIR".execute()
    }

    static void compileWithInvokeDynamic(FileInfo file) {
        log.info 'running invoke dynamic for ' + file.filename
        "groovyc --indy $file.info -d $INDY_OUTPUT_DIR".execute()
    }

    static void compileWithStaticConfig(FileInfo file) {
        log.info 'running static compilation for ' + file.filename
        "groovyc --configscript $SC_CONFIG $file.info -d $SC_OUTPUT_DIR".execute()
    }

    static void compileWithLocalGroovyC(FileInfo file) {
        log.info 'running with local groovyc for ' + file.filename
        "groovyc $file.info -d $LOCAL_GROOVYC_OUTPUT_DIR".execute()
    }

    static void compileWithLocalIndy(FileInfo file) {
        log.info 'running with local invoke dynamic for ' + file.filename
        "groovyc --indy $file.info -d $LOCAL_INDY_OUTPUT_DIR".execute()
    }

    static void compileWithLocalStaticConfig(FileInfo file) {
        log.info 'running with local static compilation for ' + file.filename
        "groovyc --configscript $SC_CONFIG $file.info -d $LOCAL_SC_OUTPUT_DIR".execute()
    }

    @SuppressWarnings('JavaIoPackageAccess')
    static String fetchByteCode(FileInfo fileInfo, String outputDir) {
        def process = "javap -c ${outputDir + '/' + fileInfo.filename + '.class'}".execute()

        StringBuffer out = new StringBuffer()
        process.consumeProcessOutputStream(out)
        process.waitFor()

        new File("${fileInfo.pathUpOneLevel}/bytecode").mkdirs()
        File file = new File("${fileInfo.pathUpOneLevel}/bytecode", "${fileInfo.filename}.bytecode")
        file << out

        out
    }
}

import groovy.util.logging.Slf4j
import report.FileReportWriter
import report.ReportWriter
import utils.FileCompiler
import utils.FileInfo

@Slf4j
@SuppressWarnings('NoDef')
class App {
    static void main(String[] args) throws Exception {
        if (!args) {
            log.error 'Error: No program specified.'
            return
        }

        FileCompiler fileCompiler = new FileCompiler()
        FileReportWriter writer = new FileReportWriter()
        ReportWriter report = new ReportWriter()

        args.each { String fileinfo ->
            FileInfo file = new FileInfo(fileinfo)
            List beforeOutputFiles = fileCompiler.compileFile(file)

            // reader, process bytecode, and write to new files
            new ByteCodeOptimizer().processClassFiles(file)

            //write to 'before' and 'after' report
            writer.writeReport(file.filename, beforeOutputFiles)
            report.write()
        }
    }
}

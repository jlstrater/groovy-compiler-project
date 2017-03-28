import benchmark.Benchmark
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
        Benchmark benchmark = new Benchmark()
        ByteCodeOptimizer byteCodeOptimizer = new ByteCodeOptimizer()

        args.each { String file ->
            FileInfo fileInfo = new FileInfo(file)
            List beforeOutputFiles = fileCompiler.compileFile(fileInfo)

            // reader, process bytecode, and write to new files
            List afterOutputFiles = byteCodeOptimizer.processClassFiles(fileInfo)

            // benchmark bytecode
            List benchmarkData = benchmark.runBenchmark(fileInfo.filename)

            //write to 'before' and 'after' report
            writer.writeReport(fileInfo.filename, beforeOutputFiles, afterOutputFiles, benchmarkData)
            report.write()
        }
    }
}

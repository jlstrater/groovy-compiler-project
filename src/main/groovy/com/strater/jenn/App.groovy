package com.strater.jenn

import com.strater.jenn.benchmark.Benchmark
import com.strater.jenn.benchmark.Stats
import com.strater.jenn.utils.FileInfo
import groovy.util.logging.Slf4j
import com.strater.jenn.report.FileReportWriter
import com.strater.jenn.report.ReportWriter
import com.strater.jenn.utils.FileCompiler

@Slf4j
@SuppressWarnings('NoDef')
class App {

    static FileCompiler fileCompiler = new FileCompiler()
    static FileReportWriter writer = new FileReportWriter()
    static ReportWriter report = new ReportWriter()
    static Benchmark benchmark = new Benchmark()
    static
    ByteCodeOptimizer byteCodeOptimizer = new ByteCodeOptimizer()

    static void main(String[] args) throws Exception {
        if (args) {
            args.each { String file ->
                runAgainstFiles(file)
            }
        } else {
            runAgainstThisApplication()
        }
    }

    static void runAgainstFiles(String file) {
        new File('build/bytecode').deleteDir()
        FileInfo fileInfo = new FileInfo(file)
        List beforeOutputFiles = fileCompiler.compileFile(fileInfo)

        // reader, process bytecode, and write to new files
        List afterOutputFiles = byteCodeOptimizer.processDirectory('build/bytecode', 'build/bytecode/new/')

        // benchmark bytecode
        List benchmarkData = benchmark.runBenchmark(fileInfo.filename)

        //write to 'before' and 'after' report
        writer.writeReport(fileInfo.filename, beforeOutputFiles, afterOutputFiles, benchmarkData)
        report.write()

        new File('build/bytecode').deleteDir()
    }

    static void runAgainstThisApplication() {
        File outputDir = new File('build/bytecode')
        outputDir.exists() ? outputDir.delete() : outputDir.mkdir()
        Integer linesRemoved = byteCodeOptimizer.processDirectory('build/classes', 'build/bytecode/').linesRemoved.sum()
        new File('build/jars').mkdirs()
        String oldJarFile = fileCompiler.packageJar('build/classes/main', 'old-app', 'com.strater.jenn.App')
        String newJarFile = fileCompiler.packageJar('build/bytecode/main', 'new-app', 'com.strater.jenn.App')
        Stats beforeBenchmarkData = benchmark.execBenchmarkOnApplication(oldJarFile,
                '/build/resources/test/scripts/HelloWorld.groovy')
        Stats afterBenchmarkData = benchmark.execBenchmarkOnApplication(newJarFile,
                '/build/resources/test/scripts/HelloWorld.groovy')

        writer.writeAppReport('App', linesRemoved, beforeBenchmarkData, afterBenchmarkData)
        report.write()
        new File('build/bytecode').deleteDir()
    }
}

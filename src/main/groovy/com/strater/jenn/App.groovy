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
        File outputDir = new File ('build/bytecode')
        outputDir.deleteDir() //clean out old results
        outputDir.mkdir() // remake dir for this run
        FileInfo fileInfo = new FileInfo(file)
        if (fileInfo.extension == 'jar') {
            File jarFile = new File(fileInfo.info)

            //unzip jar
            File tempFolder = new File('build/unzippedJars')
            tempFolder.mkdir()
            Process p = "jar xf $fileInfo.info".execute([], tempFolder)
            p.waitFor()

            // reader, process bytecode, and write to new files
            List result = byteCodeOptimizer.processDirectory('build/unzippedJars', 'build/bytecode/')

            File jarFolder = new File('build/jars')
            jarFolder.mkdir()
            StringBuffer out = new StringBuffer()
            StringBuffer error = new StringBuffer()
            p = "jar cmvf META-INF/MANIFEST.MF ${fileInfo.filename}-optimized.jar .".execute([], outputDir)
            p.consumeProcessOutput(out, error)
            p.waitFor()

            p = "cp $fileInfo.filename-optimized.jar ../jars".execute([], outputDir)
            p.consumeProcessOutput(out, error)
            p.waitFor()
            String jarLocation = "build/jars/$fileInfo.filename-optimized.jar"

            //output report stats
            Integer jarSize = jarFile.length()
            File newJarFile = new File(jarLocation)
            Integer newJarSize = newJarFile.length()

            Integer reduction = newJarSize - jarSize

            writer.writeJarReport(fileInfo.filename, result, reduction)
            report.write()

            tempFolder.deleteDir()
            outputDir.deleteDir()
        } else {
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
    }

    static void runAgainstThisApplication() {
        File outputDir = new File('build/bytecode')
        outputDir.exists() ? outputDir.delete() : outputDir.mkdir()
        List<ByteCodeOptimizer.OptimizationResult> result = byteCodeOptimizer
                .processDirectory('build/classes', 'build/bytecode/')
        new File('build/jars').mkdirs()

        Process p = './gradlew shadowJar'.execute()
        p.waitFor()
        String jarLocation = 'build/libs/groovy-compiler-project-all.jar'

        Stats beforeBenchmarkData = benchmark.execBenchmarkOnApplication(jarLocation,
                'build/resources/test/scripts/HelloWorld.groovy')

        p = './gradlew newJar'.execute()
        p.waitFor()

        Stats afterBenchmarkData = benchmark.execBenchmarkOnApplication(jarLocation,
                'build/resources/test/scripts/HelloWorld.groovy')

        writer.writeAppReport('App', result, beforeBenchmarkData, afterBenchmarkData)
        report.write()
        new File('build/bytecode').deleteDir()
    }
}

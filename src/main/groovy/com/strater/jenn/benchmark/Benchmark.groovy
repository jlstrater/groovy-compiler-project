package com.strater.jenn.benchmark

/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/* modified from Apache Groovy */
import groovy.util.logging.Slf4j

@Slf4j
class Benchmark {
    static final String GROOVY_CLASSPATH = System.getenv('GROOVY_HOME') + '/embeddable/groovy-all-2.4.9-indy.jar'

    Map<String, List> benchData = [
            ackermann: [1, 2],
            ary: [10, 100],
            binarytrees: [1],
            collectloop: [1000, 100000, 1000000, 100000000, 100000000000],
            eachloop: [1000, 100000, 1000000, 100000000, 100000000000],
            echo: [1],
            fannkuch: [1, 2, 3],
            fibo: [10, 25, 50],
            forloop: [1000, 100000, 1000000, 100000000, 100000000000],
            mandelbrot: [1, 10],
            nsieve: [1, 2, 3, 4, 5],
            random: [1, 5, 10],
            recursive: [1],
            regexdna: [1],
            revcomp: [1],
            spectralnorm: [1],
            threadring: [1],
    ]

    Map<String, String> compilationTypes = [
            groovyc: 'build/bytecode/groovyc',
            indy: 'build/bytecode/indy',
            static: 'build/bytecode/static',
            newgroovyc: 'build/bytecode/new/groovyc',
            newindy: 'build/bytecode/new/indy',
            newstatic: 'build/bytecode/new/static',
    ]

    List runBenchmark(String filename) {

        List results = []
        List params = benchData[filename] ?: [null]

        compilationTypes.each { String ctype, String path ->
            params.each {
                BenchmarkResultSet result = new BenchmarkResultSet(compilationType: ctype, filename: filename,
                        parameter: it,)
                result.stats = execBenchmark(System.getProperty('user.dir') + '/' + path, filename, it)
                results << result
            }
        }

        results
    }

    static Stats execBenchmarkOnApplication(String jarPath, String param) {
        File jarFile = new File(jarPath)
        if (jarFile.exists()) {
            log.debug '\t\t running jar'
            StringBuffer error = new StringBuffer()
            StringBuffer out = new StringBuffer()

            Map times = [:]
            10.times { n ->
                long time1 = System.nanoTime()
                Process p = "java -Djava.compiler=NONE -jar $jarFile.path $param ".execute()
                p.consumeProcessErrorStream(error)
                p.consumeProcessOutputStream(out)
                p.waitForOrKill(6 * 1000)
                long time2 = System.nanoTime()
                times[n] = (time2 - time1).toDouble()
            }

            if (error) {
                log.debug 'error: ' + error
                return new Stats(average: 'Error', stddev: 'Error')
            }
            log.debug 'main app runtime: ' + times.values().sum()

            return calculateStats(times)
        }
        new Stats(average: 'N/A', stddev: 'N/A')
    }

    static Stats execBenchmark(String classDir, String filename, param) {
        File classFile = new File(classDir + '/' + filename + '.class')
        if (classFile.exists()) {
            log.debug '\t\trunning  '
            StringBuffer error = new StringBuffer()

            // throw out first run as warmup
            "java -cp $classDir:$GROOVY_CLASSPATH $filename ${param ?: ''}".execute()

            Map times = [:]
            20.times { n ->
                long time1 = System.nanoTime()
                Process p = "java -Djava.compiler=NONE -cp $classDir:$GROOVY_CLASSPATH $filename ${param ?: ''}"
                        .execute()
                p.consumeProcessErrorStream(error)
                p.waitForOrKill(6 * 1000)
                Double time2 = System.nanoTime()
                times[n] = time2 - time1
            }

            if (error) {
                log.debug 'error: ' + error
                return new Stats(average: 'Error', stddev: 'Error')
            }

            List compilationTypeTokens = classDir.tokenize('/')[-2..-1]
            String compilationType = compilationTypeTokens.last() == 'new' ? 'new - ' + compilationTypeTokens[0] :
                    compilationTypeTokens[1]
            log.debug "total runtime for $filename with $compilationType for param: $param = ${times.values().sum()}"

            return calculateStats(times)
        }
        new Stats(average: 'N/A', stddev: 'N/A')
    }

    static Stats calculateStats(Map times) {
        Stats stats = new Stats()
        Double average = times.values().sum() / times.size()
        stats.average = (average / 1000000).round()
        stats.stddev = ((times.values().collect { (it - average ).abs() }.sum() / times.size()) / 1000000).round(2)
        stats
    }
}

class BenchmarkResultSet {
    String compilationType
    String filename
    String parameter
    Stats stats
}

class Stats {
    String average = 'N/A'
    String stddev = 'N/A'
}

package benchmark

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
@SuppressWarnings('JavaIoPackageAccess')
class Benchmark {
    static final String GROOVY_CLASSPATH = System.getenv('GROOVY_HOME') + '/embeddable/groovy-all-2.4.9.jar'

    Map<String, List> benchData = [
            ackermann: [1, 2],
            ary: [10, 100],
            collectloop: [1000, 100000, 1000000, 100000000],
            eachloop: [1000, 100000, 1000000, 100000000],
            fannkuch: [1, 2, 3],
            fibo: [5, 10, 20],
            forloop: [1000, 100000, 1000000, 100000000],
            mandelbrot: [1, 10],
            nsieve: [1, 2, 3, 4, 5],
            random: [1, 5, 10],
            recursive: [1],
            regexdna: [1],
            revcomp: [1],
            spectralnorm: [1],
            threadring: [1],
            wordfreq: [1],
    ]

    Map<String, String> compilationTypes = [
            groovyc: 'build/groovyc',
            indy: 'build/indy',
            static: 'build/static',
            newgroovyc: 'build/groovyc/new',
            newindy: 'build/indy/new',
            newstatic: 'build/static/new',
    ]

    List runBenchmark(String filename) {

        List results = []
        List params = benchData[filename] ?: [null]

        compilationTypes.each { String ctype, String path ->
            params.each {
                BenchmarkResultSet result = new BenchmarkResultSet(compilationType: ctype, filename: filename,
                        parameter: it,)
                result.runtime = execBenchmark(System.getProperty('user.dir') + '/' + path, filename, it)
                results << result
            }
        }

        results
    }

    static String execBenchmark(String classDir, String filename, param) {
        File classFile = new File(classDir + '/' + filename + '.class')
        if (classFile.exists()) {
            log.info '\t\trunning  '
            StringBuffer error = new StringBuffer()

            //throw away the first run as warmup time etc
            "java -cp $classDir:$GROOVY_CLASSPATH $filename ${param ?: ''}".execute()

            long time1 = System.nanoTime()
            20.times { n ->
                Process p = "java -cp $classDir:$GROOVY_CLASSPATH $filename ${param ?: ''}".execute()
                p.consumeProcessErrorStream(error)
                p.waitForOrKill(60 * 1000)
            }
            long time2 = System.nanoTime()
            long td = (time2 - time1) / 1000000 / 20

            if (error) {
                log.info 'error: ' + error
            }
            log.info "time ($param) = $td"

            return td.toString()
        }
        'N/A'
    }
}

class BenchmarkResultSet {
    String compilationType
    String filename
    String parameter
    String runtime = 'N/A'
}

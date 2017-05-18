package com.strater.jenn.report

import com.strater.jenn.ByteCodeOptimizer
import com.strater.jenn.benchmark.BenchmarkResultSet
import com.strater.jenn.benchmark.Stats
import groovy.xml.MarkupBuilder

@SuppressWarnings(['AbcMetric', 'CyclomaticComplexity'])
class FileReportWriter extends ReportWriter {
    public static final DEFAULT_OUTPUT_FILE = 'BytecodeReport'
    public static final IND_REPORT_DIR = REPORT_DIR + '/reports'

    @SuppressWarnings(['NestedBlockDepth', 'MethodSize'])
    void writeReport(String filename, List<String> before, List<String> after, List<BenchmarkResultSet> benchmarkData) {
        new File(IND_REPORT_DIR).mkdirs()
        File file = new File(IND_REPORT_DIR + '/' + DEFAULT_OUTPUT_FILE + '-' + filename + '.html')
        file.withWriter { writer ->
            MarkupBuilder html = new MarkupBuilder(writer)

            Integer linesRemovedGroovyc = after.findAll { it.compilationType == 'groovyc' }*.linesRemoved
                    .sum() ?: 0
            Integer linesRemovedIndy = after.findAll { it.compilationType == 'indy' }*.linesRemoved
                    .sum() ?: 0
            Integer linesRemovedStatic = after.findAll { it.compilationType == 'static' }*.linesRemoved
                    .sum() ?: 0

            Integer totalLinesGroovyc = after.findAll { it.compilationType == 'groovyc' }*.totalLines
                    .sum() ?: 0
            Integer totalLinesIndy = after.findAll { it.compilationType == 'indy' }*.totalLines
                    .sum() ?: 0
            Integer totalLinesStatic = after.findAll { it.compilationType == 'static' }*.totalLines
                    .sum() ?: 0

            html.html {
                head {
                    title 'Bytecode Analysis Report'
                    link rel: 'stylesheet', type: 'text/css', href: BOOTSTRAP_CSS
                    link rel: 'stylesheet', type: 'text/css', href: HIGHLIGHT_JS_CSS
                    script(src: JQUERY_JS, type: 'text/javascript', '')
                    script(src: BOOTSTRAP_JS, type: 'text/javascript', '')
                    script(src: HIGHLIGHT_JS_JS, type: 'text/javascript', '')
                    script('hljs.initHighlightingOnLoad();')
                }
                body {
                    ul(class: 'breadcrumb') {
                        li {
                            a href: '../index.html', 'Home'
                            span class: 'divider'
                        }
                        li {
                            a filename.toUpperCase()
                        }
                    }
                    h2 class: 'text-center', 'Bytecode Analysis for script: ' + filename
                    div(class: 'row col-lg-5 col-lg-offset-4') {
                        pre {
                            code class: 'groovy',
                                    new File('src/test/resources/scripts/' + filename + '.groovy').text.stripIndent()
                        }
                    }

                    div(class: 'row col-sm-10 text-center col-sm-offset-1') {
                        table(class: 'table') {
                            tr {
                                th ''
                                th 'Groovyc'
                                th 'Indy'
                                th 'Static'
                            }
                            tr {
                                td 'Lines Removed'
                                td linesRemovedGroovyc
                                td linesRemovedIndy
                                td linesRemovedStatic
                            }
                            tr {
                                td 'Total Lines - Before'
                                td totalLinesGroovyc
                                td totalLinesIndy
                                td totalLinesStatic
                            }
                            tr {
                                td 'Percentage of Lines Removed'
                                td totalLinesGroovyc ? ((linesRemovedGroovyc / totalLinesGroovyc * 100) as double)
                                        .round(1) : 'Error'
                                td totalLinesIndy ? ((linesRemovedIndy / totalLinesIndy * 100) as double)
                                        .round(1) : 'Error'
                                td totalLinesStatic ? ((linesRemovedStatic / totalLinesStatic * 100) as double)
                                        .round(1) : 'Error'
                            }
                        }
                    }

                    div(class: 'row col-sm-10 text-center col-sm-offset-1') {
                        h2 'Benchmarks'
                        benchmarkData*.parameter.unique().each { param ->
                            div(class: 'col-sm-6 text-center') {
                                h3 'For parameter: ' + param
                                table(class: 'table') {
                                    tr {
                                        th ''
                                        th 'Groovyc'
                                        th 'Indy'
                                        th 'Static'
                                    }
                                    tr {
                                        td 'Benchmark Average - Before'
                                        td benchmarkData.find {
                                            it.compilationType == 'groovyc' && it.parameter == param
                                        }.stats.average
                                        td benchmarkData.find {
                                            it.compilationType == 'indy' && it.parameter == param
                                        }.stats.average
                                        td benchmarkData.find {
                                            it.compilationType == 'static' && it.parameter == param
                                        }.stats.average
                                    }
                                    tr {
                                        td 'Benchmark Average - After'
                                        td benchmarkData.find {
                                            it.compilationType == 'newgroovyc' && it.parameter == param
                                        }.stats.average
                                        td benchmarkData.find {
                                            it.compilationType == 'newindy' && it.parameter == param
                                        }.stats.average
                                        td benchmarkData.find {
                                            it.compilationType == 'newstatic' && it.parameter == param
                                        }.stats.average
                                    }
                                    tr {
                                        td 'Benchmark Std Dev - Before'
                                        td benchmarkData.find {
                                            it.compilationType == 'groovyc' && it.parameter == param
                                        }.stats.stddev
                                        td benchmarkData.find {
                                            it.compilationType == 'indy' && it.parameter == param
                                        }.stats.stddev
                                        td benchmarkData.find {
                                            it.compilationType == 'static' && it.parameter == param
                                        }.stats.stddev
                                    }
                                    tr {
                                        td 'Benchmark Std Dev - After'
                                        td benchmarkData.find {
                                            it.compilationType == 'newgroovyc' && it.parameter == param
                                        }.stats.stddev
                                        td benchmarkData.find {
                                            it.compilationType == 'newindy' && it.parameter == param
                                        }.stats.stddev
                                        td benchmarkData.find {
                                            it.compilationType == 'newstatic' && it.parameter == param
                                        }.stats.stddev
                                    }
                                }
                            }
                        }
                    }

                    div(class: 'row col-sm-12') {
                        h1 'BEFORE'
                    }

                    div(class: 'row col-lg-12') {
                        div(class: 'col-md-4') {
                            h2 class: 'text-center', 'Groovyc (Legacy)'
                            pre {
                                code class: 'assembly', before.get(0)
                            }
                        }
                        div(class: 'col-md-4') {
                            h2 class: 'text-center', 'Invoke Dynamic'
                            pre {
                                code class: 'assembly', before.get(1)
                            }
                        }

                        div(class: 'col-md-4') {
                            h2 class: 'text-center', 'Static Compilation'
                            pre {
                                code class: 'assembly', before.get(2)
                            }
                        }
                    }

                    div(class: 'col-sm-12') {
                        h2 'AFTER'
                    }
                    div(class: 'row col-lg-12') {
                        div(class: 'col-md-4') {
                            h2 class: 'text-center', 'Groovyc (Legacy)'
                            pre {
                                code class: 'assembly', after.find { it.compilationType == 'groovyc' }*.text?.join('\n')
                            }
                        }
                        div(class: 'col-md-4') {
                            h2 class: 'text-center', 'Invoke Dynamic'
                            pre {
                                code class: 'assembly', after.find { it.compilationType == 'indy' }*.text?.join('\n')
                            }
                        }

                        div(class: 'col-md-4') {
                            h2 class: 'text-center', 'Static Compilation'
                            pre {
                                code class: 'assembly', after.find { it.compilationType == 'static' }*.text?.join('\n')
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings(['NestedBlockDepth', 'MethodSize'])
    void writeAppReport(String filename, List<ByteCodeOptimizer.OptimizationResult> results, Stats beforeBenchmarkData,
                        Stats afterBenchmarkData) {
        new File(IND_REPORT_DIR).mkdirs()
        File file = new File(IND_REPORT_DIR + '/' + DEFAULT_OUTPUT_FILE + '-' + filename + '.html')
        file.withWriter { writer ->
            MarkupBuilder html = new MarkupBuilder(writer)

            html.html {
                head {
                    title 'Bytecode Analysis Report'
                    link rel: 'stylesheet', type: 'text/css', href: BOOTSTRAP_CSS
                    link rel: 'stylesheet', type: 'text/css', href: HIGHLIGHT_JS_CSS
                    script(src: JQUERY_JS, type: 'text/javascript', '')
                    script(src: BOOTSTRAP_JS, type: 'text/javascript', '')
                    script(src: HIGHLIGHT_JS_JS, type: 'text/javascript', '')
                    script('hljs.initHighlightingOnLoad();')
                }
                body {
                    ul(class: 'breadcrumb') {
                        li {
                            a href: '../index.html', 'Home'
                            span class: 'divider'
                        }
                        li {
                            a filename.toUpperCase()
                        }
                    }
                    h2 class: 'text-center', 'Bytecode Analysis for com.strater.jenn.App: ' + filename

                    div(class: 'row col-sm-10 text-center col-sm-offset-1') {
                        h2 'Lines Removed: ' + results.linesRemoved.sum()
                        h2 'Total Lines: ' + results.totalLines.sum()
                        h2 'Percentage Lines Removed: ' + (((results.linesRemoved.sum() / results.totalLines.sum())
                                * 100) as double).round(1)
                    }

                    div(class: 'row col-sm-10 text-center col-sm-offset-1') {
                        h2 'Benchmarks'
                        div(class: 'col-sm-6 text-center') {
                            table(class: 'table') {
                                tr {
                                    th ''
                                    th 'Before Optimization'
                                    th 'After Optimization'
                                }
                                tr {
                                    td 'Benchmark Average'
                                    td beforeBenchmarkData.average
                                    td afterBenchmarkData.average
                                }
                                tr {
                                    td 'Benchmark Std Dev'
                                    td beforeBenchmarkData.stddev
                                    td afterBenchmarkData.stddev
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings(['NestedBlockDepth', 'MethodSize'])
    void writeJarReport(String filename, List<ByteCodeOptimizer.OptimizationResult> results, Integer reduction) {
        new File(IND_REPORT_DIR).mkdirs()
        File file = new File(IND_REPORT_DIR + '/' + DEFAULT_OUTPUT_FILE + '-' + filename + '.html')
        file.withWriter { writer ->
            MarkupBuilder html = new MarkupBuilder(writer)

            html.html {
                head {
                    title 'Bytecode Analysis Report'
                    link rel: 'stylesheet', type: 'text/css', href: BOOTSTRAP_CSS
                    link rel: 'stylesheet', type: 'text/css', href: HIGHLIGHT_JS_CSS
                    script(src: JQUERY_JS, type: 'text/javascript', '')
                    script(src: BOOTSTRAP_JS, type: 'text/javascript', '')
                    script(src: HIGHLIGHT_JS_JS, type: 'text/javascript', '')
                    script('hljs.initHighlightingOnLoad();')
                }
                body {
                    ul(class: 'breadcrumb') {
                        li {
                            a href: '../index.html', 'Home'
                            span class: 'divider'
                        }
                        li {
                            a filename.toUpperCase()
                        }
                    }
                    h2 class: 'text-center', 'Bytecode Analysis for Jar: ' + filename

                    div(class: 'row col-sm-10 text-center col-sm-offset-1') {
                        h2 'Jar File Size Reduction (in bytes): ' + reduction
                        h2 'Bytecode Instructions Removed: ' + results.linesRemoved.sum()
                    }
                }
            }
        }
    }
}

package report

import benchmark.BenchmarkResultSet
import groovy.xml.MarkupBuilder

@SuppressWarnings(['AbcMetric', 'CyclomaticComplexity'])
class FileReportWriter extends ReportWriter {
    public static final DEFAULT_OUTPUT_FILE = 'BytecodeReport'
    public static final IND_REPORT_DIR = REPORT_DIR + '/reports'

    @SuppressWarnings(['JavaIoPackageAccess', 'NestedBlockDepth', 'MethodSize'])
    void writeReport(String filename, List<String> before, List<String> after, List<BenchmarkResultSet> benchmarkData) {
        new File(IND_REPORT_DIR).mkdirs()
        File file = new File(IND_REPORT_DIR + '/' + DEFAULT_OUTPUT_FILE + '-' + filename + '.html')
        List beforeSizes = before.collect {
            it?.readLines()?.size() ?: 0
        }
        List afterSizes = after.collect {
            it?.readLines()?.size() ?: 0
        }
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
                    h2 class: 'text-center', 'Bytecode Analysis for script: ' + filename
                    div(class: 'row col-lg-4 col-lg-offset-4') {
                        pre {
                            code class: 'groovy',
                                new File('build/resources/test/scripts/' + filename + '.groovy').text.stripIndent()
                        }
                    }

                    div(class: 'row col-sm-8 text-center col-sm-offset-3') {
                        table(class: 'table') {
                            tr {
                                th ''
                                th 'Groovyc'
                                th 'Indy'
                                th 'Static'
                            }
                            tr {
                                td 'Lines Before'
                                td beforeSizes[0]
                                td beforeSizes[1]
                                td beforeSizes[2]
                            }
                            tr {
                                td 'Lines After'
                                td afterSizes[0]
                                td afterSizes[1]
                                td afterSizes[2]
                            }
                            tr {
                                td 'Lines Removed'
                                td (beforeSizes[0] - afterSizes[0])
                                td (beforeSizes[1] - afterSizes[1])
                                td afterSizes[2] ? (beforeSizes[2] - afterSizes[2]) : 'N/A'
                            }
                        }
                    }

                    div(class: 'row col-sm-12 text-center') {
                        h2 'Benchmarks'
                        benchmarkData*.parameter.unique().each { param ->
                            div (class: 'col-sm-4 text-center') {
                                h3 'For parameter: ' + param
                                table(class: 'table') {
                                    tr {
                                        th ''
                                        th 'Groovyc'
                                        th 'Indy'
                                        th 'Static'
                                    }
                                    tr {
                                        td 'Benchmark - Before'
                                        td benchmarkData.find {
                                            it.compilationType == 'groovyc' && it.parameter == param
                                        }.runtime
                                        td benchmarkData.find {
                                            it.compilationType == 'indy' && it.parameter == param
                                        }.runtime
                                        td benchmarkData.find {
                                            it.compilationType == 'static' && it.parameter == param
                                        }.runtime
                                    }
                                    tr {
                                        td 'Benchmark - After'
                                        td benchmarkData.find {
                                            it.compilationType == 'newgroovyc' && it.parameter == param
                                        }.runtime
                                        td benchmarkData.find {
                                            it.compilationType == 'newindy' && it.parameter == param
                                        }.runtime
                                        td benchmarkData.find {
                                            it.compilationType == 'newstatic' && it.parameter == param
                                        }.runtime
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
                                code class: 'assembly', after.get(0)
                            }
                        }
                        div(class: 'col-md-4') {
                            h2 class: 'text-center', 'Invoke Dynamic'
                            pre {
                                code class: 'assembly', after.get(1)
                            }
                        }

                        div(class: 'col-md-4') {
                            h2 class: 'text-center', 'Static Compilation'
                            pre {
                                code class: 'assembly', after.get(2)
                            }
                        }
                    }
                }
            }
        }
    }

}

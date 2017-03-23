package report

import groovy.xml.MarkupBuilder

@SuppressWarnings('AbcMetric')
class FileReportWriter extends ReportWriter {
    public static final DEFAULT_OUTPUT_FILE = 'BytecodeReport'
    public static final IND_REPORT_DIR = REPORT_DIR + '/reports'

    @SuppressWarnings(['JavaIoPackageAccess', 'NestedBlockDepth'])
    void writeReport(String filename, List<String> before, List<String> after) {
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
                    h2 class: 'text-center', 'Bytecode Analysis for script: ' + filename
                    div(class: 'row col-lg-4 col-lg-offset-4') {
                        pre {
                            code class: 'groovy', new File('build/resources/test/scripts/' + filename + '.groovy').text
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

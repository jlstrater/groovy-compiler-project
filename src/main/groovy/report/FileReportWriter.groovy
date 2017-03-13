package report

import groovy.xml.MarkupBuilder

class FileReportWriter extends ReportWriter {
    public static final DEFAULT_OUTPUT_FILE = 'BytecodeReport'

    @SuppressWarnings(['JavaIoPackageAccess', 'NestedBlockDepth'])
    void writeReport(String filename, List<String> results) {
        new File(REPORT_DIR).mkdirs()
        File file = new File(REPORT_DIR + '/' + DEFAULT_OUTPUT_FILE + '-' + filename + '.html')
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
                            a href: 'index.html', 'Home'
                            span class: 'divider'
                        }
                        li(class: 'active') {
                            a href: '#', filename.toUpperCase()
                        }
                    }
                    h2 class: 'text-center', 'Bytecode Analysis for script: ' + filename
                    div(class: 'row col-lg-10 col-lg-offset-1') {
                        div(class: 'col-md-6') {
                            h2 'Groovyc'
                            pre {
                                code class: 'assembly', results.get(0)
                            }
                        }
                        div(class: 'col-md-6') {
                            h2 'Invoke Dynamic'
                            pre {
                                code class: 'assembly', results.get(1)
                            }
                        }

                        /*div(class: 'col-md-4') {
                            h2 'Static Compilation'
                            pre {
                                code class: 'assembly', ''
                            }
                        }*/
                    }
                }
            }
        }
    }

}

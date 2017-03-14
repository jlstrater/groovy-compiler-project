package report

import groovy.xml.MarkupBuilder

class ReportWriter {
    public static final REPORT_DIR = 'build/reports/bytecode'
    public static final BOOTSTRAP_JS = 'https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js'
    public static final JQUERY_JS = 'https://code.jquery.com/jquery-3.1.1.slim.min.js'
    public static final HIGHLIGHT_JS_JS = '//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.10.0/highlight.min.js'
    public static final BOOTSTRAP_CSS = 'https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css'
    public static final HIGHLIGHT_JS_CSS = '//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.10.0/styles/default.min.css'

    public static final DEFAULT_OUTPUT_FILE = 'index.html'

    @SuppressWarnings(['JavaIoPackageAccess', 'NestedBlockDepth'])
    void write() {
        new File(REPORT_DIR).mkdirs()
        File file = new File(REPORT_DIR + '/' + DEFAULT_OUTPUT_FILE)
        File reportFolder = new File(REPORT_DIR + '/reports')
        List<String> filenames = reportFolder.listFiles()*.name

        file.withWriter { writer ->
            MarkupBuilder html = new MarkupBuilder(writer)

            html.html {
                head {
                    title 'Bytecode Analysis Report'
                    link rel: 'stylesheet', type: 'text/css', href: BOOTSTRAP_CSS
                    script(src: JQUERY_JS, type: 'text/javascript', '')
                    script(src: BOOTSTRAP_JS, type: 'text/javascript', '')
                }
                body {
                    h2 class: 'text-center', 'Bytecode Analysis for Various Groovy Scripts'
                    div(class: 'container') {
                        div(class: 'row col-lg-3  col-lg-offset-4') {
                            table(class: 'table') {
                                tr {
                                    th 'Filename'
                                }
                                filenames.each { filename ->
                                    tr {
                                        td {
                                            a href: 'reports/' + filename, filename - 'BytecodeReport-' - '.html'
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

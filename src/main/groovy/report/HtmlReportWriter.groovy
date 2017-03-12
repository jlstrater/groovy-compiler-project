package report

import groovy.xml.MarkupBuilder

class HtmlReportWriter {
    public static final DEFAULT_OUTPUT_FILE = 'BytecodeReport'
    public static final CSS_FILE = 'bytecode-report.css'
    public static final REPORT_DIR = 'build/reports/bytecode'

    String title

    @SuppressWarnings(['JavaIoPackageAccess', 'NestedBlockDepth'])
    void writeReport(String filename, List<String> results) {
        new File(REPORT_DIR).mkdirs()
        File file = new File(REPORT_DIR + '/' + DEFAULT_OUTPUT_FILE + '-' + filename + '.html')
        file.withWriter { writer ->
            MarkupBuilder html = new MarkupBuilder(writer)
            html.setEscapeAttributes(false)

            html.html {
                head {
                    title: 'Bytecode Analysis Report'
                }
                body {
                    h2 'Bytecode Analysis for ' + filename
                    table {
                        tr {
                            th 'GroovyC'
                            th 'Invoke Dynamic'
                            th 'Local GroovyC'
                            th 'Local Indy'
                        }
                        tr {
                            results.each { result ->
                                td {
                                    pre result
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

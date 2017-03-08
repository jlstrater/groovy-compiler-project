import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class AppTest extends Specification {

    @Shared
    private List<String> filenames = []
    private App app = new App()

    void setupSpec() {
        new File(this.class.getResource('scripts').file).eachFile { File file ->
            filenames << file.path
        }
    }

    @Unroll
    void "test example program: #filename"() {
        when:
        app.main(filename)

        then:
//        1 * app.compileWithGroovyc(_)
//        1 * app.compileWithInvokeDynamic(_)
        notThrown(FileNotFoundException)
        0 * _

        where:
        filename << filenames
    }
}

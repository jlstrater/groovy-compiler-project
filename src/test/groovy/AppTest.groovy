import com.strater.jenn.App
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class AppTest extends Specification {

    @Shared
    private List<String> scriptFilenames = []
    @Shared
    private List<String> jarFilenames = []
    private App app = new App()

    void setupSpec() {
        new File(this.class.getResource('scripts').file).eachFile { File file ->
            scriptFilenames << file.path
        }
        new File(this.class.getResource('jars').file).eachFile { File file ->
            jarFilenames << file.path
        }
    }

    @Unroll
    void "test example program: #filename"() {
        when:
        app.main(filename)

        then:
        notThrown(FileNotFoundException)
        0 * _

        where:
        filename << scriptFilenames[0..5]
    }

    void "test against this application (with no args)"() {
        when:
        app.main()

        then:
        0 * _
    }

    void "test against jar: #jarname"() {
        when:
        app.main(filename)

        then:
        notThrown(FileNotFoundException)
        0 * _

        where:
        filename << jarFilenames
    }
}

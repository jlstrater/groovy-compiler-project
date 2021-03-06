# groovy-compiler-project

This project include the work done as part of the Compiler Construction Course and the special topics course Static 
Analysis of Dynamic Languages in the Spring Semester of 2017 by Jenn Strater. The 
[report](https://jlstrater.github.io/groovy-compiler-project/html/index.html) is published to github pages
via asciidoctor and the gradle-git plugin.

### Requirements

- Java
- Groovy

You will need a version of Java and Groovy installed on the command line for this program to work.  I recommend 
installing both via the SDK Manager, [SDKMan](http://sdkman.io).

### Run

To run this project on your machine, use the gradle wrapper included in this project.

For mac:

`./gradlew run -Pfiles="path-to-file"`
 
 or windows:
 
 `gradlew run -Pfiles="path-to-file"`
 
 If no parameter is specified, the program will run the optimization against the program itself and benchmark 
 using the HelloWorld example in the `test/resources/scripts` directory.
 
 The html version of the bytecode report for each script/app/jar will be in `/build/docs/bytecode`


#### Tests

If you just want to run the tests,

`./gradlew tests --tests package.SpecName`

Without --tests it will run the entire test suite which includes all test cases, jars, and the application itself.


#### Docs

To regenerate the report, run

`./gradlew asciidoctor`

Because there are code snippets generated in the testing phase, this gradle task will also kick off the tests if there 
is no cached result.

By default, the reports will be output to `build/docs`.  There will be an html version and a pdf version.  The bytecode
reports and generated bytecode snippets will also be in that directory. This is the directory that gets published to 
github pages.

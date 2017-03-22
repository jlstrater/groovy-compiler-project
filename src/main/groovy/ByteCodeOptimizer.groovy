import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import optimizations.FirstPassClassVisitor
import utils.FileInfo

class ByteCodeOptimizer {

    List<String> classDirs = ['build/groovyc', 'build/indy', 'build/static']

    @SuppressWarnings('JavaIoPackageAccess')
    void processClassFiles(FileInfo fileInfo) {
        classDirs.each {
            String classFileInfo = it + '/' + fileInfo.filename + '.class'
            if (new File(classFileInfo).exists()) {
                ClassReader bytecodeReader = new ClassReader(new File(classFileInfo).bytes)
                ClassWriter bytecodeWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
                bytecodeReader.accept(new FirstPassClassVisitor(Opcodes.ASM4, bytecodeWriter), 0)

                File outputDir = new File(it + '/new')
                outputDir.mkdirs()
                File outputFile = new File(it + '/new/' + fileInfo.filename + '.class')
                outputFile.write bytecodeWriter.toByteArray().toString()
            }
        }
    }
}

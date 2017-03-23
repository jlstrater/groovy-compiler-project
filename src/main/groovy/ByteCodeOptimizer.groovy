
import static jdk.internal.org.objectweb.asm.Opcodes.ACONST_NULL
import static jdk.internal.org.objectweb.asm.Opcodes.ALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.ILOAD
import static jdk.internal.org.objectweb.asm.Opcodes.POP
import static jdk.internal.org.objectweb.asm.Opcodes.POP2

import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.tree.ClassNode
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode
import jdk.internal.org.objectweb.asm.tree.MethodNode

import utils.FileCompiler
import utils.FileInfo

class ByteCodeOptimizer {

    List<String> classDirs = ['build/groovyc', 'build/indy', 'build/static']

    protected static final int[] POP_CODES = [
            POP,
            POP2,
    ]

    // todo: add more codes
    protected static final int[] LOAD_CODES = [
            ACONST_NULL,
            ALOAD,
            ILOAD,
    ]

    @SuppressWarnings('JavaIoPackageAccess')
    List processClassFiles(FileInfo fileInfo) {
        classDirs.collect {
            String classFileInfo = it + '/' + fileInfo.filename + '.class'
            if (new File(classFileInfo).exists()) {
                ClassReader bytecodeReader = new ClassReader(new File(classFileInfo).bytes)
                ClassNode classNode = new ClassNode()
                bytecodeReader.accept(classNode, 0)

                List<MethodNode> methods = classNode.methods
                methods.each { iterateThroughMethodNode(it) }

                ClassWriter bytecodeWriter = new ClassWriter(0)
                classNode.accept(bytecodeWriter)

                File dir = new File(it + '/new')
                dir.mkdirs()
                String newClassFileInfo = it + '/new/' + fileInfo.filename + '.class'
                FileOutputStream out = new FileOutputStream(newClassFileInfo)
                out.write bytecodeWriter.toByteArray()

                return FileCompiler.javapOnBytecode(newClassFileInfo)
            }
        }
    }

    @SuppressWarnings('NoDef')
    void iterateThroughMethodNode(MethodNode methodNode) {
        ListIterator<AbstractInsnNode> nodes = methodNode.instructions.iterator()
        AbstractInsnNode prev = null
        while (nodes.hasNext()) {
            AbstractInsnNode current = nodes.next()
            if (current.opcode in POP_CODES && current.previous.opcode in LOAD_CODES) {
                methodNode.instructions.remove(current)
                methodNode.instructions.remove(prev)
            }
            prev = current
        }
    }
}

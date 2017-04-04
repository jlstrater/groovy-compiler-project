package com.strater.jenn

import static jdk.internal.org.objectweb.asm.Opcodes.ACONST_NULL
import static jdk.internal.org.objectweb.asm.Opcodes.ALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.GOTO
import static jdk.internal.org.objectweb.asm.Opcodes.ILOAD
import static jdk.internal.org.objectweb.asm.Opcodes.POP
import static jdk.internal.org.objectweb.asm.Opcodes.POP2

import groovy.util.logging.Slf4j

import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.tree.ClassNode
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode
import jdk.internal.org.objectweb.asm.tree.MethodNode

import com.strater.jenn.utils.FileCompiler
import com.strater.jenn.utils.FileInfo

@Slf4j
class ByteCodeOptimizer {

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

    Integer numLinesRemoved = 0

    List<OptimizationResult> processDirectory(String directory, String parent) {
        List<OptimizationResult> results = []
        File dir = new File(directory)
        dir.eachFile { File file ->
            if (file.isDirectory()) {
                results << processDirectory(file.path, parent + file.name + '/')
            } else {
                results << processClassFiles(new FileInfo(file.path), parent)
            }
        }
        results.flatten()
    }

    @SuppressWarnings('GStringAsMapKey')
    OptimizationResult processClassFiles(FileInfo classFileInfo, String outputDir) {
        numLinesRemoved = 0
        if (new File(classFileInfo.info).exists()) {
            ClassReader bytecodeReader = new ClassReader(new File(classFileInfo.info).bytes)
            ClassNode classNode = new ClassNode()
            bytecodeReader.accept(classNode, 0)

            List<MethodNode> methods = classNode.methods
            methods.each { iterateThroughMethodNode(it) }

            ClassWriter bytecodeWriter = new ClassWriter(0)
            classNode.accept(bytecodeWriter)

            File dir = new File(outputDir)
            dir.mkdirs()
            String newClassFileInfo = outputDir + classFileInfo.filename + '.class'
            FileOutputStream out = new FileOutputStream(newClassFileInfo)
            out.write bytecodeWriter.toByteArray()

            String compilationType = outputDir.tokenize('/').last()
            return new OptimizationResult(compilationType: compilationType,
                    text: FileCompiler.javapOnBytecode(newClassFileInfo), linesRemoved: numLinesRemoved,
                    filename: classFileInfo.filename,)
        }
    }

    @SuppressWarnings('NoDef')
    void iterateThroughMethodNode(MethodNode methodNode) {
        List jumpPoints = findGoTos(methodNode)
        ListIterator<AbstractInsnNode> nodes = methodNode.instructions.iterator()
        AbstractInsnNode prev = null
        while (nodes.hasNext()) {
            AbstractInsnNode current = nodes.next()

            if (current.opcode in POP_CODES && current.previous.opcode in LOAD_CODES) {
                if (current.index in jumpPoints || prev.index in jumpPoints) {
                    log.error 'Ack! Removing a line that the program jumps to!!!!'
                } else {
                    methodNode.instructions.remove(current)
                    methodNode.instructions.remove(prev)
                    numLinesRemoved += 2
                }
            }
            prev = current
        }
    }

    List findGoTos(MethodNode methodNode) {
        List jumpPoints = []
        ListIterator<AbstractInsnNode> nodes = methodNode.instructions.iterator()
        while (nodes.hasNext()) {
            AbstractInsnNode current = nodes.next()

            if (current.opcode == GOTO) {
                jumpPoints << current.next.index
            }
        }
        jumpPoints
    }

    class OptimizationResult {
        String compilationType
        String text
        Integer linesRemoved
        String filename
    }
}

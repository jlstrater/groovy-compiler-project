package com.strater.jenn

import static jdk.internal.org.objectweb.asm.Opcodes.AALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.AASTORE
import static jdk.internal.org.objectweb.asm.Opcodes.ACONST_NULL
import static jdk.internal.org.objectweb.asm.Opcodes.ALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.BALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.BASTORE
import static jdk.internal.org.objectweb.asm.Opcodes.CALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.CASTORE
import static jdk.internal.org.objectweb.asm.Opcodes.DALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.DASTORE
import static jdk.internal.org.objectweb.asm.Opcodes.DCONST_0
import static jdk.internal.org.objectweb.asm.Opcodes.DCONST_1
import static jdk.internal.org.objectweb.asm.Opcodes.DLOAD
import static jdk.internal.org.objectweb.asm.Opcodes.DSTORE
import static jdk.internal.org.objectweb.asm.Opcodes.DUP
import static jdk.internal.org.objectweb.asm.Opcodes.FALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.FASTORE
import static jdk.internal.org.objectweb.asm.Opcodes.FCONST_0
import static jdk.internal.org.objectweb.asm.Opcodes.FCONST_1
import static jdk.internal.org.objectweb.asm.Opcodes.FCONST_2
import static jdk.internal.org.objectweb.asm.Opcodes.FLOAD
import static jdk.internal.org.objectweb.asm.Opcodes.FSTORE
import static jdk.internal.org.objectweb.asm.Opcodes.GOTO
import static jdk.internal.org.objectweb.asm.Opcodes.IALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.IASTORE
import static jdk.internal.org.objectweb.asm.Opcodes.ICONST_0
import static jdk.internal.org.objectweb.asm.Opcodes.ICONST_1
import static jdk.internal.org.objectweb.asm.Opcodes.ICONST_2
import static jdk.internal.org.objectweb.asm.Opcodes.ICONST_4
import static jdk.internal.org.objectweb.asm.Opcodes.ICONST_5
import static jdk.internal.org.objectweb.asm.Opcodes.ILOAD
import static jdk.internal.org.objectweb.asm.Opcodes.LALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.LASTORE
import static jdk.internal.org.objectweb.asm.Opcodes.LCONST_0
import static jdk.internal.org.objectweb.asm.Opcodes.LCONST_1
import static jdk.internal.org.objectweb.asm.Opcodes.LDC
import static jdk.internal.org.objectweb.asm.Opcodes.LLOAD
import static jdk.internal.org.objectweb.asm.Opcodes.LSTORE
import static jdk.internal.org.objectweb.asm.Opcodes.POP
import static jdk.internal.org.objectweb.asm.Opcodes.ASTORE
import static jdk.internal.org.objectweb.asm.Opcodes.ISTORE
import static jdk.internal.org.objectweb.asm.Opcodes.SALOAD
import static jdk.internal.org.objectweb.asm.Opcodes.SASTORE

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
    ]

    // todo: add more codes
    protected static final int[] LOAD_CODES = [
            ACONST_NULL,
            ICONST_0,
            ICONST_1,
            ICONST_2,
            ICONST_1,
            ICONST_4,
            ICONST_5,
            LCONST_0,
            LCONST_1,
            FCONST_0,
            FCONST_1,
            FCONST_2,
            DCONST_0,
            DCONST_1,
            LDC,
            ALOAD,
            LLOAD,
            FLOAD,
            DLOAD,
            IALOAD,
            LALOAD,
            FALOAD,
            DALOAD,
            AALOAD,
            BALOAD,
            CALOAD,
            SALOAD,
            ILOAD,
    ]

    protected static final int[] STORE_CODES = [
            ISTORE,
            LSTORE,
            FSTORE,
            DSTORE,
            ASTORE,
            IASTORE,
            LASTORE,
            FASTORE,
            DASTORE,
            AASTORE,
            BASTORE,
            CASTORE,
            SASTORE,
    ]

    protected static final int[] DUP_CODES = [
            DUP,
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
        if (new File(classFileInfo.info).exists() && classFileInfo.extension == 'class') {
            ClassReader bytecodeReader = new ClassReader(new File(classFileInfo.info).bytes)
            ClassNode classNode = new ClassNode()
            bytecodeReader.accept(classNode, 0)

            List<MethodNode> methods = classNode.methods
            Integer numLinesBefore = methods.instructions*.size().sum()
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
                    filename: classFileInfo.filename, totalLines: numLinesBefore,)
        }
        File dir = new File(outputDir)
        dir.mkdirs()
        "cp $classFileInfo.info $outputDir".execute()
        new OptimizationResult(compilationType: 'none', text: 'N/A', linesRemoved: numLinesRemoved,
                filename: classFileInfo.filename, totalLines: 0,
        )
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
            } else if (current.opcode in STORE_CODES && current.previous.opcode in DUP_CODES &&
                    current?.next?.opcode in POP_CODES) {
                methodNode.instructions.remove(prev)
                methodNode.instructions.remove(nodes.next())
                numLinesRemoved += 2
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
                jumpPoints << current.next?.index
            }
        }
        jumpPoints
    }

    class OptimizationResult {
        String compilationType
        String text
        Integer linesRemoved
        Integer totalLines
        String filename
    }
}

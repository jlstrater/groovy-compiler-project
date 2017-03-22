package optimizations

import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.MethodVisitor

class FirstPassClassVisitor extends ClassVisitor {
    FirstPassClassVisitor(int api, ClassWriter cv) {
        super(api, cv)
    }

    FirstPassClassVisitor(int api) {
        super(api)
    }

    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
        new MethodInstructionOptimizer(api, mv)
    }
}

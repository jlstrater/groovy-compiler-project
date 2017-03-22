package optimizations

import static jdk.internal.org.objectweb.asm.Opcodes.*
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.tree.InsnNode
import jdk.internal.org.objectweb.asm.tree.MethodNode

@SuppressWarnings('NoWildcardImports')
class MethodInstructionOptimizer extends MethodVisitor {
    MethodInstructionOptimizer(int api, MethodVisitor mv) {
        super(api, mv)
    }

    protected static final int[] POP_CODES = [
        POP,
        POP2,
    ]

    // todo: add more codes
    protected static final int[] LOAD_CODES = [
        ACONST_NULL,
        ALOAD,
    ]

    void visitMethod(MethodNode methodNode) {
        Iterator<InsnNode> nodes = methodNode.instructions.iterator()
        InsnNode prev = null
        while (nodes.hasNext()) {
            InsnNode current = nodes.next()
            if (current.type == POP_CODES[0] || current.type == POP_CODES[1] ) {
                if (prev != null) {
                    if (prev.type == LOAD_CODES[0] || prev.type == LOAD_CODES[1]) {
                        methodNode.instructions.remove(prev)
                        methodNode.instructions.remove(current)
                    }
                }
            }
            prev = current
        }
    }
}

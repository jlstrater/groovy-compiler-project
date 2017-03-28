/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Jochen Hinrichsen
 */
class Random {
    static def IM = 139968
    static def IA = 3877
    static def IC = 29573
    static def last = 42D

    static def gen_random(Double max) {
        last = (last * IA + IC) % IM
        max * last / IM
    }

    public static void main(String[] args) {
        def n = (args.length == 0 ? 1 : args[0].toInteger()) - 1
        while ( n--) {
            gen_random(100D)
        }

        def nf = java.text.NumberFormat.getInstance()
        nf.setMaximumFractionDigits(9)
        nf.setMinimumFractionDigits(9)
        nf.setGroupingUsed(false)
        println nf.format(gen_random(100D))
    }
}

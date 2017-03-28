import com.sun.org.apache.xpath.internal.operations.Bool

/*
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * contributed by Pilho Kim
 */
class nsieve {
    Integer countSieve(m, primes) {
        Integer i, k
        Integer count = 0

        i = 2
        while (i <= m) {
            primes[i] = true
            i++
        }

        i = 2
        while (i <= m) {
            if (primes[i]) {
                k = i + i
                while (k <= m) {
                    primes[k] = false
                    k += i
                }
                count++
            }
            i++
        }
        return count
    }

    String padNumber(Integer number, Integer fieldLen) {
        String bareNumber = "" + number
        Integer numSpaces = fieldLen - bareNumber.length()
        StringBuffer sb = new StringBuffer(' ' * numSpaces)
        sb.append(bareNumber)
        return sb.toString()
    }

    public static void main(String[] args) {
        Integer n = 2
        if (args.length > 0)
            n = args[0].toInteger()
        if (n < 2)
            n = 2

        Integer m = (1 << n) * 10000
        Boolean flags = new boolean[m + 1]

        [n, n - 1, n - 2].each {
            Integer k = (1 << it) * 10000
            String s1 = padNumber(k, 8)
            String s2 = padNumber(countSieve(k, flags), 9)
            println("Primes up to $s1$s2")
        }
    }
}

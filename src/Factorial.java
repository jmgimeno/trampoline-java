public class Factorial {

    static long unsafeFactorial(long n) {
        if (n == 0L) return 1L;
        else return n * unsafeFactorial(n - 1L);
    }

    static TailRec<Long> factTrampolined(long n) {
        if (n == 0L)
            return new TailRec.Return<>(1L);
        else
            return new TailRec.Suspend<>(() -> factTrampolined(n - 1L))
                    .flatMap(
                            x -> new TailRec.Return<>(n * x)
                    );
    }

    static TailRec<Long> factTailRec(long n, long acc) {
        if (n == 0L)
            return new TailRec.Return<>(acc);
        else
            return new TailRec.Suspend<>(() -> factTailRec(n - 1L, n * acc));
    }

    static long safeFactorialTrampolined(long n) {
        return TailRec.runTrampoline(factTrampolined(n));
    }

    static long safeFactorialTailRec(long n) {
        return TailRec.runTrampoline(factTailRec(n, 1));
    }

    public static void main(String[] args) {
        System.out.println(unsafeFactorial(20L));
        System.out.println(safeFactorialTrampolined(20L));
        System.out.println(safeFactorialTailRec(20L));
    }
}

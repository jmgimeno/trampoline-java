public class Fibonacci {

    static long unsafeFibonacci(long n) {
        if (n <= 1L) return n;
        else return unsafeFibonacci(n - 1L) + unsafeFibonacci(n - 2L);
    }

    static TailRec<Long> fibTrampolined(long n) {
        if (n <= 1L)
            return new TailRec.Return<>(n);
        else
            return new TailRec.Suspend<>(() -> fibTrampolined(n - 1L))
                    .flatMap(
                            x -> new TailRec.Suspend<>(() -> fibTrampolined(n - 2L))
                                    .map(y -> x + y)
                    );
    }

    static long safeFibonacciTrampolined(long n) {
        return TailRec.runTrampoline(fibTrampolined(n));
    }

    public static void main(String[] args) {
        System.out.println(unsafeFibonacci(30L));
        System.out.println(safeFibonacciTrampolined(30L));
    }
}

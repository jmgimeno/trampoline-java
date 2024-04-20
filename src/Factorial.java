public class Factorial {

    static int unsafeFactorial(int n) {
        if (n == 0) return 1;
        else return n * unsafeFactorial(n - 1);
    }

    static TailRec<Integer> fact(int n) {
        if (n == 0)
            return new TailRec.Return<>(1);
        else
            return new TailRec.Suspend<>(() -> fact(n - 1))
                    .flatMap(
                            x -> new TailRec.Return<>(n * x)
                    );
    }

    static int safeFactorial(int n) {
        return TailRec.run(fact(n));
    }

    public static void main(String[] args) {
        System.out.println(safeFactorial(5));
    }
}

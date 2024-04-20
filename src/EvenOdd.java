public class EvenOdd {

    static boolean unsafeEven(long n) {
        if (n == 0L) return true;
        else return unsafeOdd(n - 1L);
    }

    static boolean unsafeOdd(long n) {
        if (n == 0L) return false;
        else return unsafeEven(n - 1L);
    }

    static TailRec<Boolean> evenTrampolined(long n) {
        if (n == 0L)
            return new TailRec.Return<>(true);
        else
            return new TailRec.Suspend<>(() -> oddTrampolined(n - 1L));
    }

    private static TailRec<Boolean> oddTrampolined(long n) {
        if (n == 0L)
            return new TailRec.Return<>(false);
        else
            return new TailRec.Suspend<>(() -> evenTrampolined(n - 1L));
    }

    public static void main(String[] args) {
        //System.out.println(unsafeEven(20_000L));
        System.out.println(TailRec.runTrampoline(evenTrampolined(200_000L)));
    }
}

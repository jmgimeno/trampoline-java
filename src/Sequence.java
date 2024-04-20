import java.util.List;

public class Sequence {
    public static void main(String[] args) {
        TailRec<Integer> tr1 = new TailRec.Return<>(1);
        TailRec<Integer> tr2 = new TailRec.Return<>(2);
        List<TailRec<Integer>> trs = List.of(tr1, tr2);
        TailRec<List<Integer>> sq = TailRec.sequence(trs);
        System.out.println(TailRec.runTrampoline(sq));
    }
}

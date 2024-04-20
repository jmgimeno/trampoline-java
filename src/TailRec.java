import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface TailRec<A> {

    default <B> TailRec<B> flatMap(Function<A, TailRec<B>> function) {
        return new FlatMap<>(this, function);
    }

    default <B> TailRec<B> map(Function<A, B> f) {
        return flatMap(f.andThen(Return::new));
    }

    record Return<A>(A a) implements TailRec<A> {
    }

    record Suspend<A>(Supplier<TailRec<A>> resume) implements TailRec<A> {
    }

    record FlatMap<A, B>(TailRec<A> sub, Function<A, TailRec<B>> k) implements TailRec<B> {
    }

    /*
    def run[A](tr: TailRec[A]): A = tr match {
        case Return(a) => a
        case Suspend(r) => run(r())
        case FlatMap(x, f) => x match {
            case Return(a) => run(f(a))
            case Suspend(r) => run(FlatMap(r(), f))
            case FlatMap(y, g) => run(y.flatMap(g(_) flatMap f))
    }
     */
    static  <A> A run(TailRec<A> t) {
        return switch (t) {
            case Return<A> r -> r.a();
            case Suspend<A> s  -> run(s.resume().get());
            case FlatMap<?, A> fm -> run(bind(fm));
        };
    }

    static <A> A runIter(TailRec<A> t) {
        while (!(t instanceof Return<A> r)) {
            t = switch (t) {
                case Suspend<A> s  -> s.resume().get();
                case FlatMap<?, A> fm -> bind(fm);
                default -> throw new IllegalStateException();
            };
        }
        return r.a();
    }

    static <A, B> TailRec<B> bind(FlatMap<A, B> fm) {
        return switch (fm.sub()) {
            case Return<A> r -> fm.k().apply(r.a());
            case Suspend<A> s -> new FlatMap<>(s.resume().get(), fm.k());
            case FlatMap<?, A> fm2 ->  combine(fm2, fm);
        };
    }

    static <A,B,C> TailRec<B> combine(FlatMap<A,C> fm, FlatMap<C,B> fm2) {
        return fm.sub().flatMap(a -> fm.k().apply(a).flatMap(c -> fm2.k().apply(c)));
    } // (TailRec<A>)
}

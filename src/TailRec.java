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

    static <A> A runTrampoline(TailRec<A> t) {
        while (!(t instanceof Return(var a))) {
            t = switch (t) {
                case Suspend(var resume) -> resume.get();
                case FlatMap<?, A> fm -> bind(fm);
                default -> throw new IllegalStateException();
            };
        }
        return a;
    }

    static <A, C> TailRec<A> bind(FlatMap<C, A> fm) {
        // to capture the existential type C
        return bind(fm.sub(), fm.k());
    }

    static <A, C> TailRec<A> bind(TailRec<C> sub, Function<C, TailRec<A>> k) {
        return switch (sub) {
            case Return(var c) -> k.apply(c);
            case Suspend(var resume) -> new FlatMap<>(resume.get(), k);
            case FlatMap<?, C> fm0 -> associativity(fm0, k);
        };
    }

    static <A, C, D> TailRec<A> associativity(FlatMap<D, C> fm0, Function<C, TailRec<A>> k) {
        // to capture the existential type D
        return associativity(fm0.sub(), fm0.k(), k);
    }

    static <A, C, D> TailRec<A> associativity(TailRec<D> sub0, Function<D, TailRec<C>> k0, Function<C, TailRec<A>> k) {
        return sub0.flatMap(c -> k0.apply(c).flatMap(k));
    }
}

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


    static <A, B> TailRec<B> bind(FlatMap<A, B> fm) {
        return switch (fm.sub()) {
            case Return(var a) -> fm.k().apply(a);
            case Suspend(var resume) -> new FlatMap<>(resume.get(), fm.k());
            case FlatMap<?, A> fm0 -> associativity(fm0, fm);
        };
    }

    static <A, B, C> TailRec<B> associativity(FlatMap<C, A> fm0, FlatMap<A, B> fm) {
        return fm0.sub().flatMap(c -> fm0.k().apply(c).flatMap(a -> fm.k().apply(a)));
    }
}

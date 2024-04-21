import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/*
sealed trait TailRec[A] {
    def map[B](f: A => B): TailRec[B] = flatMap(f andThen (Return(_)))
    def flatMap[B](f: A => TailRec[B]): TailRec[B] = FlatMap(this, f)
}

final case class Return[A](a: A) extends TailRec[A]
final case class Suspend[A](resume: () => TailRec[A]) extends TailRec[A]
final case class FlatMap[A, B](sub: TailRec[A], k: A => TailRec[B]) extends TailRec[B]
 */
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

    /*
    def sequence[A](ltt: List[TailRec[A]]): TailRec[List[A]] =
        ltt.reverse.foldLeft(Return(Nil): TailRec[List[A]]) { (tla, ta) =>
            ta map ((_: A) :: (_: List[A])).curried flatMap tla.map
    */

    static <A> TailRec<List<A>> sequence(List<TailRec<A>> trs) {
        TailRec<List<A>> result = new Return<>(new ArrayList<>());
        for (TailRec<A> tr : trs) {
            final var r = result;
            result = tr.flatMap(a -> r.map(as -> {
                as.add(a);
                return as;
            }));
        }
        return result;
    }
}



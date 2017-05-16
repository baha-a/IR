package finalir;


public interface Callable2<T> {
    void call(T t);
}

interface Callable<T> {
    T call(T p1, T p2,int target);
}
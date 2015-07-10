package javax.util.streamex;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/* package */class UnknownSizeSpliterator<T> implements Spliterator<T> {
    static final int BATCH_UNIT = 1 << 10; // batch array size increment
    static final int MAX_BATCH = 1 << 25; // max batch array size;
    private Iterator<? extends T> it;
    private Object[] array;
    private int index, fence;

    public UnknownSizeSpliterator(Iterator<? extends T> iterator) {
        this.it = iterator;
    }

    UnknownSizeSpliterator(Object[] array, int index, int fence) {
        this.array = array;
        this.index = index;
        this.fence = fence;
    }

    @Override
    public Spliterator<T> trySplit() {
        Iterator<? extends T> i = it;
        if (i != null) {
            int n = fence + BATCH_UNIT;
            if (n > MAX_BATCH)
                n = MAX_BATCH;
            Object[] a = new Object[n];
            int j = 0;
            do {
                a[j] = i.next();
            } while (++j < n && i.hasNext());
            fence = j;
            if (i.hasNext()) {
                return new UnknownSizeSpliterator<>(a, 0, j);
            }
            it = null;
            array = a;
        }
        int lo = index, mid = (lo + fence) >>> 1;
        return (lo >= mid) ? null : new UnknownSizeSpliterator<>(array, lo, index = mid);
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        if (it != null)
            it.forEachRemaining(action);
        else {
            Object[] a = array;
            int i = index, hi = fence;
            if (i < hi && a.length >= hi) {
                do {
                    action.accept((T) a[i]);
                } while (++i < hi);
            }
        }
        index = fence;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (it != null) {
            if (it.hasNext()) {
                action.accept(it.next());
                return true;
            } else {
                it = null;
                index = fence;
            }
        } else if (index < fence) {
            action.accept((T) array[index++]);
            return true;
        }
        return false;
    }

    @Override
    public long estimateSize() {
        if (it == null) {
            return fence - index;
        }
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        if (it == null) {
            return SIZED | SUBSIZED | ORDERED;
        }
        return ORDERED;
    }
}

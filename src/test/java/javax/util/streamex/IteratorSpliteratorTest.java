package javax.util.streamex;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import org.junit.Test;

public class IteratorSpliteratorTest {
    @Test
    public void testSplit() {
        List<Integer> input = IntStreamEx.range(100).boxed().toList();
        IteratorSpliterator<Integer> spliterator = new IteratorSpliterator<>(input.iterator());
        assertEquals(Long.MAX_VALUE, spliterator.estimateSize());
        AtomicInteger count = new AtomicInteger();
        assertTrue(spliterator.tryAdvance(count::addAndGet));
        assertTrue(spliterator.tryAdvance(count::addAndGet));
        assertEquals(1, count.get());
        assertEquals(Long.MAX_VALUE, spliterator.estimateSize());
        Spliterator<Integer> spliterator2 = spliterator.trySplit();
        assertEquals(49, spliterator.estimateSize());
        assertEquals(49, spliterator2.estimateSize());
        assertTrue(spliterator.tryAdvance(count::addAndGet));
        assertTrue(spliterator2.tryAdvance(count::addAndGet));
        assertEquals(48, spliterator.estimateSize());
        assertEquals(48, spliterator2.estimateSize());
        assertEquals(54, count.get());
    }
    
    @Test
    public void testAsStream() {
        List<Integer> input = IntStreamEx.range(100).boxed().toList();
        assertEquals(4950, StreamSupport.stream(new IteratorSpliterator<>(input.iterator()), false).mapToInt(x -> x).sum());
        assertEquals(4950, StreamSupport.stream(new IteratorSpliterator<>(input.iterator()), true).mapToInt(x -> x).sum());
        
        input = IntStreamEx.range(5000).boxed().toList();
        assertEquals(12497500, StreamSupport.stream(new IteratorSpliterator<>(input.iterator()), false).mapToInt(x -> x).sum());
        assertEquals(12497500, StreamSupport.stream(new IteratorSpliterator<>(input.iterator()), true).mapToInt(x -> x).sum());
    }
}

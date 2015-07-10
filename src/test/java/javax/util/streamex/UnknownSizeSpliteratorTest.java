package javax.util.streamex;

import static org.junit.Assert.*;
import static javax.util.streamex.TestHelpers.*;

import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import org.junit.Test;

public class UnknownSizeSpliteratorTest {
    @Test
    public void testSplit() {
        List<Integer> input = IntStreamEx.range(100).boxed().toList();
        UnknownSizeSpliterator<Integer> spliterator = new UnknownSizeSpliterator<>(input.iterator());
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
    public void testSpliterator() {
        for(int size: new int[] {1, 5, 100, 1000, 1023, 1024, 1025, 2049}) {
            List<Integer> input = IntStreamEx.range(size).boxed().toList();
            checkSpliterator("100", input, () -> new UnknownSizeSpliterator<>(input.iterator()));
        }
    }
    
    @Test
    public void testAsStream() {
        List<Integer> input = IntStreamEx.range(100).boxed().toList();
        assertEquals(4950, StreamSupport.stream(new UnknownSizeSpliterator<>(input.iterator()), false).mapToInt(x -> x).sum());
        assertEquals(4950, StreamSupport.stream(new UnknownSizeSpliterator<>(input.iterator()), true).mapToInt(x -> x).sum());
        
        input = IntStreamEx.range(5000).boxed().toList();
        assertEquals(12497500, StreamSupport.stream(new UnknownSizeSpliterator<>(input.iterator()), false).mapToInt(x -> x).sum());
        assertEquals(12497500, StreamSupport.stream(new UnknownSizeSpliterator<>(input.iterator()), true).mapToInt(x -> x).sum());
    }
}

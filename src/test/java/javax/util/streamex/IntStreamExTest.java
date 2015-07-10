/*
 * Copyright 2015 Tagir Valeev
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.util.streamex;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntStreamExTest {
    @Test
    public void testCreate() {
        assertArrayEquals(new int[] {}, IntStreamEx.empty().toArray());
        // double test is intended
        assertArrayEquals(new int[] {}, IntStreamEx.empty().toArray());
        assertArrayEquals(new int[] { 1 }, IntStreamEx.of(1).toArray());
        assertArrayEquals(new int[] { 1 }, IntStreamEx.of(OptionalInt.of(1)).toArray());
        assertArrayEquals(new int[] {}, IntStreamEx.of(OptionalInt.empty()).toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.of(1, 2, 3).toArray());
        assertArrayEquals(new int[] { 4, 6 }, IntStreamEx.of(new int[] { 2, 4, 6, 8, 10 }, 1, 3).toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.of(new byte[] { 1, 2, 3 }).toArray());
        assertArrayEquals(new int[] { 4, 6 }, IntStreamEx.of(new byte[] { 2, 4, 6, 8, 10 }, 1, 3).toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.of(new short[] { 1, 2, 3 }).toArray());
        assertArrayEquals(new int[] { 4, 6 }, IntStreamEx.of(new short[] { 2, 4, 6, 8, 10 }, 1, 3).toArray());
        assertArrayEquals(new int[] { 'a', 'b', 'c' }, IntStreamEx.of('a', 'b', 'c').toArray());
        assertArrayEquals(new int[] { '1', 'b' }, IntStreamEx.of(new char[] { 'a', '1', 'b', '2', 'c', '3' }, 1, 3)
                .toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.of(IntStream.of(1, 2, 3)).toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.of(Arrays.asList(1, 2, 3)).toArray());
        assertArrayEquals(new int[] { 0, 1, 2 }, IntStreamEx.range(3).toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.range(1, 4).toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.rangeClosed(1, 3).toArray());
        assertArrayEquals(new int[] { 1, 2, 4, 8, 16 }, IntStreamEx.iterate(1, x -> x * 2).limit(5).toArray());
        assertArrayEquals(new int[] { 1, 1, 1, 1 }, IntStreamEx.generate(() -> 1).limit(4).toArray());
        assertArrayEquals(new int[] { 1, 1, 1, 1 }, IntStreamEx.constant(1, 4).toArray());
        assertArrayEquals(new int[] { 'a', 'b', 'c' }, IntStreamEx.ofChars("abc").toArray());
        assertEquals(10, IntStreamEx.of(new Random(), 10).count());
        assertTrue(IntStreamEx.of(new Random(), 100, 1, 10).allMatch(x -> x >= 1 && x < 10));
        assertArrayEquals(IntStreamEx.of(new Random(1), 100, 1, 10).toArray(), IntStreamEx.of(new Random(1), 1, 10)
                .limit(100).toArray());

        IntStream stream = IntStreamEx.of(1, 2, 3);
        assertSame(stream, IntStreamEx.of(stream));

        assertArrayEquals(new int[] { 4, 2, 0, -2, -4 },
            IntStreamEx.zip(new int[] { 5, 4, 3, 2, 1 }, new int[] { 1, 2, 3, 4, 5 }, (a, b) -> a - b).toArray());

        assertArrayEquals(new int[] { 1, 5, 3 }, IntStreamEx.of(Spliterators.spliterator(new int[] { 1, 5, 3 }, 0))
                .toArray());
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testArrayOffsetUnderflow() {
        IntStreamEx.of(new byte[] { 2, 4, 6, 8, 10 }, -1, 3).findAny();
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testArrayOffsetWrong() {
        IntStreamEx.of(new byte[] { 2, 4, 6, 8, 10 }, 3, 1).findAny();
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testArrayLengthOverflow() {
        IntStreamEx.of(new byte[] { 2, 4, 6, 8, 10 }, 3, 6).findAny();
    }

    @Test
    public void testArrayLengthOk() {
        assertEquals(10, IntStreamEx.of(new byte[] { 2, 4, 6, 8, 10 }, 3, 5).skip(1).findFirst().getAsInt());
    }

    @Test
    public void testOfIndices() {
        assertArrayEquals(new int[] {}, IntStreamEx.ofIndices(new int[0]).toArray());
        assertArrayEquals(new int[] { 0, 1, 2 }, IntStreamEx.ofIndices(new int[] { 5, -100, 1 }).toArray());
        assertArrayEquals(new int[] { 0, 2 }, IntStreamEx.ofIndices(new int[] { 5, -100, 1 }, i -> i > 0).toArray());
        assertArrayEquals(new int[] { 0, 1, 2 }, IntStreamEx.ofIndices(new long[] { 5, -100, 1 }).toArray());
        assertArrayEquals(new int[] { 0, 2 }, IntStreamEx.ofIndices(new long[] { 5, -100, 1 }, i -> i > 0).toArray());
        assertArrayEquals(new int[] { 0, 1, 2 }, IntStreamEx.ofIndices(new double[] { 5, -100, 1 }).toArray());
        assertArrayEquals(new int[] { 0, 2 }, IntStreamEx.ofIndices(new double[] { 5, -100, 1 }, i -> i > 0).toArray());
        assertArrayEquals(new int[] { 0, 1, 2 }, IntStreamEx.ofIndices(new String[] { "a", "b", "c" }).toArray());
        assertArrayEquals(new int[] { 1 }, IntStreamEx.ofIndices(new String[] { "a", "", "c" }, String::isEmpty)
                .toArray());
        assertArrayEquals(new int[] { 0, 1, 2 }, IntStreamEx.ofIndices(Arrays.asList("a", "b", "c")).toArray());
        assertArrayEquals(new int[] { 1 }, IntStreamEx.ofIndices(Arrays.asList("a", "", "c"), String::isEmpty)
                .toArray());
    }

    @Test
    public void testBasics() {
        assertFalse(IntStreamEx.of(1).isParallel());
        assertTrue(IntStreamEx.of(1).parallel().isParallel());
        assertFalse(IntStreamEx.of(1).parallel().sequential().isParallel());
        AtomicInteger i = new AtomicInteger();
        try (IntStreamEx s = IntStreamEx.of(1).onClose(() -> i.incrementAndGet())) {
            assertEquals(1, s.count());
        }
        assertEquals(1, i.get());
        assertEquals(6, IntStreamEx.range(0, 4).sum());
        assertEquals(3, IntStreamEx.range(0, 4).max().getAsInt());
        assertEquals(0, IntStreamEx.range(0, 4).min().getAsInt());
        assertEquals(1.5, IntStreamEx.range(0, 4).average().getAsDouble(), 0.000001);
        assertEquals(4, IntStreamEx.range(0, 4).summaryStatistics().getCount());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.range(0, 5).skip(1).limit(3).toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.of(3, 1, 2).sorted().toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.of(1, 2, 1, 3, 2).distinct().toArray());
        assertArrayEquals(new int[] { 2, 4, 6 }, IntStreamEx.range(1, 4).map(x -> x * 2).toArray());
        assertArrayEquals(new long[] { 2, 4, 6 }, IntStreamEx.range(1, 4).mapToLong(x -> x * 2).toArray());
        assertArrayEquals(new double[] { 2, 4, 6 }, IntStreamEx.range(1, 4).mapToDouble(x -> x * 2).toArray(), 0.0);
        assertArrayEquals(new int[] { 1, 3 }, IntStreamEx.range(0, 5).filter(x -> x % 2 == 1).toArray());
        assertEquals(6, IntStreamEx.of(1, 2, 3).reduce(Integer::sum).getAsInt());
        assertEquals(Integer.MAX_VALUE, IntStreamEx.rangeClosed(1, Integer.MAX_VALUE).spliterator()
                .getExactSizeIfKnown());

        assertTrue(IntStreamEx.of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.ORDERED));
        assertFalse(IntStreamEx.of(1, 2, 3).unordered().spliterator().hasCharacteristics(Spliterator.ORDERED));
    }

    @Test
    public void testFlatMap() {
        long[][] vals = { { 1, 2, 3 }, { 2, 3, 4 }, { 5, 4, Long.MAX_VALUE, Long.MIN_VALUE } };
        assertArrayEquals(new long[] { 1, 2, 3, 2, 3, 4, 5, 4, Long.MAX_VALUE, Long.MIN_VALUE },
            IntStreamEx.ofIndices(vals).flatMapToLong(idx -> Arrays.stream(vals[idx])).toArray());
        String expected = IntStream.range(0, 200).boxed()
                .flatMap(i -> IntStream.range(0, i).<String> mapToObj(j -> i + ":" + j))
                .collect(Collectors.joining("/"));
        String res = IntStreamEx.range(200).flatMapToObj(i -> IntStreamEx.range(i).mapToObj(j -> i + ":" + j))
                .joining("/");
        String parallel = IntStreamEx.range(200).parallel()
                .flatMapToObj(i -> IntStreamEx.range(i).mapToObj(j -> i + ":" + j)).joining("/");
        assertEquals(expected, res);
        assertEquals(expected, parallel);

        double[] fractions = IntStreamEx.range(1, 5)
                .flatMapToDouble(i -> IntStreamEx.range(1, i).mapToDouble(j -> ((double) j) / i)).toArray();
        assertArrayEquals(new double[] { 1 / 2.0, 1 / 3.0, 2 / 3.0, 1 / 4.0, 2 / 4.0, 3 / 4.0 }, fractions, 0.000001);
    }

    @Test
    public void testElements() {
        assertEquals(Arrays.asList("f", "d", "b"), IntStreamEx.of(5, 3, 1).elements("abcdef".split("")).toList());
        assertEquals(Arrays.asList("f", "d", "b"),
            IntStreamEx.of(5, 3, 1).elements(Arrays.asList("a", "b", "c", "d", "e", "f")).toList());
        assertArrayEquals(new int[] { 10, 6, 2 }, IntStreamEx.of(5, 3, 1).elements(new int[] { 0, 2, 4, 6, 8, 10 })
                .toArray());
        assertArrayEquals(new long[] { 10, 6, 2 }, IntStreamEx.of(5, 3, 1).elements(new long[] { 0, 2, 4, 6, 8, 10 })
                .toArray());
        assertArrayEquals(new double[] { 10, 6, 2 },
            IntStreamEx.of(5, 3, 1).elements(new double[] { 0, 2, 4, 6, 8, 10 }).toArray(), 0.0);
    }

    @Test
    public void testPrepend() {
        assertArrayEquals(new int[] { -1, 0, 1, 2, 3 }, IntStreamEx.of(1, 2, 3).prepend(-1, 0).toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.of(1, 2, 3).prepend().toArray());
        assertArrayEquals(new int[] { 10, 11, 0, 1, 2, 3 }, IntStreamEx.range(0, 4).prepend(IntStreamEx.range(10, 12))
                .toArray());
    }

    @Test
    public void testAppend() {
        assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, IntStreamEx.of(1, 2, 3).append(4, 5).toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.of(1, 2, 3).append().toArray());
        assertArrayEquals(new int[] { 0, 1, 2, 3, 10, 11 }, IntStreamEx.range(0, 4).append(IntStreamEx.range(10, 12))
                .toArray());
    }

    @Test
    public void testHas() {
        assertTrue(IntStreamEx.range(1, 4).has(3));
        assertFalse(IntStreamEx.range(1, 4).has(4));
    }

    @Test
    public void testWithout() {
        assertArrayEquals(new int[] { 1, 2 }, IntStreamEx.range(1, 4).without(3).toArray());
        assertArrayEquals(new int[] { 1, 2, 3 }, IntStreamEx.range(1, 4).without(5).toArray());
    }

    @Test
    public void testRanges() {
        assertArrayEquals(new int[] { 5, 4, Integer.MAX_VALUE }, IntStreamEx.of(1, 5, 3, 4, -1, Integer.MAX_VALUE)
                .greater(3).toArray());
        assertArrayEquals(new int[] { 5, 3, 4, Integer.MAX_VALUE }, IntStreamEx.of(1, 5, 3, 4, -1, Integer.MAX_VALUE)
                .atLeast(3).toArray());
        assertArrayEquals(new int[] { 1, -1 }, IntStreamEx.of(1, 5, 3, 4, -1, Integer.MAX_VALUE).less(3).toArray());
        assertArrayEquals(new int[] { 1, 3, -1 }, IntStreamEx.of(1, 5, 3, 4, -1, Integer.MAX_VALUE).atMost(3).toArray());
    }

    @Test
    public void testToBitSet() {
        assertEquals("{0, 1, 2, 3, 4}", IntStreamEx.range(5).toBitSet().toString());
        assertEquals("{0, 2, 3, 4, 10}", IntStreamEx.of(0, 2, 0, 3, 0, 4, 0, 10).parallel().toBitSet().toString());
    }

    @Test
    public void testAs() {
        assertEquals(4, IntStreamEx.range(0, 5).asLongStream().findAny(x -> x > 3).getAsLong());
        assertEquals(4.0, IntStreamEx.range(0, 5).asDoubleStream().findAny(x -> x > 3).getAsDouble(), 0.0);
    }

    @Test
    public void testFind() {
        assertEquals(6, IntStreamEx.range(1, 10).findFirst(i -> i > 5).getAsInt());
        assertFalse(IntStreamEx.range(1, 10).findAny(i -> i > 10).isPresent());
    }

    @Test
    public void testRemove() {
        assertArrayEquals(new int[] { 1, 2 }, IntStreamEx.of(1, 2, 3).remove(x -> x > 2).toArray());
    }

    @Test
    public void testSort() {
        assertArrayEquals(new int[] { 0, 3, 6, 1, 4, 7, 2, 5, 8 },
            IntStreamEx.range(0, 9).sortedByInt(i -> i % 3 * 3 + i / 3).toArray());
        assertArrayEquals(new int[] { 0, 3, 6, 1, 4, 7, 2, 5, 8 },
            IntStreamEx.range(0, 9).sortedByLong(i -> (long) i % 3 * Integer.MAX_VALUE + i / 3).toArray());
        assertArrayEquals(new int[] { 8, 7, 6, 5, 4, 3, 2, 1 }, IntStreamEx.range(1, 9).sortedByDouble(i -> 1.0 / i)
                .toArray());
        assertArrayEquals(new int[] { 10, 11, 5, 6, 7, 8, 9 }, IntStreamEx.range(5, 12).sortedBy(String::valueOf)
                .toArray());
        assertArrayEquals(new int[] { Integer.MAX_VALUE, 1000, 1, 0, -10, Integer.MIN_VALUE },
            IntStreamEx.of(0, 1, 1000, -10, Integer.MIN_VALUE, Integer.MAX_VALUE).reverseSorted().toArray());
    }

    @Test
    public void testToString() {
        assertEquals("LOWERCASE", IntStreamEx.ofChars("lowercase").map(c -> Character.toUpperCase((char) c))
                .charsToString());
        assertEquals("LOWERCASE", IntStreamEx.ofCodePoints("lowercase").map(Character::toUpperCase)
                .codePointsToString());
    }

    @Test
    public void testMinMax() {
        assertEquals(9, IntStreamEx.range(5, 12).max((a, b) -> String.valueOf(a).compareTo(String.valueOf(b)))
                .getAsInt());
        assertEquals(10, IntStreamEx.range(5, 12).min((a, b) -> String.valueOf(a).compareTo(String.valueOf(b)))
                .getAsInt());
        assertEquals(9, IntStreamEx.range(5, 12).maxBy(String::valueOf).getAsInt());
        assertEquals(10, IntStreamEx.range(5, 12).minBy(String::valueOf).getAsInt());
        assertEquals(5, IntStreamEx.range(5, 12).maxByDouble(x -> 1.0 / x).getAsInt());
        assertEquals(11, IntStreamEx.range(5, 12).minByDouble(x -> 1.0 / x).getAsInt());
        assertEquals(29, IntStreamEx.of(15, 8, 31, 47, 19, 29).maxByInt(x -> x % 10 * 10 + x / 10).getAsInt());
        assertEquals(31, IntStreamEx.of(15, 8, 31, 47, 19, 29).minByInt(x -> x % 10 * 10 + x / 10).getAsInt());
        assertEquals(29, IntStreamEx.of(15, 8, 31, 47, 19, 29).maxByLong(x -> Long.MIN_VALUE + x % 10 * 10 + x / 10)
                .getAsInt());
        assertEquals(31, IntStreamEx.of(15, 8, 31, 47, 19, 29).minByLong(x -> Long.MIN_VALUE + x % 10 * 10 + x / 10)
                .getAsInt());

        Supplier<IntStreamEx> s = () -> IntStreamEx.of(1, 50, 120, 35, 130, 12, 0);
        IntUnaryOperator intKey = x -> String.valueOf(x).length();
        IntToLongFunction longKey = x -> String.valueOf(x).length();
        IntToDoubleFunction doubleKey = x -> String.valueOf(x).length();
        IntFunction<Integer> objKey = x -> String.valueOf(x).length();
        List<Function<IntStreamEx, OptionalInt>> minFns = Arrays.asList(is -> is.minByInt(intKey), 
            is -> is.minByLong(longKey), is -> is.minByDouble(doubleKey), is -> is.minBy(objKey));
        List<Function<IntStreamEx, OptionalInt>> maxFns = Arrays.asList(is -> is.maxByInt(intKey), 
            is -> is.maxByLong(longKey), is -> is.maxByDouble(doubleKey), is -> is.maxBy(objKey));
        minFns.forEach(fn -> assertEquals(1, fn.apply(s.get()).getAsInt()));
        minFns.forEach(fn -> assertEquals(1, fn.apply(s.get().parallel()).getAsInt()));
        maxFns.forEach(fn -> assertEquals(120, fn.apply(s.get()).getAsInt()));
        maxFns.forEach(fn -> assertEquals(120, fn.apply(s.get().parallel()).getAsInt()));
    }
    
    private IntStreamEx dropLast(IntStreamEx s) {
        return s.pairMap((a, b) -> a);
    }

    @Test
    public void testPairMap() {
        assertEquals(0, IntStreamEx.range(0).pairMap(Integer::sum).count());
        assertEquals(0, IntStreamEx.range(1).pairMap(Integer::sum).count());
        assertEquals(Collections.singletonMap(1, 9999L), IntStreamEx.range(10000).pairMap((a, b) -> b - a).boxed()
                .groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(Collections.singletonMap(1, 9999L), IntStreamEx.range(10000).parallel().pairMap((a, b) -> b - a)
                .boxed().groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(
            "Test Capitalization Stream",
            IntStreamEx
                    .ofChars("test caPiTaliZation streaM")
                    .parallel()
                    .prepend(0)
                    .pairMap(
                        (c1, c2) -> !Character.isLetter(c1) && Character.isLetter(c2) ? Character.toTitleCase(c2)
                                : Character.toLowerCase(c2)).charsToString());
        assertArrayEquals(IntStreamEx.range(9999).toArray(), dropLast(IntStreamEx.range(10000)).toArray());

        int data[] = new Random(1).ints(1000, 1, 1000).toArray();
        int[] expected = new int[data.length - 1];
        int lastSquare = data[0] * data[0];
        for (int i = 0; i < expected.length; i++) {
            int newSquare = data[i + 1] * data[i + 1];
            expected[i] = newSquare - lastSquare;
            lastSquare = newSquare;
        }
        int[] result = IntStreamEx.of(data).map(x -> x * x).pairMap((a, b) -> b - a).toArray();
        assertArrayEquals(expected, result);

        assertEquals(1, IntStreamEx.range(1000).map(x -> x * x).pairMap((a, b) -> b - a).pairMap((a, b) -> b - a)
                .distinct().count());

        assertArrayEquals(IntStreamEx.constant(1, 100).toArray(), IntStreamEx.iterate(0, i -> i + 1).parallel()
                .pairMap((a, b) -> b - a).limit(100).toArray());
    }

    @Test
    public void testToByteArray() {
        byte[] expected = new byte[10000];
        for (int i = 0; i < expected.length; i++)
            expected[i] = (byte) i;
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).toByteArray());
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).parallel().toByteArray());
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).greater(-1).toByteArray());
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).parallel().greater(-1).toByteArray());
    }

    @Test
    public void testToCharArray() {
        char[] expected = new char[10000];
        for (int i = 0; i < expected.length; i++)
            expected[i] = (char) i;
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).toCharArray());
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).parallel().toCharArray());
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).greater(-1).toCharArray());
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).parallel().greater(-1).toCharArray());
    }

    @Test
    public void testToShortArray() {
        short[] expected = new short[10000];
        for (int i = 0; i < expected.length; i++)
            expected[i] = (short) i;
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).toShortArray());
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).parallel().toShortArray());
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).greater(-1).toShortArray());
        assertArrayEquals(expected, IntStreamEx.range(0, 10000).parallel().greater(-1).toShortArray());
    }

    @Test
    public void testJoining() {
        assertEquals("0,1,2,3,4,5,6,7,8,9", IntStreamEx.range(10).joining(","));
        assertEquals("0,1,2,3,4,5,6,7,8,9", IntStreamEx.range(10).parallel().joining(","));
        assertEquals("[0,1,2,3,4,5,6,7,8,9]", IntStreamEx.range(10).joining(",", "[", "]"));
        assertEquals("[0,1,2,3,4,5,6,7,8,9]", IntStreamEx.range(10).parallel().joining(",", "[", "]"));
    }

    @Test
    public void testMapToEntry() {
        Map<Integer, List<Integer>> result = IntStreamEx.range(10).mapToEntry(x -> x % 2, x -> x).grouping();
        assertEquals(Arrays.asList(0, 2, 4, 6, 8), result.get(0));
        assertEquals(Arrays.asList(1, 3, 5, 7, 9), result.get(1));
    }

    static final class HundredIterator implements PrimitiveIterator.OfInt {
        int i = 0;

        @Override
        public boolean hasNext() {
            return i < 100;
        }

        @Override
        public int nextInt() {
            return i++;
        }
    }

    @Test
    public void testRecreate() {
        Set<Integer> expected = IntStreamEx.range(1, 100).boxed().toSet();
        assertEquals(
            expected,
            IntStreamEx
                    .of(StreamSupport.intStream(
                        Spliterators.spliteratorUnknownSize(new HundredIterator(), Spliterator.ORDERED), false))
                    .skip(1).boxed().toSet());
        assertEquals(
            expected,
            IntStreamEx
                    .of(StreamSupport.intStream(
                        Spliterators.spliteratorUnknownSize(new HundredIterator(), Spliterator.ORDERED), true)).skip(1)
                    .boxed().toCollection(HashSet<Integer>::new));
        assertEquals(
            expected,
            IntStreamEx
                    .of(StreamSupport.intStream(
                        Spliterators.spliteratorUnknownSize(new HundredIterator(), Spliterator.ORDERED), true))
                    .skipOrdered(1).boxed().toSet());
        assertEquals(
            expected,
            IntStreamEx
                    .of(StreamSupport.intStream(
                        Spliterators.spliterator(new HundredIterator(), 100, Spliterator.ORDERED
                            | Spliterator.CONCURRENT), true)).skipOrdered(1).boxed().toSet());
        assertEquals(
            expected,
            IntStreamEx
                    .of(StreamSupport.intStream(
                        Spliterators.spliteratorUnknownSize(new HundredIterator(), Spliterator.ORDERED), true))
                    .unordered().skipOrdered(1).boxed().toCollection(HashSet<Integer>::new));

        assertEquals(expected, IntStreamEx.iterate(0, i -> i + 1).skip(1).greater(0).limit(99).boxed().toSet());
        assertEquals(500, (int) IntStreamEx.iterate(0, i -> i + 1).skipOrdered(1).greater(0).boxed().parallel()
                .findAny(i -> i == 500).get());
        assertEquals(expected, IntStreamEx.iterate(0, i -> i + 1).skipOrdered(1).greater(0).limit(99).boxed()
                .parallel().toSet());
    }
}

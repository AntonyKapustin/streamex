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

import static org.junit.Assert.*;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class DoubleCollectorTest {
    @Test
    public void testJoining() {
        String expected = IntStream.range(0, 10000).asDoubleStream().mapToObj(String::valueOf)
                .collect(Collectors.joining(", "));
        assertEquals(expected, IntStreamEx.range(10000).asDoubleStream().collect(DoubleCollector.joining(", ")));
        assertEquals(expected,
            IntStreamEx.range(10000).asDoubleStream().parallel().collect(DoubleCollector.joining(", ")));
        String expected2 = IntStreamEx.range(0, 1000).asDoubleStream().boxed().toList().toString();
        assertEquals(expected2,
            IntStreamEx.range(1000).asDoubleStream().collect(DoubleCollector.joining(", ", "[", "]")));
        assertEquals(expected2,
            IntStreamEx.range(1000).asDoubleStream().parallel().collect(DoubleCollector.joining(", ", "[", "]")));
    }

    @Test
    public void testCounting() {
        assertEquals(5000L,
            (long) IntStreamEx.range(10000).asDoubleStream().atLeast(5000).collect(DoubleCollector.counting()));
        assertEquals(
            5000L,
            (long) IntStreamEx.range(10000).asDoubleStream().parallel().atLeast(5000)
                    .collect(DoubleCollector.counting()));
        assertEquals(5000,
            (int) IntStreamEx.range(10000).asDoubleStream().atLeast(5000).collect(DoubleCollector.countingInt()));
        assertEquals(
            5000,
            (int) IntStreamEx.range(10000).asDoubleStream().parallel().atLeast(5000)
                    .collect(DoubleCollector.countingInt()));
    }

    @Test
    public void testSumming() {
        assertEquals(3725, IntStreamEx.range(100).asDoubleStream().atLeast(50).collect(DoubleCollector.summing()), 0.0);
        assertEquals(3725,
            IntStreamEx.range(100).asDoubleStream().parallel().atLeast(50).collect(DoubleCollector.summing()), 0.0);
    }

    @Test
    public void testMin() {
        assertEquals(50, IntStreamEx.range(100).asDoubleStream().atLeast(50).collect(DoubleCollector.min())
                .getAsDouble(), 0.0);
        assertFalse(IntStreamEx.range(100).asDoubleStream().atLeast(200).collect(DoubleCollector.min()).isPresent());
    }

    @Test
    public void testMax() {
        assertEquals(99, IntStreamEx.range(100).asDoubleStream().atLeast(50).collect(DoubleCollector.max())
                .getAsDouble(), 0.0);
        assertEquals(99, IntStreamEx.range(100).asDoubleStream().parallel().atLeast(50).collect(DoubleCollector.max())
                .getAsDouble(), 0.0);
        assertFalse(IntStreamEx.range(100).asDoubleStream().atLeast(200).collect(DoubleCollector.max()).isPresent());
    }

    @Test
    public void testToArray() {
        assertArrayEquals(new double[] { 0, 1, 2, 3, 4 },
            IntStreamEx.of(0, 1, 2, 3, 4).asDoubleStream().collect(DoubleCollector.toArray()), 0.0);
        assertArrayEquals(IntStreamEx.range(1000).asDoubleStream().toFloatArray(), IntStreamEx.range(1000).parallel()
                .asDoubleStream().collect(DoubleCollector.toFloatArray()), 0.0f);
    }

    @Test
    public void testPartitioning() {
        double[] expectedEven = IntStream.range(0, 1000).asDoubleStream().map(i -> i * 2).toArray();
        double[] expectedOdd = IntStream.range(0, 1000).asDoubleStream().map(i -> i * 2 + 1).toArray();
        Map<Boolean, double[]> oddEven = IntStreamEx.range(2000).asDoubleStream()
                .collect(DoubleCollector.partitioningBy(i -> i % 2 == 0));
        assertArrayEquals(expectedEven, oddEven.get(true), 0.0);
        assertArrayEquals(expectedOdd, oddEven.get(false), 0.0);
        oddEven = IntStreamEx.range(2000).asDoubleStream().parallel()
                .collect(DoubleCollector.partitioningBy(i -> i % 2 == 0));
        assertArrayEquals(expectedEven, oddEven.get(true), 0.0);
        assertArrayEquals(expectedOdd, oddEven.get(false), 0.0);
    }

    @Test
    public void testGroupingBy() {
        Map<Double, double[]> collected = IntStreamEx.range(2000).asDoubleStream()
                .collect(DoubleCollector.groupingBy(i -> i % 3));
        for (double i = 0; i < 3; i++) {
            double rem = i;
            assertArrayEquals(IntStream.range(0, 2000).asDoubleStream().filter(a -> a % 3 == rem).toArray(),
                collected.get(i), 0.0);
        }
        collected = IntStreamEx.range(2000).asDoubleStream().parallel().collect(DoubleCollector.groupingBy(i -> i % 3));
        for (double i = 0; i < 3; i++) {
            double rem = i;
            assertArrayEquals(IntStream.range(0, 2000).asDoubleStream().filter(a -> a % 3 == rem).toArray(),
                collected.get(i), 0.0);
        }
    }

    @Test
    public void testAsCollector() {
        assertEquals(499.5, IntStream.range(0, 1000).asDoubleStream().boxed().collect(DoubleCollector.summarizing())
                .getAverage(), 0.000001);
        assertEquals(499.5,
            IntStream.range(0, 1000).parallel().asDoubleStream().boxed().collect(DoubleCollector.summarizing())
                    .getAverage(), 0.000001);
    }

    @Test
    public void testAdaptor() {
        assertEquals(499.5,
            IntStreamEx.range(1000).asDoubleStream().collect(DoubleCollector.of(DoubleCollector.summarizing()))
                    .getAverage(), 0.000001);
        assertEquals(
            499.5,
            IntStreamEx.range(1000).parallel().asDoubleStream()
                    .collect(DoubleCollector.of(Collectors.summarizingDouble(Double::doubleValue))).getAverage(),
            0.000001);
    }

    @Test
    public void testReducing() {
        assertEquals(7.0, DoubleStreamEx.of(1.0, 2.0, 3.5).collect(DoubleCollector.reducing(1.0, (a, b) -> a * b)), 0.0);
    }

    @Test
    public void testMapping() {
        assertArrayEquals(
            LongStreamEx.of(1, 1, 2, 3).toArray(),
            DoubleStreamEx.of(0.8, 1.3, 1.7, 2.9).collect(
                DoubleCollector.mappingToObj(Math::round, LongCollector.toArray())));
    }
}

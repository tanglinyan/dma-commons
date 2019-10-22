/* Copyright (c) 2011 Danish Maritime Authority.
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
package dk.dma.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * The type Iterators test.
 *
 * @author Kasper Nielsen
 */
public class IteratorsTest {
    /**
     * The Natural int.
     */
    static final Comparator<Integer> NATURAL_INT = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }
    };

    /**
     * Combine 1.
     */
    @Test
    public void combine1() {
        List<Integer> l = Arrays.asList(1, 2, 3, 4);
        Iterator<Integer> combine = Iterators.combine(Arrays.asList(l.iterator()), NATURAL_INT);
        assertEquals(1, combine.next().intValue());
        assertEquals(2, combine.next().intValue());
        assertEquals(3, combine.next().intValue());
        assertEquals(4, combine.next().intValue());
        assertFalse(combine.hasNext());
    }

    /**
     * Combine 2.
     */
    @Test
    public void combine2() {
        List<Integer> l1 = Arrays.asList(1, 3);
        List<Integer> l2 = Arrays.asList(2, 4);
        Iterator<Integer> combine = Iterators.combine(Arrays.asList(l1.iterator(), l2.iterator()), NATURAL_INT);
        assertEquals(1, combine.next().intValue());
        assertEquals(2, combine.next().intValue());
        assertEquals(3, combine.next().intValue());
        assertEquals(4, combine.next().intValue());
        assertFalse(combine.hasNext());
    }

    /**
     * Combine 4.
     */
    @Test
    public void combine4() {
        List<Integer> l1 = Arrays.asList(1, 4, 8, 9, 19);
        List<Integer> l2 = Arrays.asList(2, 7, 10, 14, 18);
        List<Integer> l3 = Arrays.asList(3, 5, 6, 11, 15);
        List<Integer> l4 = Arrays.asList(12, 13, 16, 17, 20);
        Iterator<Integer> combine = Iterators.combine(
                Arrays.asList(l1.iterator(), l2.iterator(), l3.iterator(), l4.iterator()), NATURAL_INT);
        for (int i = 1; i <= 20; i++) {
            assertEquals(i, combine.next().intValue());
        }
        assertFalse(combine.hasNext());
    }
}

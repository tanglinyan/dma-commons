/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
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
 * 
 * @author Kasper Nielsen
 */
public class IteratorsTest {
    static final Comparator<Integer> NATURAL_INT = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }
    };

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

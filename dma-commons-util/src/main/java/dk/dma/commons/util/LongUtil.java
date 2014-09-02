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

/**
 * Various {code int} utility methods.
 * 
 * @author Kasper Nielsen
 */
public class LongUtil {

    /**
     * Returns {@link Integer#MAX_VALUE} if the specified long is above {@link Integer#MAX_VALUE}, otherwise it cast the
     * specified long to an int.
     * <p>
     * No check of whether the specified long is positive is made.
     * 
     * @param value
     *            a positive long value
     * @return a saturated cast of the specified long
     */
    public static long saturatedAdd(long x, long y) {
        if (x == 0 || y == 0 || x > 0 ^ y > 0) {
            return x + y;
        } else if (x > 0) {
            return Long.MAX_VALUE - x < y ? Long.MAX_VALUE : x + y;
        } else {
            return Long.MIN_VALUE - x > y ? Long.MIN_VALUE : x + y;
        }
    }
}

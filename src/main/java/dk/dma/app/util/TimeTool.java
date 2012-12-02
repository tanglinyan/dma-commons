/*
 * Copyright (c) 2008 Kasper Nielsen.
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
package dk.dma.app.util;

/**
 * 
 * @author Kasper Nielsen
 */
public class TimeTool {

    public static int daySinceEpoch(long epochTime) {
        return hoursSinceEpoch(epochTime) / 24;
    }

    public static int hoursSinceEpoch(long epochTime) {
        return minutesSinceEpoch(epochTime) / 60;
    }

    public static int minutesSinceEpoch(long epochTime) {
        if (epochTime <= 0) {
            throw new IllegalArgumentException("epochTime must be positive, was " + epochTime);
        }
        long result = epochTime / 60 / 1000;
        if (result >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("epochtime was to high, epochTime = " + epochTime);
        }
        return (int) result;
    }
}

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

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * 
 * @author Kasper Nielsen
 */
public class DateTimeUtil {

    static Date substract(Date date, long durationToSubstract, TimeUnit unit) {
        long result = unit.toSeconds(durationToSubstract);
        if (result >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Cannot substract " + durationToSubstract);
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.SECOND, -(int) result);
        return c.getTime();
    }

    public static Interval toInterval(Date startDate, Date endDate) {
        return new Interval(startDate.getTime(), endDate.getTime());
    }

    public static Interval toInterval(String isoXXInterval) {
        if (!isoXXInterval.contains("/")) {
            isoXXInterval += "/" + DateTime.now();
        }
        return Interval.parse(isoXXInterval);
    }

    public static Interval toIntervalFromNow(long timeback, TimeUnit unit) {
        Date now = new Date();
        return toInterval(substract(now, timeback, unit), now);
    }
}

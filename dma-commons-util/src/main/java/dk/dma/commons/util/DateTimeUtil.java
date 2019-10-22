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

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * The type Date time util.
 *
 * @author Kasper Nielsen
 */
public class DateTimeUtil {

    /**
     * Substract date.
     *
     * @param date                the date
     * @param durationToSubstract the duration to substract
     * @param unit                the unit
     * @return the date
     */
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

    /**
     * To interval interval.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return the interval
     */
    public static Interval toInterval(Date startDate, Date endDate) {
        return new Interval(startDate.getTime(), endDate.getTime());
    }

    /**
     * To interval interval.
     *
     * @param isoXXInterval the iso xx interval
     * @return the interval
     */
    public static Interval toInterval(String isoXXInterval) {
        if (!isoXXInterval.contains("/")) {
            isoXXInterval += "/" + DateTime.now();
        }
        return Interval.parse(isoXXInterval);
    }

    /**
     * To interval from now interval.
     *
     * @param timeback the timeback
     * @param unit     the unit
     * @return the interval
     */
    public static Interval toIntervalFromNow(long timeback, TimeUnit unit) {
        Date now = new Date();
        return toInterval(substract(now, timeback, unit), now);
    }

    /**
     * Convert no. of millis since epoch to LocalDateTime for Zone UTC
     */
    public static final Function<Long, LocalDateTime> MILLIS_TO_LOCALDATETIME_UTC = epochMillis -> LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);

    /**
     * Convert LocalDateTime for Zone UTC to no. of millis since epoch
     */
    public static final Function<LocalDateTime, Long> LOCALDATETIME_UTC_TO_MILLIS = t -> t == null ? Long.MIN_VALUE : t.toInstant(ZoneOffset.UTC).toEpochMilli();

}

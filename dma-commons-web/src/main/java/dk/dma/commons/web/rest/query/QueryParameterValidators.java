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
package dk.dma.commons.web.rest.query;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Range;

/**
 * Various utility methods to extract information from query parameters.
 * 
 * @author Kasper Nielsen
 */
public class QueryParameterValidators {

    /**
     * Returns the value of the specified query parameter. Or throws a {@link QueryParameterException} if the parameter
     * is not present. Or if there are more than 1 parameters with the specified name.
     * <p>
     * If instead of throwing a {@link QueryParameterException} for a missing parameter you want to return null. Use
     * {@link #getParameter(UriInfo, String, String)} with a default value of <tt>null</tt>
     * 
     * @param info
     *            the URI info to extract the query parameter from
     * @param name
     *            the name of the parameter
     * @return the value of the query parameter
     * @throws QueryParameterException
     */
    public static String getParameter(UriInfo info, String name) {
        String s = getParameter(info, name, null);
        if (s == null) {
            throw new QueryParameterException(info, name, 1);
        }
        return s;
    }

    public static String getParameter(UriInfo info, String name, String defaultValue) {
        List<String> p = getParameters(info, name);
        if (p.size() > 1) {
            throw new QueryParameterException(info, name, 1);
        }
        return p.isEmpty() ? defaultValue : p.get(0);
    }

    public static Integer getParameterAsInt(UriInfo info, String name) {
        return getParameterAsIntWithRange(info, name, Range.<Integer> all());
    }

    /**
     * Returns the integer value of the specified query parameter. Or the specified default value. If there are more
     * than 1 parameters with the specified name. Or if the value of parameter is not an integer throws a
     * {@link QueryParameterException}.
     * 
     * @param info
     *            the URI info to extract the query parameter from
     * @param name
     *            the name of the parameter
     * @param defaultValue
     *            the value to return if the query parameter was not present
     * @return the value of the query parameter or the default value if the query parameter is not present
     * @throws QueryParameterException
     */
    public static Integer getParameterAsInt(UriInfo info, String name, Integer defaultValue) {
        return getParameterAsIntWithRange(info, name, defaultValue, Range.<Integer> all());
    }

    public static Integer getParameterAsIntWithRange(UriInfo info, String name, Integer defaultValue,
            Range<Integer> range) {
        String p = getParameter(info, name, null);
        return p == null ? defaultValue : parseInt(info, name, p, range);
    }

    public static Integer getParameterAsIntWithRange(UriInfo info, String name, Range<Integer> range) {
        Integer i = getParameterAsIntWithRange(info, name, null, range);
        if (i == null) {
            throw new QueryParameterException(info, name, 1);
        }
        return i;
    }

    public static List<String> getParameters(UriInfo info, String name) {
        List<String> list = info.getQueryParameters().get(requireNonNull(name));
        return list == null ? Collections.<String> emptyList() : list;
    }

    public static List<Integer> getParametersAsInt(UriInfo info, String name) {
        return getParametersAsInt(info, name, Range.<Integer> all());
    }

    public static List<Integer> getParametersAsInt(UriInfo info, String name, Range<Integer> range) {
        List<Integer> result = new ArrayList<>();
        for (String s : getParameters(info, name)) {
            result.add(parseInt(null, name, s, range));
        }
        return result;
    }

    public static String getParameterWithCustomErrorMessage(UriInfo info, String param, String customErrorMessage) {
        String s = getParameter(info, param, null);
        if (s == null) {
            throw new QueryParameterException(info, customErrorMessage);
        }
        return s;
    }

    public static void main(String[] args) {
        Range<Integer> r = Range.closed(10, 1000);
        System.out.println(r.contains(9));
        System.out.println(r.contains(10));
    }

    /**
     * Parses the specified parameter value to an integer
     * 
     * @param info
     *            the URI info
     * @param parameterName
     *            the name of the parameter we are parsing
     * @param value
     *            the string value of the parameter
     * @param range
     *            the range the value bust be within. Use Range#all to include all possible integers
     * @return the value as an integer
     * @see Range#all()
     * @throws QueryParameterException
     *             if the specified value is not an integer. Or if the specified value is not within the specified range
     */
    static Integer parseInt(UriInfo info, String parameterName, String value, Range<Integer> range) {
        Integer result;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new QueryParameterException(info, parameterName, value, Integer.class);
        }
        if (!range.contains(result)) {
            throw new QueryParameterException(info, parameterName, value, range);
        }
        return result;
    }
}

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

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Range;

/**
 * 
 * @author Kasper Nielsen
 */
public class QueryParameterException extends WebApplicationException {

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    public QueryParameterException(UriInfo info, String customMessage) {
        // super("To many parameters with name " + requireNonNull(parameterName), 400);
    }

    /**
     * Used when the the users specifies more than the allowed number of parameters
     * 
     * @param parameterName
     *            the name of the parameter
     * @param multipleParameters
     *            the values of the parameter
     */
    public QueryParameterException(UriInfo info, String parameterName, List<String> multipleParameters) {
        super("To many parameters with name " + requireNonNull(parameterName), 400);
    }

    public QueryParameterException(UriInfo info, String parameterName, String value, Class<?> expectedType) {
        // throw new WebApplicationException("Expected a valid integer for parameter '" + parameterName
        // + "' but was " + parameterName + " = " + s, Response.Status.BAD_REQUEST);
    }

    /**
     * @param name
     * @param p
     * @param range
     */
    public QueryParameterException(UriInfo info, String name, String p, Range<?> range) {}

    public QueryParameterException(UriInfo info, String name, int expectedCount) {
        // getParameterAsInt
    }
}

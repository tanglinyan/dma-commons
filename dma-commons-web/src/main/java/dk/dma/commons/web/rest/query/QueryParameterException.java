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

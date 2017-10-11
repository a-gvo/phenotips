/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.services.annotations.ncr.internal.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A utilities class for Neural Concept Recognizer annotations.
 *
 * @version $Id$
 * @since 1.4
 */
public class NCRAnnotationUtils
{
    private static final String TEXT_LABEL = "text";

    private static final String CONTENT_LABEL = "content";

    private static final String SERVICE_URL = "https://ncr.ccm.sickkids.ca/curr/annotate/";

    /**
     * Gets the service URL for the Neural Concept Recognizer.
     *
     * @return the internal service URL, as string
     */
    @Nonnull
    public static String getServiceUrl()
    {
        return SERVICE_URL;
    }

    /**
     * The jackson object mapper.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Translates the request parameters to the format expected by the NCR service located at {@link #SERVICE_URL}.
     *
     * @param request the incoming request
     * @return a list of name-value pairs, in format expected by the NCR service
     * @throws JsonProcessingException if there was an error converting request data to json string
     */
    @Nonnull
    private static String translateRequest(@Nonnull final HttpServletRequest request) throws JsonProcessingException
    {
        final String text = request.getParameter(CONTENT_LABEL);
        final Map<String, String> params = new HashMap<>();
        params.put(TEXT_LABEL, text != null ? text : StringUtils.EMPTY);
        return MAPPER.writeValueAsString(params);
    }

    /**
     * Translates the response returned by service to the desired format.
     *
     * @param response the response from the NCR service located at {@link #SERVICE_URL}, as string
     * @return the adapted response, as string
     * @throws IOException if {@code response} could not be adapted to the desired format
     */
    @Nonnull
    private static String translateResponse(@Nonnull final String response) throws IOException
    {
        final List<NCRMatch> matches = MAPPER.readValue(response, NCRResponse.class).getMatches();
        return MAPPER.writer().writeValueAsString(matches);
    }

    /**
     * Post the {@code request} parameters to the {@link #SERVICE_URL}.
     *
     * @param request the string containing request parameters
     * @return the response, as string
     * @throws JsonProcessingException if the provided matches cannot be written as string
     * @throws MalformedURLException if the {@link #SERVICE_URL} is malformed
     * @throws URISyntaxException if the url cannot be converted to a URI
     * @throws IOException if the forwarded post request cannot be executed, or if the json cannot be read into the
     *                     required object structure
     */
    @Nonnull
    public static String doPost(@Nonnull final HttpServletRequest request)
        throws URISyntaxException, IOException
    {
        final String requestStr = translateRequest(request);
        final URI uri = new URL(SERVICE_URL).toURI();
        final String responseStr = Request.Post(uri)
            .bodyString(requestStr, ContentType.APPLICATION_JSON)
            .execute()
            .returnContent()
            .asString();
        return translateResponse(responseStr);
    }
}

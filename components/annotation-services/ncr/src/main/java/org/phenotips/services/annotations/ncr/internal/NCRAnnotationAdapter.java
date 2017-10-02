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
package org.phenotips.services.annotations.ncr.internal;

import org.phenotips.services.annotations.ncr.AnnotationAdapter;

import org.xwiki.component.annotation.Component;
import org.xwiki.container.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An implementation of an {@link AnnotationAdapter} for the Neural Concept Recogniser.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("ncr")
@Singleton
public class NCRAnnotationAdapter implements AnnotationAdapter
{
    private static final String NCR_LABEL = "ncr";

    private static final String TEXT_LABEL = "text";

    private static final String CONTENT_LABEL = "content";

    private static final String NAME_LABEL = "Neural Concept Recognizer";

    private static final String SERVICE_URL = "https://ncr.ccm.sickkids.ca/curr/annotate/";

    /** The jackson object mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Nonnull
    @Override
    public String getServiceName()
    {
        return NCR_LABEL;
    }

    @Nonnull
    @Override
    public String getServiceLabel()
    {
        return NAME_LABEL;
    }

    @Nonnull
    @Override
    public String getServiceURL()
    {
        return SERVICE_URL;
    }

    @Override
    public ContentType getContentType()
    {
        return ContentType.APPLICATION_JSON;
    }

    @Nonnull
    @Override
    public String adaptRequest(@Nonnull final Request request) throws JsonProcessingException
    {
        final String text = (String) request.getProperty(CONTENT_LABEL);
        final Map<String, String> params = new HashMap<>();
        params.put(TEXT_LABEL, text != null ? text : StringUtils.EMPTY);
        return MAPPER.writeValueAsString(params);
    }

    @Nonnull
    @Override
    public String adaptResponse(@Nonnull final String response) throws IOException
    {
        final List<AnnotatedResponse.Match> matches = MAPPER.readValue(response, AnnotatedResponse.class).getMatches();
        return MAPPER.writer().writeValueAsString(matches);
    }

    // TODO: Move out of here.
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class AnnotatedResponse
    {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static final class Match
        {
            @JsonIgnoreProperties(ignoreUnknown = true)
            private static final class Token
            {
                /**
                 * The token's id.
                 */
                @JsonProperty("id")
                private String id;

                /**
                 * Return the id.
                 * @return the id
                 */
                @JsonProperty("id")
                public String getId()
                {
                    return this.id;
                }

                /**
                 * Set the id.
                 * @param id the id
                 */
                @JsonProperty("id")
                void setId(String id)
                {
                    this.id = id;
                }
            }

            @JsonProperty("token")
            private Token token;

            @JsonProperty("start")
            private int start;

            @JsonProperty("end")
            private int end;

            @JsonProperty("token")
            private Token getToken()
            {
                return this.token;
            }

            @JsonProperty("hp_id")
            private void setId(final String id)
            {
                final Token tokenObj = new Token();
                tokenObj.setId(id);
                this.token = tokenObj;
            }

            @JsonProperty("start")
            private int getStart()
            {
                return this.start;
            }

            @JsonProperty("start")
            private void setStart(final int start)
            {
                this.start = start;
            }

            @JsonProperty("end")
            private int getEnd()
            {
                return this.end;
            }

            @JsonProperty("end")
            private void setEnd(final int end)
            {
                this.end = end;
            }
        }

        @JsonProperty("matches")
        private List<Match> matches;

        @JsonProperty("matches")
        private List<Match> getMatches()
        {
            return this.matches;
        }

        @JsonProperty("matches")
        private void setMatches(final List<Match> matches)
        {
            this.matches = matches;
        }
    }
}

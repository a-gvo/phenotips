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
package org.phenotips.services.annotations.rest.internal.utils;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link DefaultAnnotationAPI}.
 */
public class DefaultAnnotationAPITest
{
    private static final String SERVICE_URL = "https://ncr.ccm.sickkids.ca/curr/annotate/";

    private static final String TEXT = "The paitient was diagnosed with both cardiac disease and renal cancer.";

    private static final String REQUEST = "{\"text\":\"" + TEXT + "\"}";

    private static final String NCR_RESPONSE = "{\"matches\":[{\"end\":52,\"hp_id\":\"HP:0001627\",\"names\":"
        + "[\"Abnormal heart morphology\",\"Abnormality of cardiac morphology\",\"Abnormality of the heart\",\"Cardiac "
        + "abnormality\",\"Cardiac anomalies\",\"Congenital heart defect\",\"Congenital heart defects\"],\"score\":"
        + "\"0.696756\",\"start\":37},{\"end\":69,\"hp_id\":\"HP:0009726\",\"names\":[\"Renal neoplasm\",\"Kidney "
        + "cancer\",\"Neoplasia of the kidneys\",\"Renal neoplasia\",\"Renal tumors\"],\"score\":\"0.832163\","
        + "\"start\":57}]}";

    @Rule
    public MockitoComponentMockingRule<AnnotationAPI> mocker =
        new MockitoComponentMockingRule<>(DefaultAnnotationAPI.class);

    @Mock
    private Request request;

    @Mock
    private Response response;

    @Mock
    private Content content;

    private AnnotationAPI component;

    private URI uri;

    @Before
    public void setUp() throws ComponentLookupException, IOException, URISyntaxException
    {
        MockitoAnnotations.initMocks(this);

        this.component = spy(this.mocker.getComponentUnderTest());

        this.uri = new URL(SERVICE_URL).toURI();
        doReturn(this.request).when(((DefaultAnnotationAPI) this.component)).getPostRequest(this.uri);
        when(this.request.bodyString(REQUEST, ContentType.APPLICATION_JSON)).thenReturn(this.request);
        when(this.request.execute()).thenReturn(this.response);
        when(this.response.returnContent()).thenReturn(this.content);
        when(this.content.asString()).thenReturn(NCR_RESPONSE);
    }

    @Test(expected = AnnotationAPI.ServiceException.class)
    public void postRequestInvalidUrl() throws AnnotationAPI.ServiceException
    {
        this.component.post("wrong", ContentType.APPLICATION_JSON, REQUEST);
    }

    @Test
    public void postRequestWithDefaultData() throws AnnotationAPI.ServiceException
    {
        final String result = this.component.post(SERVICE_URL, ContentType.APPLICATION_JSON, REQUEST);
        Assert.assertEquals(NCR_RESPONSE, result);
    }

    @Test
    public void getPostRequest() throws ComponentLookupException
    {
        final Request reqObj = ((DefaultAnnotationAPI) this.mocker.getComponentUnderTest()).getPostRequest(this.uri);
        Assert.assertNotNull(reqObj);
        Assert.assertEquals("POST https://ncr.ccm.sickkids.ca/curr/annotate/ HTTP/1.1", reqObj.toString());
    }
}

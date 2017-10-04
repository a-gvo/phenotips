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
package org.phenotips.services.annotations.rest.internal;

import org.phenotips.services.annotations.ncr.AnnotationAdapter;
import org.phenotips.services.annotations.ncr.AnnotationAdapterFactory;
import org.phenotips.services.annotations.rest.AnnotationServiceResource;
import org.phenotips.services.annotations.rest.internal.utils.AnnotationAPI;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.container.Container;
import org.xwiki.container.Request;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link DefaultAnnotationServiceResource}.
 */
public class DefaultAnnotationServiceResourceTest
{
    private static final String NCR_LABEL = "ncr";

    private static final String RESPONSE = "{\"matches\":[{\"end\":52,\"hp_id\":\"HP:0001627\",\"names\":"
        + "[\"Abnormal heart morphology\",\"Abnormality of cardiac morphology\",\"Abnormality of the heart\",\"Cardiac "
        + "abnormality\",\"Cardiac anomalies\",\"Congenital heart defect\",\"Congenital heart defects\"],\"score\":"
        + "\"0.696756\",\"start\":37},{\"end\":69,\"hp_id\":\"HP:0009726\",\"names\":[\"Renal neoplasm\",\"Kidney "
        + "cancer\",\"Neoplasia of the kidneys\",\"Renal neoplasia\",\"Renal tumors\"],\"score\":\"0.832163\","
        + "\"start\":57}]}";

    private static final String ADAPTED_RESPONSE = "[{\"end\":52,\"token\":{\"id\":\"HP:0001627\"},\"start\":37},"
        + "{\"end\":69,\"token\":{\"id\":\"HP:0009726\"},\"start\":57}]";

    private static final String CONTENT = "The paitient was diagnosed with both cardiac disease and renal cancer.";

    private static final String ADAPTED_REQUEST = "{\"text\":\"" + CONTENT + "\"}";

    private static final String SERVICE_URL = "https://ncr.ccm.sickkids.ca/curr/annotate/";

    private static final String WRONG = "wrong";

    @Rule
    public MockitoComponentMockingRule<AnnotationServiceResource> mocker =
        new MockitoComponentMockingRule<>(DefaultAnnotationServiceResource.class);

    @Mock
    private Request request;

    @Mock
    private AnnotationAdapter ncr;

    private AnnotationServiceResource component;

    private AnnotationAdapterFactory annotationAdapterFactory;

    private AnnotationAPI annotationAPI;

    private Container container;

    private Logger logger;

    @Before
    public void setUp() throws ComponentLookupException, AnnotationAPI.ServiceException, IOException
    {
        MockitoAnnotations.initMocks(this);

        final Execution execution = mock(Execution.class);
        final ExecutionContext executionContext = mock(ExecutionContext.class);
        final ComponentManager componentManager = this.mocker.getInstance(ComponentManager.class, "context");
        when(componentManager.getInstance(Execution.class)).thenReturn(execution);
        when(execution.getContext()).thenReturn(executionContext);
        when(executionContext.getProperty("xwikicontext")).thenReturn(mock(XWikiContext.class));

        this.component = this.mocker.getComponentUnderTest();
        this.logger = this.mocker.getMockedLogger();

        this.annotationAdapterFactory = this.mocker.getInstance(AnnotationAdapterFactory.class);
        this.annotationAPI = this.mocker.getInstance(AnnotationAPI.class);
        this.container = this.mocker.getInstance(Container.class);

        when(this.ncr.adaptRequest(this.request)).thenReturn(ADAPTED_REQUEST);
        when(this.ncr.getContentType()).thenReturn(ContentType.APPLICATION_JSON);
        when(this.ncr.getServiceURL()).thenReturn(SERVICE_URL);
        when(this.ncr.adaptResponse(RESPONSE)).thenReturn(ADAPTED_RESPONSE);

        when(this.annotationAdapterFactory.build(NCR_LABEL)).thenReturn(this.ncr);
        when(this.container.getRequest()).thenReturn(this.request);
        when(this.annotationAPI.post(SERVICE_URL, ContentType.APPLICATION_JSON, ADAPTED_REQUEST)).thenReturn(RESPONSE);
    }

    @Test
    public void annotateAnnotationServiceIsNull()
    {
        // Want to verify logger messages.
        try {
            this.component.annotate(null);
            Assert.fail("Annotate with null annotation service should throw a WebApplicationException");
        } catch (final WebApplicationException e) {
            verify(this.logger).error("Annotation service should not be blank.");
        }
    }

    @Test
    public void annotateAnnotationServiceIsEmpty()
    {
        try {
            this.component.annotate(StringUtils.EMPTY);
            Assert.fail("Annotate with empty annotation service should throw a WebApplicationException");
        } catch (final WebApplicationException e) {
            verify(this.logger).error("Annotation service should not be blank.");
        }
    }

    @Test
    public void annotateAnnotationServiceIsBlank()
    {
        try {
            this.component.annotate(StringUtils.SPACE);
            Assert.fail("Annotate with blank annotation service should throw a WebApplicationException");
        } catch (final WebApplicationException e) {
            verify(this.logger).error("Annotation service should not be blank.");
        }
    }

    @Test
    public void annotateAdapterIsNull()
    {
        when(this.annotationAdapterFactory.build(NCR_LABEL)).thenReturn(null);
        try {
            this.component.annotate(NCR_LABEL);
            Assert.fail("Null adapter should throw a WebApplicationException");
        } catch (final WebApplicationException e) {
            verify(this.logger).error("The requested annotations service [{}] does not exist.", NCR_LABEL);
        }
    }

    @Test
    public void annotateRequestIsNull()
    {
        when(this.container.getRequest()).thenReturn(null);
        try {
            this.component.annotate(NCR_LABEL);
            Assert.fail("Null request should throw a WebApplicationException");
        } catch (final WebApplicationException e) {
            verify(this.logger).error("The request object should not be null.");
        }
    }

    @Test
    public void annotateServiceExceptionIsHandled() throws AnnotationAPI.ServiceException
    {
        when(this.annotationAPI.post(SERVICE_URL, ContentType.APPLICATION_JSON, ADAPTED_REQUEST))
            .thenThrow(new AnnotationAPI.ServiceException(WRONG));
        try {
            this.component.annotate(NCR_LABEL);
            Assert.fail("ServiceException should result in WebApplicationException being thrown");
        } catch (final WebApplicationException e) {
            verify(this.logger).error("Encountered an error while requesting annotations from {} service: [{}]",
                NCR_LABEL, WRONG);
        }
    }

    @Test
    public void annotateIOExceptionIsHandled() throws IOException
    {
        when(this.ncr.adaptResponse(RESPONSE)).thenThrow(new IOException());
        try {
            this.component.annotate(NCR_LABEL);
            Assert.fail("IOException should result in WebApplicationException being thrown");
        } catch (final WebApplicationException e) {
            verify(this.logger).error("Could not process response from {} service: [{}]", NCR_LABEL, null);
        }
    }

    @Test
    public void annotateUnknownExceptionIsHandled() throws IOException
    {
        when(this.ncr.adaptResponse(RESPONSE)).thenThrow(new RuntimeException());
        try {
            this.component.annotate(NCR_LABEL);
            Assert.fail("RuntimeException should result in WebApplicationException being thrown");
        } catch (final WebApplicationException e) {
            verify(this.logger).error("Unexpected exception occurred: [{}]", (Object) null);
        }
    }

    @Test
    public void annotateResponseIsGenerated()
    {
        final Response response = this.component.annotate(NCR_LABEL);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(ADAPTED_RESPONSE, response.getEntity());
    }
}

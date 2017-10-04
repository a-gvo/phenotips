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
import org.phenotips.services.annotations.ncr.internal.NCRAnnotationAdapter;
import org.phenotips.services.annotations.rest.AnnotationServicesResource;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.xpn.xwiki.XWikiContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link DefaultAnnotationServicesResource}.
 */
public class DefaultAnnotationServicesResourceTest
{
    private static final String ID = "id";

    private static final String LABEL = "label";

    private static final String URL = "url";

    private static final String NCR_ID = "ncr";

    private static final String NCR_LABEL = "Neural Concept Recognizer";

    private static final String SERVICE_URL = "https://ncr.ccm.sickkids.ca/curr/annotate/";

    private static final String CONTEXT = "context";

    private static final String XWIKI_CONTEXT = "xwikicontext";

    private static final AnnotationAdapter NCR = new NCRAnnotationAdapter();

    @Rule
    public MockitoComponentMockingRule<AnnotationServicesResource> mocker =
        new MockitoComponentMockingRule<>(DefaultAnnotationServicesResource.class);

    private AnnotationServicesResource component;

    private AnnotationAdapterFactory adapterFactory;

    @Before
    public void setUp() throws ComponentLookupException
    {
        final Execution execution = mock(Execution.class);
        final ExecutionContext executionContext = mock(ExecutionContext.class);
        final ComponentManager componentManager = this.mocker.getInstance(ComponentManager.class, CONTEXT);
        when(componentManager.getInstance(Execution.class)).thenReturn(execution);
        when(execution.getContext()).thenReturn(executionContext);
        when(executionContext.getProperty(XWIKI_CONTEXT)).thenReturn(mock(XWikiContext.class));

        this.component = this.mocker.getComponentUnderTest();

        this.adapterFactory = this.mocker.getInstance(AnnotationAdapterFactory.class);
        when(this.adapterFactory.getAll()).thenReturn(Collections.singletonList(NCR));
    }

    @Test
    public void getAllAnnotationServicesNoneAreAvailable()
    {
        when(this.adapterFactory.getAll()).thenReturn(Collections.emptyList());
        final Response response = this.component.getAllAnnotationServices();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(new JSONArray().similar(response.getEntity()));
    }

    @Test
    public void getAllAnnotationServicesOneIsAvailable()
    {
        final Response response = this.component.getAllAnnotationServices();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final JSONArray expected = new JSONArray().put(
            new JSONObject()
                .put(ID, NCR_ID)
                .put(LABEL, NCR_LABEL)
                .put(URL, SERVICE_URL)
        );
        Assert.assertTrue(expected.similar(response.getEntity()));
    }
}

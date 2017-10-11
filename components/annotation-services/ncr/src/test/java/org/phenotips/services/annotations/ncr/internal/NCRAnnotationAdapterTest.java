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

//import org.phenotips.services.annotations.ncr.AnnotationAdapter;
//
//import org.xwiki.component.manager.ComponentLookupException;
//import org.xwiki.container.Request;
//import org.xwiki.test.mockito.MockitoComponentMockingRule;
//
//import java.io.IOException;
//
//import org.apache.commons.lang3.StringUtils;
//import org.apache.http.entity.ContentType;
//import org.json.JSONArray;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//
//import static org.mockito.Mockito.when;
//
///**
// * Unit tests for {@link NCRAnnotationAdapter}.
// */
//public class NCRAnnotationAdapterTest
//{
//    private static final String NCR_LABEL = "ncr";
//
//    private static final String CONTENT_LABEL = "content";
//
//    private static final String NAME_LABEL = "Neural Concept Recognizer";
//
//    private static final String SERVICE_URL = "https://ncr.ccm.sickkids.ca/curr/annotate/";
//
//    private static final String CONTENT = "The paitient was diagnosed with both cardiac disease and renal cancer.";
//
//    private static final String NCR_RESPONSE = "{\"matches\":[{\"end\":52,\"hp_id\":\"HP:0001627\",\"names\":"
//        + "[\"Abnormal heart morphology\",\"Abnormality of cardiac morphology\",\"Abnormality of the heart\",\"Cardiac "
//        + "abnormality\",\"Cardiac anomalies\",\"Congenital heart defect\",\"Congenital heart defects\"],\"score\":"
//        + "\"0.696756\",\"start\":37},{\"end\":69,\"hp_id\":\"HP:0009726\",\"names\":[\"Renal neoplasm\",\"Kidney "
//        + "cancer\",\"Neoplasia of the kidneys\",\"Renal neoplasia\",\"Renal tumors\"],\"score\":\"0.832163\","
//        + "\"start\":57}]}";
//
//    private static final String FORMATTED_RESPONSE = "[{\"end\":52,\"token\":{\"id\":\"HP:0001627\"},\"start\":37},"
//        + "{\"end\":69,\"token\":{\"id\":\"HP:0009726\"},\"start\":57}]";
//
//    @Rule
//    public MockitoComponentMockingRule<AnnotationAdapter> mocker =
//        new MockitoComponentMockingRule<>(NCRAnnotationAdapter.class);
//
//    @Mock
//    private Request request;
//
//    private AnnotationAdapter component;
//
//    @Before
//    public void setUp() throws ComponentLookupException
//    {
//        MockitoAnnotations.initMocks(this);
//
//        this.component = this.mocker.getComponentUnderTest();
//
//        when(this.request.getProperty(CONTENT_LABEL)).thenReturn(CONTENT);
//    }
//
//    @Test
//    public void getServiceName()
//    {
//        Assert.assertEquals(NCR_LABEL, this.component.getServiceName());
//    }
//
//    @Test
//    public void getServiceLabel()
//    {
//        Assert.assertEquals(NAME_LABEL, this.component.getServiceLabel());
//    }
//
//    @Test
//    public void getServiceURL()
//    {
//        Assert.assertEquals(SERVICE_URL, this.component.getServiceURL());
//    }
//
//    @Test
//    public void getContentType()
//    {
//        Assert.assertEquals(ContentType.APPLICATION_JSON, this.component.getContentType());
//    }
//
//    @Test
//    public void adaptRequestWhenRequestHasNullContent() throws JsonProcessingException
//    {
//        when(this.request.getProperty(CONTENT_LABEL)).thenReturn(null);
//        final String result = this.component.adaptRequest(this.request);
//        Assert.assertEquals("{\"text\":\"\"}", result);
//    }
//
//    @Test
//    public void adaptRequestWhenRequestHasEmptyContent() throws JsonProcessingException
//    {
//        when(this.request.getProperty(CONTENT_LABEL)).thenReturn(StringUtils.EMPTY);
//        final String result = this.component.adaptRequest(this.request);
//        Assert.assertEquals("{\"text\":\"\"}", result);
//    }
//
//    @Test
//    public void adaptRequestWhenRequestHasBlankContent() throws JsonProcessingException
//    {
//        when(this.request.getProperty(CONTENT_LABEL)).thenReturn(StringUtils.SPACE);
//        final String result = this.component.adaptRequest(this.request);
//        Assert.assertEquals("{\"text\":\" \"}", result);
//    }
//
//    @Test
//    public void adaptRequestWhenRequestHasContent() throws JsonProcessingException
//    {
//        final String result = this.component.adaptRequest(this.request);
//        Assert.assertEquals("{\"text\":\"" + CONTENT + "\"}", result);
//    }
//
//
//    @Test
//    public void adaptResponseWhenResponseIsEmpty() throws IOException
//    {
//        final String response = this.component.adaptResponse("{\"matches\":[]}");
//        Assert.assertEquals("[]", response);
//    }
//
//    @Test
//    public void adaptResponseWhenResponseIsNotEmpty() throws IOException
//    {
//        final String response = this.component.adaptResponse(NCR_RESPONSE);
//        Assert.assertTrue(new JSONArray(FORMATTED_RESPONSE).similar(new JSONArray(response)));
//    }
//}

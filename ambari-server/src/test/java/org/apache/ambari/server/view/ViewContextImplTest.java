/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.view;

import org.apache.ambari.server.controller.internal.URLStreamProvider;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.view.configuration.InstanceConfig;
import org.apache.ambari.server.view.configuration.InstanceConfigTest;
import org.apache.ambari.view.ResourceProvider;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * ViewContextImpl tests.
 */
public class ViewContextImplTest {
  @Test
  public void testGetViewName() throws Exception {
    InstanceConfig instanceConfig = InstanceConfigTest.getInstanceConfigs().get(0);
    ViewDefinition viewDefinition = ViewDefinitionTest.getViewDefinition();
    ViewInstanceDefinition viewInstanceDefinition = new ViewInstanceDefinition(viewDefinition, instanceConfig);

    ViewContextImpl viewContext = new ViewContextImpl(viewInstanceDefinition);

    Assert.assertEquals("MY_VIEW", viewContext.getViewName());
  }

  @Test
  public void testGetInstanceName() throws Exception {
    InstanceConfig instanceConfig = InstanceConfigTest.getInstanceConfigs().get(0);
    ViewDefinition viewDefinition = ViewDefinitionTest.getViewDefinition();
    ViewInstanceDefinition viewInstanceDefinition = new ViewInstanceDefinition(viewDefinition, instanceConfig);

    ViewContextImpl viewContext = new ViewContextImpl(viewInstanceDefinition);

    Assert.assertEquals("INSTANCE1", viewContext.getInstanceName());
  }

  @Test
  public void testGetProperties() throws Exception {
    InstanceConfig instanceConfig = InstanceConfigTest.getInstanceConfigs().get(0);
    ViewDefinition viewDefinition = ViewDefinitionTest.getViewDefinition();
    ViewInstanceDefinition viewInstanceDefinition = new ViewInstanceDefinition(viewDefinition, instanceConfig);
    viewInstanceDefinition.addProperty("p1", "v1");
    viewInstanceDefinition.addProperty("p2", "v2");
    viewInstanceDefinition.addProperty("p3", "v3");

    ViewContextImpl viewContext = new ViewContextImpl(viewInstanceDefinition);

    Map<String, String> properties = viewContext.getProperties();
    Assert.assertEquals(3, properties.size());

    Assert.assertEquals("v1", properties.get("p1"));
    Assert.assertEquals("v2", properties.get("p2"));
    Assert.assertEquals("v3", properties.get("p3"));
  }

  @Test
  public void testGetResourceProvider() throws Exception {
    InstanceConfig instanceConfig = InstanceConfigTest.getInstanceConfigs().get(0);
    ViewDefinition viewDefinition = ViewDefinitionTest.getViewDefinition();
    ViewInstanceDefinition viewInstanceDefinition = new ViewInstanceDefinition(viewDefinition, instanceConfig);

    ResourceProvider provider = createNiceMock(ResourceProvider.class);
    Resource.Type type = new Resource.Type("MY_VIEW/myType");

    viewInstanceDefinition.addResourceProvider(type, provider);

    ViewContextImpl viewContext = new ViewContextImpl(viewInstanceDefinition);

    Assert.assertEquals(provider, viewContext.getResourceProvider("myType"));
  }

  @Test
  public void testGetURLStreamProvider() throws Exception {
    InstanceConfig instanceConfig = InstanceConfigTest.getInstanceConfigs().get(0);
    ViewDefinition viewDefinition = ViewDefinitionTest.getViewDefinition();
    ViewInstanceDefinition viewInstanceDefinition = new ViewInstanceDefinition(viewDefinition, instanceConfig);

    ResourceProvider provider = createNiceMock(ResourceProvider.class);
    Resource.Type type = new Resource.Type("MY_VIEW/myType");

    viewInstanceDefinition.addResourceProvider(type, provider);

    ViewContextImpl viewContext = new ViewContextImpl(viewInstanceDefinition);

    Assert.assertNotNull(viewContext.getURLStreamProvider());
  }

  @Test
  public void testViewURLStreamProvider() throws Exception {

    URLStreamProvider streamProvider = createNiceMock(URLStreamProvider.class);
    HttpURLConnection urlConnection = createNiceMock(HttpURLConnection.class);
    InputStream inputStream = createNiceMock(InputStream.class);

    Map<String, String> headers = new HashMap<String, String>();
    headers.put("header", "headerValue");

    Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
    headerMap.put("header", Collections.singletonList("headerValue"));

    expect(streamProvider.processURL("spec", "requestMethod", "params", headerMap)).andReturn(urlConnection);
    expect(urlConnection.getInputStream()).andReturn(inputStream);

    replay(streamProvider, urlConnection, inputStream);

    ViewContextImpl.ViewURLStreamProvider viewURLStreamProvider =
        new ViewContextImpl.ViewURLStreamProvider(streamProvider);

    Assert.assertEquals(inputStream, viewURLStreamProvider.readFrom("spec", "requestMethod", "params", headers));

    verify(streamProvider, urlConnection, inputStream);
  }
}

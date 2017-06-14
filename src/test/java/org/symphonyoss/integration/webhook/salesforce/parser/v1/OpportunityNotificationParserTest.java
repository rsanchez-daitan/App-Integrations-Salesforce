/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.webhook.salesforce.parser.v1;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.salesforce.BaseSalesforceTest;
import org.symphonyoss.integration.webhook.salesforce.SalesforceParseException;
import org.symphonyoss.integration.webhook.salesforce.parser.SalesforceParser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

/**
 * Created by cmarcondes on 11/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class OpportunityNotificationParserTest extends BaseSalesforceTest {

  private static final String CONTENT_TYPE_HEADER_PARAM = "content-type";

  @Mock
  private UserService userService;

  @InjectMocks
  private SalesforceParser salesforceParser = new OpportunityNotificationParser();

  @Before
  public void setup() {
    User returnedUser =
        createUser("amysak", "amysak@company.com", "Alexandra Mysak", 7627861918843L);
    when(userService.getUserByEmail(anyString(), anyString())).thenReturn(returnedUser);
  }

  @Test
  public void testAddingMentionTag() throws WebHookParseException, IOException,
      JAXBException {
    String messageML = readFile("parser/v1/opportunityNotification.xml");

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(CONTENT_TYPE_HEADER_PARAM, MediaType.APPLICATION_XML);
    WebHookPayload payload = new WebHookPayload(Collections.<String, String>emptyMap(), headerParams, messageML);

    Message result = salesforceParser.parse(payload);

    String expected = readFile("parser/v1/opportunityNotification_withMentionTags_expected.xml");

    assertEquals(expected, result.getMessage());
  }

  @Test
  public void testWithoutOpportunityOwner() throws WebHookParseException, IOException,
      JAXBException {
    String messageML = readFile("parser/v1/opportunityNotification_without_OpportunityOwner.xml");

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(CONTENT_TYPE_HEADER_PARAM, MediaType.APPLICATION_XML);
    WebHookPayload payload = new WebHookPayload(Collections.<String, String>emptyMap(), headerParams, messageML);

    Message result = salesforceParser.parse(payload);

    String expected = readFile(
        "parser/v1/opportunityNotification_without_OpportunityOwner_expected.xml");

    assertEquals(expected, result.getMessage());
  }

  @Test(expected = SalesforceParseException.class)
  public void testParserJson() throws IOException {
    salesforceParser.parse(null, JsonNodeFactory.instance.objectNode());
  }
}

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

package org.symphonyoss.integration.webhook.salesforce.parser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.entity.MessageMLParser;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.salesforce.BaseSalesforceTest;

import java.io.IOException;

import javax.xml.bind.JAXBException;

/**
 * Unit tests for {@link AccountStatusParser}
 * Created by cmarcondes on 11/3/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountStatusParserTest extends BaseSalesforceTest {

  @Mock
  private UserService userService;

  @InjectMocks
  private SalesforceParser salesforceParser = new AccountStatusParser();

  @Before
  public void setup() {
    User returnedUser =
        createUser("amysak", "amysak@company.com", "Alexandra Mysak", 7627861918843L);
    when(userService.getUserByEmail(anyString(), anyString())).thenReturn(returnedUser);
  }

  @Test
  public void testAddingMentionTag()
      throws WebHookParseException, IOException,
      JAXBException {
    String messageML = readFile("accountStatus.xml");
    String result = salesforceParser.parse(MessageMLParser.parse(messageML).getEntity());

    String expected = readFile("parser/accountStatus_withMentionTags_expected.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testUserNotFound()
      throws WebHookParseException, IOException,
      JAXBException {
    User returnedUser =
        createUser(null, "amysak@company.com", null, null);
    when(userService.getUserByEmail(anyString(), anyString())).thenReturn(returnedUser);
    String messageML = readFile("accountStatus.xml");
    String result = salesforceParser.parse(MessageMLParser.parse(messageML).getEntity());

    String expected = readFile("parser/accountStatus_withoutMentionTags_expected.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testWithoutAccountOwner()
      throws WebHookParseException, IOException,
      JAXBException {
    String messageML = readFile("parser/accountStatus_without_AccountOwner.xml");
    String result = salesforceParser.parse(MessageMLParser.parse(messageML).getEntity());

    String expected = readFile("parser/accountStatus_without_AccountOwner_expected.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testWithoutOpportunityOwner()
      throws WebHookParseException, IOException,
      JAXBException {
    String messageML = readFile("parser/accountStatus_without_OpportunityOwner.xml");
    String result = salesforceParser.parse(MessageMLParser.parse(messageML).getEntity());

    String expected = readFile("parser/accountStatus_without_OpportunityOwner_expected.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testWithoutOwnerEmail()
      throws WebHookParseException, IOException,
      JAXBException {
    String messageML = readFile("parser/accountStatus_without_ownerEmail.xml");
    String result = salesforceParser.parse(MessageMLParser.parse(messageML).getEntity());

    String expected = readFile("parser/accountStatus_without_ownerEmail_expected.xml");

    assertEquals(expected, result);
  }
}

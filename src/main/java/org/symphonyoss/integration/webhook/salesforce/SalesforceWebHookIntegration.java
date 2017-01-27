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

package org.symphonyoss.integration.webhook.salesforce;

import org.symphonyoss.integration.service.model.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.MessageML;
import org.symphonyoss.integration.entity.MessageMLParser;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.salesforce.parser.SalesforceParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

/**
 * Created by rsanchez on 31/08/16.
 */
@Component
public class SalesforceWebHookIntegration extends WebHookIntegration {

  @Autowired
  private MessageMLParser messageMLParser;

  private Map<String, SalesforceParser> parsers = new HashMap<>();

  @Autowired
  private List<SalesforceParser> salesforceParserBeans;

  @PostConstruct
  public void init() {
    for (SalesforceParser parser : salesforceParserBeans) {
      List<String> events = parser.getEvents();
      for (String eventType : events) {
        this.parsers.put(eventType, parser);
      }
    }
  }

  @Override
  public void onConfigChange(Configuration conf) {
    super.onConfigChange(conf);

    for (SalesforceParser parsers : salesforceParserBeans) {
      parsers.setSalesforceUser(conf.getType());
    }
  }

  @Override
  public String parse(WebHookPayload input) throws WebHookParseException {
    Entity mainEntity = parsePayloadToEntity(input);

    String type = mainEntity.getType();

    SalesforceParser parser = getParser(type);

    if(parser == null){
      return messageMLParser.validate(input.getBody());
    }

    String messageML = parser.parse(mainEntity);
    messageML = super.buildMessageML(messageML, type);

    return messageMLParser.validate(messageML);
  }

  private SalesforceParser getParser(String type) {
    return parsers.get(type);
  }

  private Entity parsePayloadToEntity(WebHookPayload payload) {
    try {
      MessageML messageML = MessageMLParser.parse(payload.getBody());
      return messageML.getEntity();
    } catch (JAXBException e) {
      throw new SalesforceParseException(
          "Something went wrong when trying to validate the MessageML received to object.", e);
    }
  }

}

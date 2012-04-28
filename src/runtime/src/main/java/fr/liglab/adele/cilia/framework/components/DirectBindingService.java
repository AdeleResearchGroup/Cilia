package fr.liglab.adele.cilia.framework.components;

import java.util.Dictionary;
import java.util.Properties;
import java.util.Random;

import fr.liglab.adele.cilia.Binding;
import fr.liglab.adele.cilia.framework.AbstractBindingService;
import fr.liglab.adele.cilia.framework.CiliaBindingService;

public class DirectBindingService extends AbstractBindingService implements CiliaBindingService {
   protected static final String senderProperty = "mediator.address";
   protected static final String collectorProperty = senderProperty;
   private static Random rn = new Random();
   private volatile static long generatedSequence = 0;

   public Dictionary getProperties(Dictionary collectorProperties, Dictionary senderProperties, Binding b) {
      Dictionary properties = new Properties();

      String topic = getTopic(b);
      topic = topic.replace(" ", "_");
      if (collectorProperties != null) {
         collectorProperties.put(collectorProperty, topic);
         properties.put(CILIA_COLLECTOR_PROPERTIES, collectorProperties);
      }
      if (senderProperties != null) {
         // senderProperties.put(senderProperty, topic);
         String mediatorAddress = topic;
         Properties requiredFilter = new Properties();
         requiredFilter.put("mediator.address", "(mediator.address=" + mediatorAddress + ")");
         senderProperties.put("requires.filters", requiredFilter);

         properties.put(CILIA_SENDER_PROPERTIES, senderProperties);
      }
      return properties;
   }

   private String getTopic(Binding b) {
      if (b.getProperty(senderProperty) != null) {
         String topic = (String) b.getProperty(senderProperty);
         return topic;
      }
      StringBuffer topic = new StringBuffer();
      topic.append("direct/");
      topic.append(b.getChain().getId());
      if (b.getSourceMediator() != null) {
         topic.append("/");
         topic.append(b.getSourceMediator().getId());
      }
      if (b.getTargetMediator() != null) {
         topic.append("/");
         topic.append(b.getTargetMediator().getId());
      }
      // topic.append("/");
      // topic.append(rn.nextLong());
      topic.append("/");
      topic.append(generatedSequence++);
      return topic.toString();
   }

}

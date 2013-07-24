package fr.liglab.adele.cilia.components.mediators;

import fr.liglab.adele.cilia.annotations.*;


/**
 * It will Visit this class
 * 
 */

@Processor(name="HelloProcessor")
public class HelloProcessor {

   /**
    * Method modifying the received data
    * 
    * @param data The processor received data
    * @return The data with "Hello, " prefix
    */
   @ProcessData
   public String sayHello(String data) {
	   String result = "Hello" + data;
	   System.out.println(result);
      return result;
   }   
}

package hello.world.example.processors;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.annotations.*;




/**
*
*/
@Processor(name="HelloProcessor", namespace = "hello.world.example")
public class HelloProcessor {

   /**
    * Method modifying the received data
    * 
    * @param data The processor received data
    * @return The data with "Hello, " prefix
    */
   @ProcessData()
   public Data sayHello(Data data) {
       if (data != null) {
           data.setContent("Hello, " + data.getContent().toString());
       }
       return data;
   }   
}

package hello.world.example.processors;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.annotations.*;
import hello.world.example.data.ContentData;

import javax.activation.DataContentHandler;


/**
*
*/
@Processor(name="HelloProcessor", namespace = "hello.world.example")
public class HelloProcessor {

   /**
    * Method modifying the received data
    * 
    * @param param The processor received data
    * @return The data with "Hello, " prefix
    */
   @ProcessData()
   public Data sayHello(Data param) {
       ContentData data = (ContentData)param.getContent();
       System.out.println("call processor");
       if (data != null) {
           data.put("Response", "Hello (" + data.get("user.name") + ") value = " +  data.getName());
       }
       return param;
   }   
}

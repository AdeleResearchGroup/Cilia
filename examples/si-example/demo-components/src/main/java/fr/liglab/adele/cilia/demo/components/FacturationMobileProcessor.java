package fr.liglab.adele.cilia.demo.components;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.demo.application.FacturationMobileService;


public class FacturationMobileProcessor {

	private FacturationMobileService service;

    public Data process(Data data) {
		
		
		String content = (String) data.getContent();
		String responseContent =  service.getConsommation(content);
		data.setContent(responseContent);
		
		StringBuffer buff = new StringBuffer();
		buff.append("\n");
		buff.append("------------------M O B I L E------------------------");
		buff.append("\n");
		buff.append(data.getContent());
		buff.append("\n");
		buff.append("------------------------------------------------------");
		buff.append("\n");
		System.out.println(buff.toString());
		
		return data;
	}
	
}

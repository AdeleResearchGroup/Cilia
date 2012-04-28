package fr.liglab.adele.cilia.demo.components;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.demo.application.FacturationInternetService;


public class FacturationInternetProcessor {

	private FacturationInternetService service;

	public Data process(Data data) {
		
	
		String content = (String) data.getContent();
		String responseContent =  service.getConsommation(content);
		data.setContent(responseContent);
		
		StringBuffer buff = new StringBuffer();
		buff.append("\n");
		buff.append("------------------IN T E R N E T------------------------");
		buff.append("\n");
		buff.append(data.getContent());
		buff.append("\n");
		buff.append("--------------------------------------------------------");
		buff.append("\n");
		System.out.println(buff.toString());
		
		
		return data;
	}

		
}

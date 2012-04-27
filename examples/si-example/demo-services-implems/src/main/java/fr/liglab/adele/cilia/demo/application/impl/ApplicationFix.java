package fr.liglab.adele.cilia.demo.application.impl;

import fr.liglab.adele.cilia.demo.application.FacturationFixeService;



public class ApplicationFix implements FacturationFixeService {

	public String getConsommation(String Info) {
		int conso=(int) (Math.random()*100+10);
		return "<suiviconso-reponseFixe xmlns=\"http://www.example.org/suiviconsoFixe/\">\n"
 	        	+"<conso>"+conso+"</conso>\n"
				+"</suiviconso-reponseFixe>";
	}

	
}

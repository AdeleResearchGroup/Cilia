package fr.liglab.adele.cilia.demo.application.impl;


import fr.liglab.adele.cilia.demo.application.FacturationMobileService;


public class ApplicationMobile implements FacturationMobileService {

	public String getConsommation(String Info) {
		int conso=(int) (Math.random()*100+10);
		return "<suiviconso-reponseMobile xmlns=\"http://www.example.org/suiviconsoMobile/\">\n"
 	        	+"<conso>"+conso+"</conso>\n"
				+"</suiviconso-reponseMobile>";
	}
	
}

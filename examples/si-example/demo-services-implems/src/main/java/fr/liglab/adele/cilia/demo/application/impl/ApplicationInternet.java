package fr.liglab.adele.cilia.demo.application.impl;


import fr.liglab.adele.cilia.demo.application.FacturationInternetService;


public class ApplicationInternet implements FacturationInternetService {

    public String getConsommation(String Info) {
        int conso = (int) (Math.random() * 100 + 10);
        return "<suiviconso-reponseInternet xmlns=\"http://www.example.org/suiviconsoInternet/\">\n"
                + "<conso>" + conso + "</conso>\n"
                + "</suiviconso-reponseInternet>";
    }

}

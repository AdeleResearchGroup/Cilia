package fr.liglab.adele.cilia.util;


public interface ThreadFactory {

    public Thread newThread(Runnable command);

}

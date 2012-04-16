package fr.liglab.adele.cilia.knowledge.util;


public interface ThreadFactory {

  public Thread newThread(Runnable command);
  
}

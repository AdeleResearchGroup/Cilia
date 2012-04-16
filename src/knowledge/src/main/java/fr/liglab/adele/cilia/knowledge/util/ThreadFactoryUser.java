package fr.liglab.adele.cilia.knowledge.util;

public class ThreadFactoryUser {

	protected ThreadFactory threadFactory_ = new DefaultThreadFactory();

	private static class DefaultThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable command) {
			return new Thread(command);
		}
	}
	
	public synchronized ThreadFactory setThreadFactory(ThreadFactory factory) {
		ThreadFactory old = threadFactory_;
		threadFactory_ = factory;
		return old;
	}

	/**
	 * Get the factory for creating new threads.
	 **/
	public synchronized ThreadFactory getThreadFactory() {
		return threadFactory_;
	}

}

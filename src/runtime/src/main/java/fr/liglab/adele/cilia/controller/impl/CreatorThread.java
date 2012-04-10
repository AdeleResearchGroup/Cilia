package fr.liglab.adele.cilia.controller.impl;

import java.util.ArrayList;
import java.util.List;

import fr.liglab.adele.cilia.controller.MediatorController;
import fr.liglab.adele.cilia.model.Collector;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.Sender;

public class CreatorThread implements Runnable {

	private boolean m_started = true;

	private Thread thread;

	private AuxiliarThread auxiliarThread;

	private List waitedModels = new ArrayList();


	public CreatorThread() {
		thread = new Thread(this);
	}

	protected void initializeAuxiliar() {
		auxiliarThread = new AuxiliarThread(this);
		auxiliarThread.initialize();
	}
	
	protected void shutdownAuxiliar(){
		auxiliarThread.shutdown();
	}
	
	public synchronized void initialize(){
		m_started = true;	
		thread.start();
		initializeAuxiliar();
	}
	
	

	public synchronized void shutdown(){
		m_started = false;
		waitedModels.clear();
		notifyAll();
		shutdownAuxiliar();
	}


	public void addSender(MediatorController mediator, Sender sender, boolean postpone) {
		ControllerPair controller = new ControllerPair(mediator, sender);
		addTask(controller, postpone);
	}


	public void addCollector(MediatorController mediator, Collector collector, boolean postpone) {
		ControllerPair controller = new ControllerPair(mediator, collector);
		addTask(controller, postpone);
	}

	protected void addTask(ControllerPair pair) {
		addTask(pair, false);
	}

	protected synchronized void addTask(ControllerPair pair, boolean postpone) {
		if (postpone) {
			auxiliarThread.addTask(pair);
		} else {
			waitedModels.add(pair);
			notifyAll();
		}
	}

	protected void startTask(ControllerPair pair) {
		MediatorController controller = pair.getController();
		Component comp = pair.getModel();
		if (comp instanceof Sender) {
			controller.createSender((Sender)comp);
		} else if (comp instanceof Collector) {
			controller.createCollector((Collector)comp);
		}
	}

	public void run() {
		boolean started;
		synchronized (this) {
			started = m_started;
		}
		while (started) {
			ControllerPair pair;
			synchronized (this) {
				while ((m_started && waitedModels.isEmpty()) ) {
					try {
						wait();
					} catch (InterruptedException e) {
						// Interruption, re-check the condition
					}
				}
				if (!m_started) {
					return; // The thread must be stopped immediately.
				} else {
					pair =  (ControllerPair)waitedModels.remove(0);
				}
			}
			try {
				startTask(pair);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			synchronized (this) {
				started = m_started;
			}
		}
	}

	private class AuxiliarThread extends CreatorThread {
		CreatorThread creator;
		long time = 1000;
		public AuxiliarThread(CreatorThread creat) {
			super();
			creator = creat;
		}

		protected void startTask(ControllerPair pair) {
			try {
				synchronized (this) {
					long currentTime = System.currentTimeMillis();
					while(System.currentTimeMillis() - currentTime < time ) {
						wait(time);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			creator.addTask(pair);
		}
		
		protected void initializeAuxiliar() {}
		
		protected void shutdownAuxiliar() {}
	}

	private class ControllerPair {
		MediatorController mediator;
		Component model;
		public ControllerPair(MediatorController controller, Component component) {
			mediator = controller;
			model = component;
		}
		public MediatorController getController(){
			return mediator;
		}
		public Component getModel() {
			return model;
		}
	}
}

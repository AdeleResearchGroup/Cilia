package fr.liglab.adele.cilia.knowledge.runtime;

import fr.liglab.adele.cilia.knowledge.Node;

public interface MeasureCallback {
	void onUpdate(Node node,String variable);
}

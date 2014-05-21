package fr.liglab.adele.cilia.model.impl;


public final class PortType {

    public static final PortType INPUT = new PortType(1);

    public static final PortType OUTPUT = new PortType(2);

    private final int pt;

    private PortType(int id) {
        this.pt = id;
    }

    public boolean equals(Object object) {
        PortType port = (PortType) object;
        if (port.pt == pt) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return pt;
    }
}

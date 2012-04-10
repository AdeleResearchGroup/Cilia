package fr.liglab.adele.cilia.model;

public final class PatternType {
	public static final PatternType IN_ONLY = new PatternType(1);
	
	public static final PatternType OUT_ONLY = new PatternType(2);
	
	public static final PatternType IN_OUT = new PatternType(3);
	
	public static final PatternType UNASSIGNED = new PatternType(0);
	
	private final int pt;
	
	private String name;
	
	private PatternType (int id) {
		this.pt = id;
	}

	private String getStringName() {
		String name = "unassigned";
		if (this.equals(IN_ONLY)) {
			name = "in-only";
		}
		if (this.equals(OUT_ONLY)) {
			name = "out-only";
		}
		if (this.equals(IN_OUT)) {
			name = "in-out";
		}
		return name;
	}
	
	public String getName(){
		if (name == null) {//This should be done once.
			name = getStringName(); 
		} 
		return name; 
	}
	
	public boolean equals(Object object) 
	{
		PatternType pat = (PatternType)object;
		if (pat.pt == pt) {
			return true;
		}
		return false;
	}
	
	  public int hashCode() 
	  {
	    return pt;
	  }
}

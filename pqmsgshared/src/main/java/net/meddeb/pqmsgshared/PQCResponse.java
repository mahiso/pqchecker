package net.meddeb.pqmsgshared;

public enum PQCResponse {
	READ("PQ-RES-Read"), 
	WRITE("PQ-RES-Write"), 
	TEST("PQ-RES-Test");
	
	private final String name;
	
	private PQCResponse(String str){
		name = str;
	}
	
	public boolean equalsName(String otherName){
		return (otherName == null)? false: name.equals(otherName);
	}
	
	public String toString(){
		return name;
	}
}

package net.meddeb.pqmsgshared;

public enum PQCRequest {
	READ("PQ-REQ-Read"), 
	WRITE("PQ-REQ-Write"), 
	TEST("PQ-REQ-Test");
	
	public static PQCRequest fromName(String sn){
		if (sn.equalsIgnoreCase(PQCRequest.READ.toString())){
			return READ;
		} else if (sn.equalsIgnoreCase(PQCRequest.WRITE.toString())){
			return WRITE;
		} else if (sn.equalsIgnoreCase(PQCRequest.TEST.toString())){
			return TEST;
		} else return null;
	}
	
	private final String name;
	
	private PQCRequest(String str){
		name = str;
	}
	
	public boolean equalsName(String otherName){
		return (otherName == null)? false: name.equals(otherName);
	}
	
	public String toString(){
		return name;
	}
}

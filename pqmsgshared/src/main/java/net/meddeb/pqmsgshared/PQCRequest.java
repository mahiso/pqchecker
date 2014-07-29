package net.meddeb.pqmsgshared;

public enum PQCRequest {
	READ("PQ-REQ-Read"), 
	WRITE("PQ-REQ-Write"), 
	TEST("PQ-REQ-Test");
	
	private final String name;
	
	public static PQCRequest fromName(String sn){
		if (sn.equalsIgnoreCase(PQCRequest.READ.toString())){
			return READ;
		} else if (sn.equalsIgnoreCase(PQCRequest.WRITE.toString())){
			return WRITE;
		} else if (sn.equalsIgnoreCase(PQCRequest.TEST.toString())){
			return TEST;
		} else return null;
	}
	
	public static String[] getStrArray(){
		String[] rslt = {PQCRequest.READ.toString(), PQCRequest.WRITE.toString(), PQCRequest.TEST.toString()};
		return rslt;
	}
	
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

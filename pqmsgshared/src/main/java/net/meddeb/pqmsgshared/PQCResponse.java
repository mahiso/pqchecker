package net.meddeb.pqmsgshared;

public enum PQCResponse {
	READ("PQ-RES-Read"), 
	WRITE("PQ-RES-Write"), 
	TEST("PQ-RES-Test");
	
	private final String name;
	
	public static PQCResponse fromName(String sn){
		if (sn.equalsIgnoreCase(PQCResponse.READ.toString())){
			return READ;
		} else if (sn.equalsIgnoreCase(PQCResponse.WRITE.toString())){
			return WRITE;
		} else if (sn.equalsIgnoreCase(PQCResponse.TEST.toString())){
			return TEST;
		} else return null;
	}
	
	public static String[] getStrArray(){
		String[] rslt = {PQCResponse.READ.toString(), PQCResponse.WRITE.toString(), PQCResponse.TEST.toString()};
		return rslt;
	}
	
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

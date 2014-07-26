package net.meddeb.pqmsgshared;

public enum MsgProperties {
	TYPE("msgType"), 
	SENDERID("senderID");
	
	private final String name;
	
	private MsgProperties(String str){
		name = str;
	}
	
	public boolean equalsName(String otherName){
		return (otherName == null)? false: name.equals(otherName);
	}
	
	public String toString(){
		return name;
	}
}

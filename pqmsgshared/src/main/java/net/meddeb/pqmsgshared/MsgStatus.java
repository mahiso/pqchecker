package net.meddeb.pqmsgshared;

public enum MsgStatus {
	SUCCESS("Success"),
	FAIL("Fail");
	
	public static MsgStatus fromName(String sn){
		if (sn.equalsIgnoreCase(MsgStatus.SUCCESS.toString())){
			return SUCCESS;
		} else if (sn.equalsIgnoreCase(MsgStatus.FAIL.toString())){
			return FAIL;
		} else return null;
	}
	
	private final String name;
	
	private MsgStatus(String str){
		name = str;
	}
	
	public boolean equalsName(String otherName){
		return (otherName == null)? false: name.equals(otherName);
	}
	
	public String toString(){
		return name;
	}
}

package net.meddeb.pqmsgshared;
/*--------------------------------------------------------------------
pqMsgshared, Shared resources manager for pqMessenger and JMS provider
Copyright (C) 2014, Abdelhamid MEDDEB (abdelhamid@meddeb.net)  

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
---------------------------------------------------------------------*/

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

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

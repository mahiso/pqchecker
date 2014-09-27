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

public enum PQChannel {
	READ_REQUEST("PQ-REQ-Read"), 
	WRITE_REQUEST("PQ-REQ-Write"), 
	TEST_REQUEST("PQ-REQ-Test"),
	READ_RESPONSE("PQ-RES-Read"), 
	WRITE_RESPONSE("PQ-RES-Write"), 
	TEST_RESPONSE("PQ-RES-Test");
	
	private final String name;
	
	public static PQChannel fromName(String sn){
		if (sn.equalsIgnoreCase(PQChannel.READ_REQUEST.toString())){
			return READ_REQUEST;
		} else if (sn.equalsIgnoreCase(PQChannel.WRITE_REQUEST.toString())){
			return WRITE_REQUEST;
		} else if (sn.equalsIgnoreCase(PQChannel.TEST_REQUEST.toString())){
			return TEST_REQUEST;
		} else if (sn.equalsIgnoreCase(PQChannel.READ_RESPONSE.toString())){
			return READ_RESPONSE;
		} else if (sn.equalsIgnoreCase(PQChannel.WRITE_RESPONSE.toString())){
			return WRITE_RESPONSE;
		} else if (sn.equalsIgnoreCase(PQChannel.TEST_RESPONSE.toString())){
			return TEST_RESPONSE;
		} else return null;
	}
	
	public static String[] getStrArray(){
		String[] rslt = {PQChannel.READ_REQUEST.toString(), PQChannel.WRITE_REQUEST.toString(), 
										 PQChannel.TEST_REQUEST.toString(), PQChannel.READ_RESPONSE.toString(),
										 PQChannel.WRITE_RESPONSE.toString(), PQChannel.TEST_RESPONSE.toString()};
		return rslt;
	}
	
	private PQChannel(String str){
		name = str;
	}
	
	public boolean equalsName(String otherName){
		return (otherName == null)? false: name.equals(otherName);
	}
	
	public String toString(){
		return name;
	}
}

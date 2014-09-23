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

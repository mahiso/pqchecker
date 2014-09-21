package net.meddeb.pqmessenger;
/*--------------------------------------------------------------------
ppMessenger, password policy messenger for ppInspect component
Messaging middleware between ppInspect OpenLDAP plugin and messaging 
service on TomEE+ applicaion server
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

public class JNIGateway {
	static {
		System.loadLibrary("pqchecker");
	}
	
	public JNIGateway() {
	}
	
	public native String getParams(String fmt);
	
	public native boolean setParams(String params, String fmt);
	
}
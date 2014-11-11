package net.meddeb.pqmsgshared;

import java.io.Serializable;
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

public class PQParamsDto implements Serializable  {
	private static final long serialVersionUID = 5896234623724191454L;
	public static final String FORMAT = "ULDS";
	private String strParams = "";
	private int upper = 0;
	private int lower = 0;
	private int digit = 0;
	private int special = 0;
	private String forbidden = "";
	
	private String intToStr(int i){
		if (i > 99) i =99;
		String rslt = Integer.toString(i);
		if (rslt.length() < 2) rslt = "0" + rslt;
		return rslt;
	}
	
	private void valuesToStrParams(){
		strParams = intToStr(upper) + intToStr(lower) + intToStr(digit) + intToStr(special) + forbidden;
		strParams = strParams.trim();
	}
	private void strParamsToValues(){
		String strComplete = "";
		if (strParams.length() < 8) strComplete = new String(new char[8-strParams.length()]).replace("\0", "0");
		strParams = strParams + strComplete;
		char c;
		for (int i=0; i<8; i++){
			c = strParams.charAt(i);
			if (!Character.isDigit(c)) strParams.replace(c, '0');
		}
		String strValue = "00";
		int idxBegin = 0;
		for (int i=0; i<4; i++){
			idxBegin = i * 2;
			strValue = strParams.substring(idxBegin, idxBegin + 2);
			switch (i){
				case 0:
					upper = Integer.parseInt(strValue);
					break;
				case 1:
					lower = Integer.parseInt(strValue);
					break;
				case 2:
					digit = Integer.parseInt(strValue);
					break;
				case 3:
					special = Integer.parseInt(strValue);
					break;
			}
		}
		if (strParams.length() > 8) forbidden = strParams.substring(8, strParams.length() -1);
	}

	public PQParamsDto(String strParams) {
		this.strParams = strParams;
		strParamsToValues();
	}

	public PQParamsDto() {
	}

	public String getStrParams() {
		return strParams;
	}

	public void setStrParams(String strParams) {
		this.strParams = strParams;
		strParamsToValues();
	}

	public int getUpper() {
		return upper;
	}
	
	public String getUpperStr() {
		return Integer.toString(upper);
	}

	public void setUpper(int upper) {
		this.upper = upper;
		valuesToStrParams();
	}

	public void setUpper(String strUpper) {
		this.upper = Integer.parseInt(strUpper);
		valuesToStrParams();
	}

	public int getLower() {
		return lower;
	}
	
	public String getLowerStr(){
		return Integer.toString(lower);
	}

	public void setLower(int lower) {
		this.lower = lower;
		valuesToStrParams();
	}

	public void setLower(String strLower) {
		this.lower = Integer.parseInt(strLower);
		valuesToStrParams();
	}

	public int getDigit() {
		return digit;
	}
	
	public String getDigitStr(){
		return Integer.toString(digit);
	}

	public void setDigit(int digit) {
		this.digit = digit;
		valuesToStrParams();
	}

	public void setDigit(String strDigit) {
		this.digit = Integer.parseInt(strDigit);
	}

	public int getSpecial() {
		return special;
	}
	
	public String getSpecialStr(){
		return Integer.toString(special);
	}

	public void setSpecial(int special) {
		this.special = special;
		valuesToStrParams();
	}
	
	public void setSpecial(String strSpecial) {
		this.special = Integer.parseInt(strSpecial);
	}

	public String getForbidden() {
		return forbidden;
	}

	public void setForbidden(String forbidden) {
		this.forbidden = forbidden;
		valuesToStrParams();
	}
	
}

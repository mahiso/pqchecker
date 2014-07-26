package net.meddeb.pqmsgshared;

public class PQParams {
	
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

	public PQParams(String strParams) {
		this.strParams = strParams;
		strParamsToValues();
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

	public void setUpper(int upper) {
		this.upper = upper;
		valuesToStrParams();
	}

	public int getLower() {
		return lower;
	}

	public void setLower(int lower) {
		this.lower = lower;
		valuesToStrParams();
	}

	public int getDigit() {
		return digit;
	}

	public void setDigit(int digit) {
		this.digit = digit;
		valuesToStrParams();
	}

	public int getSpecial() {
		return special;
	}

	public void setSpecial(int special) {
		this.special = special;
		valuesToStrParams();
	}

	public String getForbidden() {
		return forbidden;
	}

	public void setForbidden(String forbidden) {
		this.forbidden = forbidden;
		valuesToStrParams();
	}
	
}

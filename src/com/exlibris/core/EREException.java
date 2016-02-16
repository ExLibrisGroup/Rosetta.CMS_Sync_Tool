package com.exlibris.core;

public class EREException extends Exception {

	public Object additional = null;

	  public EREException(String reason)
	  {
	    super(reason);
	  }

	  public EREException(String reason, Object addinfo)
	  {
	    super(reason);
	    this.additional = addinfo;
	  }

	  public String getException(){
		  String returnStr = super.getMessage();
		  if (additional != null){
			  returnStr = returnStr + " " + (String)additional;
		  }
		  return returnStr;
	  }

}

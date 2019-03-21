package project;

import java.util.ArrayList;
import java.util.TreeMap;

public class Instruction {
	static int currentLineNum=1;
	static boolean inData=false;
	
	static void reset() {
		currentLineNum=1;
		inData=false;
	}
	private int lineNumber;
	private boolean blankLine; // true if this line is a blank line
	private boolean isData; // true if this line comes after the DATA delimiter
	private boolean isDataDelim; // true of this line IS the DATA delimiter

	// The following fields are used for instructions (isData==false)
	private String mnemonic; // The mnemonic on this line
	private int opcode; // The integer opcode associated with the mnemonic
	private int mode; // The mode number
	private int argument; // The argument value

	// The following fields are used for data (isData==true)
	private int location; 
	private int value;

	// The following is a list of all errors assocaited with this line
	ArrayList<String> errors = new ArrayList<String>();
	
	public Instruction(String text) {
		lineNumber = currentLineNum;			//1
		currentLineNum++;
		
		if(text.trim().length()==0) {     
			blankLine = true;					//2
			return;
		}
		if(text.charAt(0)== ' ' || text.charAt(0)== '\t') {					
			errors.add("Line starts with illegal white space");			//3
			text=text.trim();
		}
		
		if(text.trim().toUpperCase().equals("DATA")) {
			if(inData==true) {
				errors.add("Illegal second DATA delimiter");			//4
			}else {
				inData=true;
			}
			if(!text.trim().equals("DATA")) {
				errors.add("Illegal mixed or lower case DATA delimiter");			
			}else {
				isDataDelim = true;
				return ;
			}
			
		}
		
		String parts[] = text.split("\\s+");			//5
		
									//6
			if(inData==true) {
				isData=true;							
				
				try {
					location = Integer.parseInt(parts[0],16);			
				}
				catch(NumberFormatException e){
					errors.add("Data location is not a hex number" );
					location=0;
				}
				
				
				if(parts.length < 2) {
					errors.add("There is no value specification");			
				}
				
				else if(parts.length > 2) {
					errors.add("There are too many tokens on this line");		
				}
				
				else if(parts.length == 2) {										
					try {															
						value=Integer.parseInt(parts[1],16);						
					}																
					catch(NumberFormatException e) {								
						errors.add("Data location is not a hex number");			
					}
				}
				//location=0;
			}else {						//7
				
				isData=false;			
				
				mnemonic=parts[0];		
				
				if(!Model.OPCODES.keySet().contains(mnemonic.toUpperCase())) {		
					errors.add(" Invalid mnemonics");
					return ;
				}
				
				if((mnemonic.toUpperCase()) != mnemonic) {
					errors.add(" The mnemonic was in mixed or lower case");
				}
				
				opcode=Model.OPCODES.get(mnemonic);			
				
				if(Model.NO_ARG_MNEMONICS.contains(mnemonic)) {
					if(parts.length>1) {
						errors.add("No-argument mnemonic contains an argument");
					}
				}else {
					if(parts.length<2) {
						errors.add("Opcode requires an argument, but does not have one");
					}
					if(parts.length > 2) {
						errors.add("There are too many tokens on this line");
					}
					else if(parts.length == 2) {
						//Model.NO_ARG_MNEMONICS.contains(mnemonic);
						
						mode = 2;
						
						if(parts[1].startsWith("#")) {
								mode = 1;
								parts[1] = parts[1].substring(1);
								if(Model.IND_MNEMONICS.contains(mnemonic)) {
									errors.add("illegal immediate error");
								}
						}
						if(parts[1].startsWith("@")) {
							mode = 3;
							parts[1]=parts[1].substring(1);
							
						}
						if(parts[1].startsWith("&")) {
							if(!Model.JMP_MNEMONICS.contains(mnemonic)) {
								errors.add("Illegal absolute error");
							}else {
								mode = 0;
								parts[1]=parts[1].substring(1);
							}
						}
						try {
							argument=Integer.parseInt(parts[1],16);
						}
						catch(NumberFormatException e) {
							errors.add("Data location is not a hex number");
						}
					}
				}	
			}	
	}
	
	
	
	
	
	
		static String hex2String( int value ) {
			if (value<0) return "-"+Integer.toHexString(-value).toUpperCase();
			else return (Integer.toHexString(value).toUpperCase());
		}
		String objectCode() {
			if (blankLine) return "";
			if (!errors.isEmpty()) return ""; // Don't write object code when errors occurred
			if (isDataDelim) return "-1";
			if (isData) return "" + Integer.toHexString(location).toUpperCase() + " " + hex2String(value);
			else return "" + Integer.toHexString(opcode).toUpperCase() + " " + mode + " " + hex2String(argument);
		}
		void addErrors(TreeMap<Integer, String> errorMap) {
			if (errors.isEmpty()) return;
			errorMap.put(lineNumber,errors.get(0) + " on line " + lineNumber);
		}
		boolean isBlank() {return blankLine;}
		int getLineNumber() {return currentLineNum;}
		void checkBlanks(int lastNonBlank) {
			if (blankLine && lastNonBlank > lineNumber) {
				errors.add("Blank line not at the end of the file");
			}
			
		
		


	

		

}
}
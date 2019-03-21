package project;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.stream.Stream;
import static project.Model.Mode.*;

public class Assembler {
	private boolean readingCode;
	
	
	
	private String makeOutputCode(String line) {
		
		Model.Mode mode= null;
		if (readingCode) { 
		    // Working on instructions...
			if( line.trim().equals("DATA")) {
				readingCode = false;
				return ("-1");
			}
			else{
				String[] parts = line.split("\\s+"); // Split line based on white space
				int opcode = Model.OPCODES.get(parts[0]);
				
				if(parts.length == 1){
				 return Integer.toHexString(opcode).toUpperCase() + " 0 0";
				}else{
					mode = DIRECT; 
				}
				if(parts[1].startsWith("#")) {
  					mode = IMMEDIATE;
  					parts[1] = parts[1].substring(1);
				}
				if(parts[1].startsWith("@")) {
 					mode = INDIRECT;
  					parts[1] = parts[1].substring(1);
				}
				if(parts[1].startsWith("&")) {
 					mode = null; 
  					parts[1] = parts[1].substring(1);
				}
				int modeNumber = 0;
				if(mode != null) {
  					modeNumber = Code.MODE_NUMBER.get(mode);
				}
				return Integer.toHexString(opcode).toUpperCase() + " " + modeNumber + " " + parts[1];
			}
			
		}
			 else {
		    // Working on data...
		    return line; // Data lines are the same in the output code
		  }
		
		
	}

public int assemble(String inputFileName, String outputFileName, TreeMap<Integer, String> errors) {
	readingCode=true;
	try (Stream<String> lines = Files.lines(Paths.get(inputFileName))) {
		PrintWriter output = new PrintWriter(outputFileName);
		lines
			.map(line -> line.trim()) //remove any spaces or tabs from the end (or start) of the file
			.filter(line -> line.length() > 0) // remove blank lines at end of file
			.map(this::makeOutputCode)
			.forEach(output::println);
		output.close();
	} catch (IOException e) {
		// e.printStackTrace();
		errors.put(-1, "Unable to open the source file"); 
		return -1;
	}
	return 0;
}

public static void main(String[] args) {
	TreeMap<Integer, String> errors = new TreeMap<>();
	Assembler test = new Assembler();
	test.assemble("src/project/pexe/myfactorial.pexe", "src/project/pexe/myfactorial.pexe", errors);	
	test.assemble("src/project/pasm/merge.pasm", "src/project/pexe/merge.pexe", errors);
	test.assemble("src/project/pasm/qsort.pasm", "src/project/pexe/qsort.pexe", errors);		
}


}
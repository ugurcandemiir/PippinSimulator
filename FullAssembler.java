package project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FullAssembler extends Assembler {
	private int lastNonBlank;

	public FullAssembler() {
		super();
	}
	
	@Override
	public int assemble(String inputFileName, String outputFileName, TreeMap<Integer, String> errors) {
		if(errors == null) throw new IllegalArgumentException("Coding error: the error map is null");
		List<Instruction> instrs;
		Instruction.reset();
		try (Stream<String> lines = Files.lines(Paths.get(inputFileName))) {
			instrs = lines
				.map(line -> new Instruction(line)) // map each line to an instruction object
				.peek(ins -> { if (!ins.isBlank()) lastNonBlank=ins.getLineNumber(); } ) // Find last non-blank line
				.collect(Collectors.toList());
		} catch (IOException e) {
			errors.put(-1, "Unable to open the source file");
			return -1;
		}
		instrs = instrs.stream()
				.peek(ins -> ins.checkBlanks(lastNonBlank))
				.peek(ins -> ins.addErrors(errors)) // Save any errors found on a line
				.filter(ins ->!ins.isBlank()) // Finally we can remove blank lines
				.collect(Collectors.toList());
		if (errors.isEmpty()) {
			try (PrintWriter output = new PrintWriter(outputFileName)){
				instrs.stream()
					.forEach(ins->output.println(ins.objectCode()));
					// System.out.println("Assembly completed with no errors");
			} catch (FileNotFoundException e) {
				errors.put(-1, "Unable to open the object file");
				return -1;
			}
			return 0;
		} 
		return errors.firstKey();
	}
	
	public static void main(String[] args) {
		TreeMap<Integer, String> errors = new TreeMap<>();
		Assembler test = new FullAssembler();
		test.assemble("src/project/pasm/02.pasm", "src/project/pexe/02.pexe", errors);	
	}

}

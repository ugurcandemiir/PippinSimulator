package project;
import static project.Model.Mode.*;
import projectview.States;

import java.util.Map;
import java.util.Set;


public class Model {
	

	static class CPU {
		private int accumulator;
		private int instructionPointer;
		private int memoryBase;
	}
	static enum Mode {
		INDIRECT, DIRECT, IMMEDIATE;
		Mode next() {
			if (this==DIRECT) return IMMEDIATE;
			if (this==INDIRECT) return DIRECT;
			return null;
		}
	}

	
	public static final Map<Integer, String> MNEMONICS = Map.ofEntries(
			Map.entry(0, "NOP"), 
			Map.entry(1, "LOD"), 
			Map.entry(2, "STO"), 
			Map.entry(3, "ADD"),
			Map.entry(4, "SUB"), 
			Map.entry(5, "MUL"), 
			Map.entry(6, "DIV"), 
			Map.entry(7, "AND"),
			Map.entry(8, "NOT"), 
			Map.entry(9, "CMPL"), 
			Map.entry(10, "CMPZ"), 
			Map.entry(11, "JUMP"),
			Map.entry(12, "JMPZ"), // NOTE THERE IS A DELIBERATE GAP for 13 and 14
			Map.entry(15, "HALT"));
	
	public static final Map<String, Integer> OPCODES = Map.ofEntries(
			Map.entry("NOP", 0),
			Map.entry("LOD", 1), 
			Map.entry("STO", 2), 
			Map.entry("ADD", 3),
			Map.entry("SUB", 4), 
			Map.entry("MUL", 5), 
			Map.entry("DIV", 6), 
			Map.entry("AND", 7),
			Map.entry("NOT", 8), 
			Map.entry("CMPL", 9), 
			Map.entry("CMPZ", 10), 
			Map.entry("JUMP", 11),
			Map.entry("JMPZ", 12), // NOTE THERE IS A DELIBERATE GAP for 13 and 14
			Map.entry("HALT", 15));
	
	public static final Set<String> NO_ARG_MNEMONICS = Set.of("NOP", "NOT", "HALT");

	public interface Instruction { void execute(int arg,Mode immediate); }
	public interface HaltCallBack { void halt(); }
	
	private final Instruction[] INSTR = new Instruction[16];
	private CPU cpu = new CPU();
	private Data dataMemory = new Data();
	private Code codeMemory= new Code();
	private HaltCallBack callback;
	private Job[] jobs = new Job[4];
	private Job currentJob;	
	
	public Model() {
		this(()->System.exit(0));
	
	}
	
	public Model(HaltCallBack cb) {
		callback = cb;
		for(int i = 0; i < jobs.length; i++) {
			jobs[i] = new Job();
			jobs[i].setId(i);
			jobs[i].setStartcodeIndex (i*Code.CODE_MAX/jobs.length);
			jobs[i].setStartmemoryIndex(i*Data.DATA_SIZE/jobs.length);
		}
		currentJob = jobs[0];
		
		INSTR[0] = (arg, mode) -> {
		    if(mode != null) throw new IllegalArgumentException( "Illegal Mode in NOP instruction");
		    cpu.instructionPointer++;
		};

		INSTR[1] = (arg, mode) -> {
			if(mode == null) throw new IllegalArgumentException("Illegal Mode in LOD instruction");
			if(mode != IMMEDIATE) {
				INSTR[1].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
				} else {
					cpu.accumulator = arg;
					cpu.instructionPointer++;
			}
		};

		INSTR[2] = (arg, mode) -> {
			if(mode == null || mode == IMMEDIATE) throw new IllegalArgumentException("Illegal Mode in STO instruction");
			if(mode != DIRECT) {
				INSTR[2].execute(dataMemory.getData(cpu.memoryBase+arg),mode.next());	
			}
			if (mode == DIRECT){
				dataMemory.setData(cpu.memoryBase+arg, cpu.accumulator);	
				cpu.instructionPointer++;
			}
		};

		INSTR[3] = (arg, mode) -> {
			if(mode == null) throw new IllegalArgumentException("Illegal Mode in ADD instruction");
			if(mode != IMMEDIATE) {
				INSTR[3].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
				} else {
					cpu.accumulator += arg;
					cpu.instructionPointer++;
			}
		};

		INSTR[4] = (arg, mode) -> {
			if(mode == null) throw new IllegalArgumentException("Illegal Mode in SUB instruction");
			if(mode != IMMEDIATE) {
				INSTR[4].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				cpu.accumulator -= arg;
				cpu.instructionPointer++;
			}
		};

		INSTR[5] = (arg, mode) -> {
			if(mode == null) throw new IllegalArgumentException("Illegal Mode in MUL instruction");
			if(mode != IMMEDIATE) {
				INSTR[5].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				cpu.accumulator *= arg;
				cpu.instructionPointer++;
			}
		};

		INSTR[6] = (arg, mode) -> {
			if(mode == null) throw new IllegalArgumentException("Illegal Mode in DIV instruction");
			if(mode != IMMEDIATE) {
				INSTR[6].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				if (arg ==0 ) { throw new DivideByZeroException("Divide by Zero"); }
				cpu.accumulator /= arg; 
				cpu.instructionPointer++;
			}
		};
		INSTR[7] = (arg, mode) -> {
			if(mode == null) throw new IllegalArgumentException("Illegal Mode in AND instruction");
			if(mode != IMMEDIATE) {
				INSTR[7].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				if(arg == 0 || cpu.accumulator == 0) {cpu.accumulator = 0;}
				if(arg != 0 && cpu.accumulator != 0) {cpu.accumulator = 1;}
				cpu.instructionPointer++;
			}
		};

		
		INSTR[8] = (arg, mode) -> {
			if(mode == null) {
				if(cpu.accumulator == 0) {cpu.accumulator = 1;}
				else {cpu.accumulator = 0;}
				cpu.instructionPointer++;
				
			}
			else {throw new IllegalArgumentException("Illegal Mode in NOT instruction");}
		};
		
		INSTR[9] = (arg, mode) -> {
			if(mode == null||mode == IMMEDIATE) { throw new IllegalArgumentException("Illegal Mode in CMPL instruction");}
			if (mode== DIRECT) { 
				int result = dataMemory.getData(cpu.memoryBase + arg);
				if(result < 0) {
					cpu.accumulator = 1;
				}
				else {
					cpu.accumulator = 0;
				}
			}
			if(mode==INDIRECT) {
					INSTR[9].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
				}
			else {cpu.instructionPointer++;}
		};

		
		INSTR[10] = (arg, mode) -> {
			if(mode == null||mode == IMMEDIATE) { throw new IllegalArgumentException("Illegal Mode in CMPZ instruction");}
			if (mode == DIRECT) { 
				int result = dataMemory.getData(cpu.memoryBase + arg);
				if(result == 0) {
					cpu.accumulator = 1;
				}
				else {
					cpu.accumulator = 0;
				}
			}
			if(mode==INDIRECT) {
				INSTR[10].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			}
			else {cpu.instructionPointer++;}
		};
		
		INSTR[11] = (arg, mode) -> {
			if(mode == null) {
				cpu.instructionPointer = dataMemory.getData(cpu.memoryBase + arg) + currentJob.getStartcodeIndex();
			}
			if (mode == INDIRECT || mode == DIRECT){
				INSTR[11].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			}
			
			if(mode == IMMEDIATE) {
				cpu.instructionPointer += arg ;
			}
		};
		INSTR[12] = (arg, mode) -> {
			if(cpu.accumulator == 0) {
				
				if(mode == null) {
					cpu.instructionPointer = dataMemory.getData(cpu.memoryBase + arg) + currentJob.getStartcodeIndex();
				}
	
				if (mode == INDIRECT || mode == DIRECT){
					INSTR[11].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
				}
				
				if(mode == IMMEDIATE) {
					cpu.instructionPointer += arg ;
				}
				
			}else {
				cpu.instructionPointer++;
			}

		};

		INSTR[13] = (arg, mode) -> {
			return;
		};
		INSTR[14] = (arg, mode) -> {
			return;
		};
		INSTR[15] = (arg, mode) -> {
			callback.halt();
		};
	}
	
	int[] getData() {return dataMemory.getData();}
	public int getInstrPtr() {return cpu.instructionPointer;}	
	public int getAccum() {return cpu.accumulator;}
	Instruction get(int i) {return INSTR[i];}
	void setData(int index,int value) {dataMemory.setData(index,value);}
	public int getData(int index) {int a= dataMemory.getData(index);return a;}
	void setAccum(int accInit) {cpu.accumulator = accInit;}
	void setInstrPtr(int ipInit) { cpu.instructionPointer = ipInit;}
	void setMemBase(int offsetInit){cpu.memoryBase = offsetInit;}
	public Job getCurrentJob() {return currentJob;}
	public Code getCodeMemory() {return codeMemory;}
	public void setCode(int index, int op, Mode mode, int arg) {
		codeMemory.setCode(index, op, mode, arg);
	}
	public void getHex(int index) {codeMemory.getHex(index);}
	public void getText(int index) {codeMemory.getText(index);}
	public void getOp(int index) {codeMemory.getOp(index);}
	public void getMode(int index) {codeMemory.getMode(index);}
	public void getArg(int index) {codeMemory.getArg(index);}
	public int getMemBase() {return cpu.memoryBase;}

	public States getCurrentState() {
		return currentJob.getCurrentState();
	}
	public void setCurrentState(States state) {
		currentJob.setCurrentState(state);
		state.enter();
	}
	public void changeToJob(int i) {
		if (i<0 || i>3) throw new IllegalArgumentException("Invalid job- job must be 0-3");
		currentJob.setCurrentAcc(cpu.accumulator);
		currentJob.setCurrentIP(cpu.instructionPointer);
		currentJob=jobs[i];
		currentJob.getCurrentState().enter();
		cpu.accumulator=currentJob.getCurrentAcc();
		cpu.instructionPointer=currentJob.getCurrentIP();
		cpu.memoryBase = currentJob.getStartmemoryIndex();
	}
	public void clearJob() {
		dataMemory.clearData(currentJob.getStartmemoryIndex(), 
			currentJob.getStartmemoryIndex()+Data.DATA_SIZE/4);
		codeMemory.clear(currentJob.getStartcodeIndex(), 
			currentJob.getStartcodeIndex() + currentJob.getCodeSize());
		cpu.accumulator=0;
		cpu.instructionPointer=currentJob.getStartcodeIndex();
		cpu.memoryBase = currentJob.getStartmemoryIndex();
		currentJob.reset();
	}
	public void step() {
		try {
			if (cpu.instructionPointer < currentJob.getStartcodeIndex()) {
				throw new CodeAccessException("instruction pointer less than the start of the current job code");
			}
			if (cpu.instructionPointer >= currentJob.getStartcodeIndex() + currentJob.getCodeSize()) {
				throw new CodeAccessException("instruction pointer greater than the end of the current job code");
			}
			int opcode=codeMemory.getOp(cpu.instructionPointer);
			Mode mode=codeMemory.getMode(cpu.instructionPointer);
			int arg=codeMemory.getArg(cpu.instructionPointer);
			get(opcode).execute(arg, mode);
		} catch(Exception e) {
			callback.halt();
			throw e;
		}
	}
	public int getChangeIndex() {
		return dataMemory.getChangedIndex();
	}

	public static final Set<String> IND_MNEMONICS = Set.of("STO","CMPL","CMPZ");
	public static final Set<String> JMP_MNEMONICS = Set.of("JUMP","JMPZ");
	
	
	
	
	
	
	
}
	
	
	
	
	
	
	
	
	

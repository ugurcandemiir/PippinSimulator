# Final Project - Installment 4

Finally, the end is in sight!

First, no new java code will be supplied to you in this repository. This project-4 repository consists soley of a README.md (this file) as well as lots of syntactically incorrect Pippin Assembler (.pasm) files to be used as test cases for an error checking assembler.

The final challenge is to actually write the error checking assembler. But before we get into the details of the error checking assember, let's first list the syntax rules we need to check for.

## Pippin Assembler Syntax Rules

The following is the list of syntax rules for Pippin Assembler data, along with the error message the assembler should print if the syntax rule is violated.

1. There can be no blank lines in the file EXCEPT at the end of the file. Error message: ```Blank line not at the end of the file``` Blank lines at the end of the file should be ignored.
 
2. There can be no white space at the beginning of a non-blank line. It is an error if the first char on the line is a space (== ' ') or a tab (== '\t') Error message: ```Line starts with illegal white space```
 
3. Although some programs have no data, the ones that do have data need a single separator line "DATA". The word DATA must be in upper case. It CAN have white space _after_ the letters of DATA as in "DATA    "   Error message: ```Illegal mixed or lower case DATA delimiter``` (detected when line has only "data", but not in upper case.)

4. A program can have only *one* "DATA" separator line. If there is more than one "DATA" separator line, it is an error. Error message: ```Illegal second DATA delimiter```

5. In the instruction part of the file, before the DATA delimiter line: 

   a. the first blank delimited word on the line must be a valid Pippin op-code mnemonic, using upper case letters. If not, it is an error. Error Message: ```Illegal mnemonic``` (if no match) Error Message: ```mnemonic not in upper case``` (if mnemonic is in lower or mixed case)
   
   b. The mnemonic must be followed by white space (blank, tab, or newline). There may be more than one white space character after the mnemonic.
   
   c. If the mnemonic is one of the no-argument mnemonics (NOP, NOT, or HALT), then no other non-white space characters may appear on this line. Error Message: ```Illegal argument in no-argument instruction``` All other mnemonics must have an argument after the white space after the mnemonic. Error message: ```Instruction requires argument```
   
   d. If there is an argument, the argument may optionally a prefix of either #, @, or &.
   
   e. If the mnemonic is one that does not allow immediate arguments (STO, CMPL, and CMPZ) then the # prefix is an error. Error message: ```Illegal immediate argument```
   
   f. If the mnemonic is **not** a mnemonic that allows ABSOLUTE-DIRECT arguments (anything other than JUMP and JMPZ), then the & prefix is an error. Error message: ```Illegal absolute argument```
   
   g. Anything after the prefix must be a valid hexadecimal number: it may optionally have a minus sign, followed by one or more hexadecimal digits, 0-9 or A-F. Error message: ```Argument is not a hex number```
   
   h. After the argument specification, no other non-white space characters may appear on the line. Error message: ```Instruction has too many arguments```

6. In the DATA section of the file, The following rules apply:

   a. The first blank delimited word on the line must consist of a location, specified in hexadecimal, with one or more digits 0-9, or A-F. A preceding minus sign is an error. Error Message: ```DATA location is not a hex number```
   
   b. After the first blank delimited word, there may be any number of white space characters (blanks or tabs)
   
   c. After the white space must be a value specification, specified in hexadecimal, with an optional minus sign, followed by one or more hexadecimal digits, 0-9 or A-F. Error Message: ```No DATA value``` (if no value specified) Error Message: ```DATA value is not a hex number``` (if value specified, but not hexadecimal characters)
   
   d. After the value specification any number of white space characters may be included up to the end of the line. No non-white space characters may be specified. Error Message: ```DATA has too many values```
   
## Implementing Error Checks

In order to implement error checking, there are several changes required.

1. In the Model class, we already have the set of NO_ARG_MNEMONICS, but we need to add the following two sets...

```java
public static final Set<String> IND_MNEMONICS = Set.of("STO","CMPL","CMPZ");
public static final Set<String> JMP_MNEMONICS = Set.of("JUMP","JMPZ");
```

2. There are many ways to implement the error checking, but I will describe a methodology that I found to be the simplest solution. I created a class called "Instruction". (Note that this does not conflict with the *Instruction* interface, because the *Instruction* interface is inside the Model class, and Model.Instruction is different than Instruction.) The Instruction class keeps track of everything required for a single instruction in a single data line of the assembler input file. Details on the Instruction class are described below.  Then I created a FullAssembler class that extends Assembler, and overrides the "assembler" method.  Details on the FullAssembler class are also described below.

3. In the projectview package, the FilesMgr class invokes the assembler.  Add an import for project.FullAssembler, and change the assembleFile method declaration of Assembler assembler to instantiate a new FullAssember instead of the default Assembler.

## Implementing the Instructions Class

Create a new class in the project package called ```Instruction```. Create two static fields:

```java
static int currentLineNum=1;
static boolean inData=false;
```

Note that these two values need to get reset every time the assembler is run, so we need a static reset method as follows:

```java 
static void reset() {
	currentLineNum=1;
	inData=false;
}
```

Next, we need private fields to keep track of all the possible information about a single line of Pippin assembler code.  Here are the private fields I use:

```java
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
```

The creator for the Instruction class takes a line of Pippin Assembler text as input (I used the parameter ```String text```), and uses that line to provide values for all of the fields relevant to that line.  In doing so, the creator also must check the syntax of the line, and may report errors assocaited with the line. My creator method does this as follows:

1. Set the lineNumber to the currentLineNum, and increment currentLineNum.

2. Check to see if the current line is a blank line. In java, this can be checked with ```text.trim().length==0```. If the line is a blank line, set the blankLine flag to true, and return.

3. Check to see if the first character of text (```text.charAt(0)```) is a blank or a tab (\t) If so, add the appropriate error message to the errors array, and trim the text using ```text=text.trim()``` so you can look for more errors on this line.

4. Check to see if the line contains only the word "DATA" in either lower or upper case.  This can be checked with ```text.trim().toUpperCase().equals("DATA")```. If so, and the ```inData``` flag is true, this must be the second DATA delimiter... add an error message. Then, set `inData` to true. Then, check to see if the DATA specification is in upper case, by checking ```text.trim().equals("DATA")```. If not, add an error message. Set the `isDataDelim` flag to true, and return.

5. Now, we use the same trick we used in the simple assembler... we split the text into an array of blank delimited words using the following line of code:

   ```java
   String parts[] = test.split("\\s+");
   ```
   
   The split function takes care of all of the "one or more white space characters" clauses in the syntax rules.  What we are left with is an array of blank delimited words.
   
6. if the `inData` flag is true, we are working on the data specification... do the following:

   a. Set the `isData` flag to true.
   
   b. We use the Integer class ```parseInt``` method to determine if the location is a valid hexadecimal specification for a location. The parseInt method will throw a NumberFormatException if it runs into any characters other than valid hexadecimal characters, so make a try/catch block. In the try section, invoke ```location = Integer.parseInt(parts[0],16);```. Then, in the catch block, catch a NumberFormatException e, add an error saying ```Data location is not a hex number```, and set location to zero.
   
   c. If ```parts.length < 2``` then there is no value specification. add an error message to that effect.
   
   d. if ```parts.length > 2``` then there are too many tokens on this line, so add an error message to that effect.
   
   e. if ```parts.length==2``` then it's just right... do the same trick with a try/catch block using ```value=Integer.parseInt(parts[1],16)```, and adding an error message if you get a NumberFormatException.
   
7. If the inData flag is not true, then we are processing an instruction line.  Do the following:

   a. set the `isData` flag to false.
   
   b. Set ```mnemonic=parts[0];```
   
   c. Check for invalid mnemonics using ```!Model.OPCODES.keySet().contains(mnemonic.toUpperCase())``` If the mnemonic is invalid, add an error message and return.
   
   d. Check to see if the toUpperCase version of the mnemonic equals itself. If not, the mnemonic was in mixed or lower case, so add an error message.
   
   e. Set the opcode number using ```opcode=Model.OPCODES.get(mnemonic);```.
   
   f. Check to see if a no-argument mnemonic contains an argument, using the check ```Model.NO_ARG_MNEMONICS.contains(mnemonic)``` If the result is true, and if ```parts.length>1```, then add the appropriate error message. If it's not a no-argument menmonic, and ```parts.length < 2``` then the opcode requires an argument, but does not have one.  Add an error message. If ```parts.length > 2``` it's a problem as well. Otherwise, ```parts.length==2```, and we need to continue checking the argument...
   
   g. Set mode=2. The default mode (if no prefix is set) is DIRECT, which is mode number 2.
   
   h. Check for a # prefix with ```parts[1].startsWith("#")```. If so, set mode=1, set ```parts[1]=parts[1].substring(1);```, and then check to see if ```Model.IND_MNEMONICS.contains(mnemonic)```. If so, add an error message.
   
   i. Check for a @ prefix. If there is one, remove it from `parts[1]` and set mode=3 (INDIRECT).
   
   j. Check for a & prefix. If there is one, check to make sure the mnemonic is in `JMP_MNEMONICS`. If not, add an error message. If so, set mode to 0, and remove the & prefix from `parts[1]`.
   
   k. Use the try/catch on ```Integer.parseInt(parts[1],16)``` trick to determine if the argument is a valid hex number. If not, add an error message.
   
Finally, we are done with the constructor! However, there are some other methods need in the Instruction class.

Most importantly, we need a method which writes out object code. We normally use the Integer.toHexString method to convert from an integer to a hex string, but this causes problems with negative numbers. The toHexString method uses two's complements values to represent negative numbers, and we have a different convention for our object code.  A negative number should be represented by a minus sign, followed by the hex representation of the absolute value of the number. Since this shows up in a couple of differnt places, make a static method to handle this conversion as follows:

```java
static String hex2String(int value) {
	if (value<0) return "-"+Integer.toHexString(-value).toUpperCase();
	else return Integer.toHexString(value).toUpperCase();
}
```

Now the method to write the object code is very simple...

```java
String objectCode() {
	if (blankLine) return "";
	if (!errors.isEmpty()) return ""; // Don't write object code when errors occured
	if (isDataDelim) return "-1";
	if (isData) return "" + Integer.toHexString(location).toUpperCase() + " " + hex2String(value);
	else return "" + Integer.toHexString(opcode).toUpperCase() + " " + mode + " " + hex2String(argument);
}
```

Next, it would be nice to have a method to take the first error on this line, and add it to a TreeMap of errors, keyed by line number.

```java
void addErrors(TreeMap<Integer, String> errorMap) {
	if (errors.isEmpty()) return;
	errorMap.put(lineNumber,errors.get(0) + " on line " + lineNumber);
}
```

We also need some infrastructure to help us check for blank line errors.

We need a `boolean isBlank()` method that returns the value of the blankLine flag.

We need an `int getLineNumber()` method that returns the current line number.

Finally, if we know the last non blank line number, we can check this line to see if it is a blank line before the last non-blank line number, as follows:

```java
void checkBlanks(int lastNonBlank) {
	if (blankLine && lastNonBlank > lineNumber) {
		errors.add("Blank line not at the end of the file");
	}
}
```

Now we are done with the Instruction class... but how are we going to use this class?  We are going to use this class with streams in the FullAssembler class that is described next.

## The FullAssembler class

Create a FullAssembler class in the project package that extends Assembler. We will need one private field in our FullAssembler class to help us keep track of the last non blank line.  I called it `lastNonBlank`.

The FullAssembler class will override the `assemble` method we defined in the Assembler class.  Our FullAssembler `assemble` method will perform error checking as well as write object code. If there are any errors in the assembler code, the FullAssembler `assemble` method should *not* write object code, but instead, update the `errors` parameter, and return a return code that contains the line number of the first error discovered. Since we number lines in files starting at line 1, a return value of 0 indicates that there were no errors.  There are also some errors, such as `Unable to open the source file` which are not associated with any specific line. For these, we use the convention of a line number of -1, and return a -1 return code.

Our FullAssembler `assemble` method will use streams to process the assmebler input file and to write out the object code. Most of the processing, including reading the assembler file and converting each line of text to an Instruction object, can occur in a single stream.  The problem with a single stream is that we don't know 1) if there will be an error in a future line, so we shouldn't be writing object code; and 2) if the current line is a blank line, are there any non-blank lines yet to come (so this is an error) or not (so this is a valid trailing blank line.)  In order to handle this, we create a stream which reads the input file and makes a list of Instruction objects. We then make a second stream from that list of Instruction objects which checks for illegal blank lines, and updates the `errors` TreeMap. Then if there are no errors, we make a third stream to print the object code.

Getting into details, here is the declaration of the `assemble` method:

```java
@Override
public int assemble(String inputFileName, String outputFileName, TreeMap<Integer, String> errors) {
```

Then, some housekeeping.  Make sure the caller has supplied a valid `errors` parameter, create a list of references to the Instruction class, and invoke the Instruction reset method...

```java
if(errors == null) throw new IllegalArgumentException("Coding error: the error map is null");
List<Instruction> instrs;
Instruction.reset();
```

Next, we make a stream of lines from the input file. We use a special form of a try/catch block called "try with resources". The "resource" is defined in parenthesis after the `try` keyword, and it represents something which needs to be opened.  The "try with resources" Java code is smart enough to relinquish the resource when the try block is finished, whether the catch is invoked or not. In this case, the resource is a stream of lines read from a file, and the file must be closed when we are finished. We let the try code do that for us. (See the text book section 11.4.4 for more details on try-with-resource.)

Once we have opened a stream of lines, we map that stream using the Instruction creator to convert each line from the file into a instance of an Instruction object. Next, we peek at each line to check if that line is blank. If not, we set `lastNonBlank` to the line number of that line. Finally, we use the Stream `collect` method to write each element of the stream (by this time, a reference to an Instruction object) into our list of instructions.  Here is the code to accomplish all this:

```java
try (Stream<String> lines = Files.lines(Paths.get(inputFileName))) {
	instrs = lines
		.map(line -> new Instruction(line)) // map each line to an instruction object
		.peek(ins -> { if (!ins.isBlank()) lastNonBlank=ins.getLineNumber(); } ) // Find last non-blank line
		.collect(Collectors.toList());
} catch (IOException e) {
	errors.put(-1, "Unable to open the source file");
	return -1;
}
```

At this point, we have caught most of the syntax errors, except for blank lines that are not at the end of the file. We make another stream to check for this error, now that we know the line number of the last non-blank line in the file. We also use this stream to update the errors TreeMap from any error found on an individual line.  While we are at it, we also remove any blank lines... we have already save error messages about internal blank lines, so we don't need Instruction objects for those lines anymore.  We also use the Stream `collect` method to rewrite our stream back to the same list.  Here is the code for the second stream:

```java
instrs = instrs.stream()
	.peek(ins -> ins.checkBlanks(lastNonBlank))
	.peek(ins -> ins.addErrors(errors)) // Save any errors found on a line
	.filter(ins ->!ins.isBlank()) // Finally we can remove blank lines
	.collect(Collectors.toList());
```

Finally, we check to see if there are any errors. If not, we create our third stream to write out the object code. If so, we return the correct return code.

```java
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
```

You may want to add a main method to the FullAssembler class. (Copy the main method from the Assembler class, and modify the instance of Assembler to isntantiate FullAssembler.) This will allow you to unit test your Assembler code.

The remaining .pasm files included in this repository contain mostly code with syntax errors (although some of them actually work correctly.) Move this into your project/pasm directory so you can run them from the GUI.  Notice that when you run from the GUI, you get a pop-up if there are errors that list all of the errors in the file.

## Testing the Final Project

Make sure you test all aspects of the final project. For instance, make sure you run a test with multiple jobs and can successfully switch back and forth between jobs.  Make sure that your assembler catches syntax errors and prints correct results. Make sure the factorial program computes the correct values. Maybe try to code some simple Pippin Assembler code on your own.

## Submitting your Final Project

After testing all of your code, make sure you commit and push both the project and projectview repositories. Then paste the commit string for both project and projectview (installment 1 and installment 3) in myCourses. Note that there are two submission areas in myCourses. Cut and paste the hash-code for the submission of the "project" repository (which was the repository delivered with Installment 1, but we have updated in Installment 2, 3, and 4) in the myCourses `Final Project Submission - project repository` area.  Also cut and paste the hash-code for the submission of the "projectview" repository (which was the repository delivered with Installment 3, and updated in Installment 4) in the myCourses `Final Project Submission - projectview repository` area. Only one submission is required per team, so choose the team member who is going to submit, and make sure the commit takes place and that myCourses has been updated. The due date for these final commits is Thursday, December 13 at 11:59 P.M.

## Final Project Grading Criteria

Installment 1 was worth 30 points and the grading criteria for Installment 1 was published in Installment 1.  

Installments 2, 3, and 4 will make up the remaining 70 points for the final project grade, broken down as follows:

- 15 points if you can assemble, load, and clear a simple, correct Pippin Assembler program

- 15 points if you can identify errors in three randomly chosen syntactically incorrect programs (5 points each)

- 30 points if the GUI runs correctly - all fields update and highlight correctly with a correct program, and all buttons behave as expected.

- 10 points if Job Management works correctly - it must be possible to run two different jobs and switch back and forth between these jobs, and still get the right answers.


, divided into 35 points for the project repository, and 35 points for the projectview repository.  If there are compiler warnings or errors in either of these repostiories, points will be deducted. The professor will excercise your code and make sure it works as expected. If not, there will be deductions for that as well. If everything is working as expected, then you will get full credit.

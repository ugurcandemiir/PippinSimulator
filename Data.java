package project;

public class Data {
	public static final int DATA_SIZE = 2048;
	private int[] data ;  
	private int changedIndex ;
	public Data (){
		changedIndex = -1;
		data = new int[DATA_SIZE]; 
	}
	int getData(int index) {
		   if (index < 0 || index > DATA_SIZE) {
		        throw new MemoryAccessException("Illegal access to data memory, index "+ index);
		    }
		return data[index];
		  
	}
	int[] getData() {
		return this.data;
	}


    void setData(int index, int value) {
    	 if (index < 0 || index > DATA_SIZE) {
		        throw new MemoryAccessException("Illegal access to data memory, index "+ index);
		    }
    	 else {
    		 this.data[index] = value;
    		 this.changedIndex = index;
    		

    	 }
    }
	public int getChangedIndex() {
		return changedIndex;
	}
	void clearData(int start, int end) {
		for(int i=start;i<end;i++) data[i]=0;
	changedIndex=-1;
	}
	
}

 

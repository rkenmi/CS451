
public class Frame {
	int error;
	String fileName;
	
	public Frame(String fN, int e){
		fileName = fN;
		error = e;
	}
	
	public void setError(int e){
		error = e;
	}
	
	public int getError(){
		return error;
	}
	
	public String getFileName(){
		return fileName;
	}
}

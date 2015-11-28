
public class Frame {
	int error;
	int index;
	
	public Frame(int i, int e){
		index = i;
		error = e;
	}
	
	public void setError(int e){
		error = e;
	}
	
	public int getError(){
		return error;
	}
	
	public void setIndex(int i){
		index = i;
	}
	
	public int getIndex(){
		return index;
	}
	
    public int compare(Frame o) {
        if(error > o.getError())
        	return 1;
        else
        	return 0;
    }
}

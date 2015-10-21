import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class DictCoding {
	private List<String> dict;
	private List<String> encoded = new ArrayList<String>();
	private String str2encode = null, decoded = "";
	
	private int userDictSize, bitsPerSymbol = 8, preDataSize, newDataSize;
	
	public DictCoding(){
		;
	}
	
	public DictCoding(String fileName){
		readTXT(fileName);
	}
	
	public DictCoding(String fileName, int dictSize){
		this.userDictSize = dictSize;
		readTXT(fileName);
	}
	
	public List<String> getDict() {
		return dict;
	}
	
	public String getDecoded() {
		return decoded;
	}
	
	public List<String> getEncoded() {
		return encoded;
	}
	
	public void setDictSize(int dictSize){
		this.userDictSize = dictSize;
	}
	
	public Boolean isFileRead(){
		if (str2encode == null)
			return false;
		else
			return true;
	}
	
	public Boolean readTXT(String fileName){
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        line = br.readLine();
		    }
		    str2encode = sb.toString();
		    //System.out.println(str2encode);
		    //System.out.println(str2encode.length());
			System.out.println("Read "+fileName+" Successfully.");
			br.close();
			return true;
		} // try
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			return false;
		}
	}
	
	public void initDict (){
		dict = new ArrayList<String>();
		int di = 0; // dictionary index
		String temp, old = "";
		
		for(int i = 0; i < str2encode.length(); i++){
			temp = Character.toString(str2encode.charAt(i));
			if(!old.contains(temp)){
				dict.add(di, temp);
				old += temp;
				di++;
			}
		}
		
		preDataSize =  (str2encode.length() * bitsPerSymbol);
	}
	
	public List<String> encode() {
		int codeIndex = 0;
				
		initDict();
		// begin encoding
		for(int i = 0; i < str2encode.length(); i++){
			String currSeq = Character.toString(str2encode.charAt(i) );
					
			while( i + 1 < str2encode.length()){
				String nextChar = Character.toString(str2encode.charAt(i + 1));
				
				if ( dict.contains(currSeq + nextChar) ){
					currSeq = currSeq + nextChar;
					i++;
				}else{
					if(dict.size() < userDictSize)
						dict.add(dict.size(), currSeq + nextChar); // append to dictionary the new entry
					
					encoded.add(codeIndex, Integer.toString(dict.indexOf(currSeq)));
					codeIndex++;
					break;
				}
	
			}
			if ( i == str2encode.length() - 1 )
				encoded.add(codeIndex, Integer.toString(dict.indexOf(currSeq)));
		}
		
		newDataSize = encoded.size() * (int) ( Math.log(dict.size()) / Math.log(2) );
		
		System.out.println("Data Size before Compression = " + preDataSize + " bits");
		System.out.println("Data Size after Compression = " + newDataSize + " bits");
		System.out.println("Compress ratio = " + preDataSize/(double)newDataSize);
		
		return encoded;
	}
	
	public String decode (String encodedStr) {
		List<String> encodedStrArr = new ArrayList<String>(); // empty encoded array
		StringTokenizer st = new StringTokenizer(encodedStr, " ");
		
		while (st.hasMoreElements()){
			encodedStrArr.add(encodedStrArr.size(), (String) st.nextElement() );
		}
		
		initDict();
		for(int i = 0; i < encodedStrArr.size(); i++){
			String k =  encodedStrArr.get(i);
			String e = dict.get(Integer.parseInt(k));
			decoded += e;

			if ( i + 1 < encodedStrArr.size() ){
				String k2 = encodedStrArr.get(i + 1);
			
				if (dict.size() < userDictSize && Integer.parseInt(k2) < dict.size()){
					String e2 = dict.get(Integer.parseInt(k2));
					if ( dict.contains(e2))
						dict.add(e + Character.toString(e2.charAt(0)));
				} else {
					dict.add(e + Character.toString(e.charAt(0)));
				}

			}
		}
		return decoded;
	}
}

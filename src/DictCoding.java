import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class DictCoding {
	private List<String> dict;
	private List<String> encoded = new ArrayList<String>();
	private String decoded = "";
	
	private int maxDictSize, bitsPerSymbol, preDataSize, newDataSize;
	private String str;
	
	public DictCoding(){
		maxDictSize = 256;
		bitsPerSymbol = 8;
		//str = "abbaabbaababbaaaabaabba";
		str = "Multimedia is media and content that uses a combination of different content forms. The term can be used as a noun (a medium with multiple content forms) or as an  adjective describing a medium as having multiple content forms. The term is used in contrast to media which only use traditional forms of printed or hand-produced material. Multimedia includes a combination of text, audio, still images, animation, video, and interactivity content forms. Multimedia is usually recorded and played, displayed or accessed by information content processing devices, such as computerized and electronic devices, but can also be part of a live performance. Multimedia (as an adjective) also describes electronic media devices used to store and experience multimedia content. Multimedia is distinguished from mixed media in fine art; by including audio, for example, it has a broader scope. The term rich media is synonymous for interactive multimedia. Hypermedia can be considered one particular multimedia application.";
		initDict();
		encode();
		System.out.println("preDataSize = " + preDataSize + " bits");
		System.out.println("newDataSize = " + newDataSize + " bits");
		System.out.println("Compress ratio = " + preDataSize/(double)newDataSize);
	
		String encodeStr = "";
		for(int i = 0; i < encoded.size(); i++) encodeStr += " " + encoded.get(i);
		decode(encodeStr);
		
		System.out.println(decoded);
	}
	
	public void initDict (){
		dict = new ArrayList<String>();
		int di = 0; // dictionary index
		String temp, old = "";
		
		for(int i = 0; i < str.length(); i++){
			temp = Character.toString(str.charAt(i));
			if(!old.contains(temp)){
				dict.add(di, temp);
				old += temp;
				di++;
			}
		}
		
		preDataSize =  (str.length() * bitsPerSymbol);
	}
	
	public void encode() {
		int codeIndex = 0;
				
		// begin encoding
		for(int i = 0; i < str.length(); i++){ // change to str.length() later
			String currSeq = Character.toString(str.charAt(i) );
					
			while( i + 1 < str.length()){
				String nextChar = Character.toString(str.charAt(i + 1));
				
				if ( dict.contains(currSeq + nextChar) ){
					currSeq = currSeq + nextChar;
					i++;
				}else{
					if(dict.size() < maxDictSize)
						dict.add(dict.size(), currSeq + nextChar); // append to dictionary the new entry
					
					encoded.add(codeIndex, Integer.toString(dict.indexOf(currSeq)));
					codeIndex++;
					break;
				}
	
			}
			if ( i == str.length() - 1 )
				encoded.add(codeIndex, Integer.toString(dict.indexOf(currSeq)));
		}
		
		System.out.println();
		newDataSize = encoded.size() * (int) ( Math.log(dict.size()) / Math.log(2) );
	}
	
	public void decode (String encodeStr) {
		List<String> encodeStrArr = new ArrayList<String>(); // empty encoded array
		StringTokenizer st = new StringTokenizer(encodeStr, " ");
		
		while (st.hasMoreElements()){
			encodeStrArr.add(encodeStrArr.size(), (String) st.nextElement() );
		}
		
		initDict();
		for(int i = 0; i < encodeStrArr.size(); i++){
			String k =  encodeStrArr.get(i);
			String e = dict.get(Integer.parseInt(k));
			decoded += e;

			if ( i + 1 < encodeStrArr.size() ){
				String k2 = encodeStrArr.get(i + 1);
				String e2 = dict.get(Integer.parseInt(k2));
				if( dict.size() < maxDictSize ){
					if ( dict.contains(e2))
						dict.add(e + Character.toString(e2.charAt(0)));
					else 
						dict.add(e + Character.toString(e.charAt(0)));
				}
			}
		}
		System.out.println();
	}
}

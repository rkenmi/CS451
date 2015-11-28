import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/*******************************************************
 CS451 Multimedia Software Systems
 @ Author: Elaine Kang
 *******************************************************/

// Template Code

public class CS451_Miyamoto
{
  public static void main(String[] args)
  {
  	Scanner scan = new Scanner(System.in);
  	int i1= -1, i2 = -1, i3 = -1, i4 = -1;
  	    
	do{
	    if(args[0].equals("1") && args.length == 2){
		    System.out.println("--Welcome to Multimedia Software System--");
	    	ImageQuantization img = new ImageQuantization(args[1]);
	    	
	    	if (img.getSize() != 0){
		    	System.out.println("Main Menu-----------------------------------");
		    	System.out.println("1. Conversion to Gray-scale Image (24bits->8bits)");
		    	System.out.println("2. Conversion to N-level Image");
		    	System.out.println("3. Conversion to 8bit Indexed Color Image using Uniform Color Quantization (24bits ->8bits)");
		    	System.out.println("4. Quit");
		    	System.out.print("\nPlease enter the task number [1-4]: ");
		    	
		    	i1 = scan.nextInt();
		    	switch(i1){
		    	case 1 : img.Threshold();
		    		break;
		    	case 2 : {
		    		int n = 0;
		    		while( n != 2 && n != 4 && n != 8 && n != 16 ){
			    		System.out.print("Enter N : ");
		    			n = scan.nextInt();
		    		}
		    		img.N_Level(n);
		    		break;
		    	}
		    	case 3 : img.UCQuant();
		    		break;
		    	case 4 : System.out.println("Quitting.");
		    		break;
		    	default : System.out.println("Invalid Task Number.");
		    			break;
		    	}
		    	if (i1 > 0 && i1 < 4) // only display image after some work is complete
		    		img.display(args[1] + "-out");
		        System.out.println("End of Menu---------------------------------\n");
	    	}
	    	else break;
	    } else if (args[0].equals("2")){
	      	System.out.println("Main Menu-----------------------------------");
	      	System.out.println("1. Aliasing");
	      	System.out.println("2. Dictionary Coding");
	      	System.out.println("3. Quit");
	      	System.out.print("\nPlease enter the task number [1-3]: ");
	      	i2 = scan.nextInt();
	      	switch(i2){
	      	case 1: {
	      		int m = 0, n = 0, k = 0;
	      		while( k < 1 || k > 16){
		      		System.out.println("Enter M, N, and K (1-16) : ");
		      		m = scan.nextInt();
		      		n = scan.nextInt();
		      		k = scan.nextInt();
	      		}
		      	System.out.println("Settings: " + m + " " + n + " " + k);
	      		Aliasing a1 = new Aliasing(m, n, k);
		  		a1.resize_NoFilter(k);
				a1.resize_AvgFilter(k);
				a1.resize_3x3Filter(k, 1);
				a1.resize_3x3Filter(k, 2);
		      	break;
	      	}
	      	case 2 : {
	      		String fileName, encodedStr = "", decodedStr;
	      		int d; // ignore init value
	      		DictCoding dc = new DictCoding();
	      		
	      		while(!dc.isFileRead()){
		      		System.out.print("Enter filename: ");
		      		fileName = scan.next();
		      		dc.readTXT(fileName);
		      		System.out.println();
	      		}
	      		d = -1;
	      		while(d < 1 || d > 256){
		      		System.out.print("Enter dictionary size (<=256): ");
	      			d = scan.nextInt();
	      		}
	      		dc.setDictSize(d);
	      		System.out.println();
	      		
	      		List<String> enc = dc.encode();
	    		for(int i = 0; i < enc.size(); i++) 
	    			encodedStr += " " + enc.get(i);
	    		
	    		System.out.println("Encoded Result : " + encodedStr);
	    		decodedStr = dc.decode(encodedStr);
	    		System.out.println("Decoded Message : " + decodedStr + "\n");
	      		
	      		break;
	      	}

	    	case 3 : System.out.println("Quitting.");
    			break;
	    	default : System.out.println("Invalid Task Number.");
    			break;
	      	}
	    }
      	else if (args[0].equals("3") && args.length == 2 ){
      		int n_choice = -1;
      		while( n_choice < 0 || n_choice > 5){
		      	System.out.print("\nPlease enter n value (0-5) for quantization : ");
		      	n_choice = scan.nextInt();
      		}
      		
      		DCTCompress img = new DCTCompress(args[1], n_choice);
      		
      		i1 = 4;
      	}
      	else if (args[0].equals("4") && args.length == 1 ){
	      	System.out.println("Main Menu-----------------------------------");
	      	System.out.println("1. Block-Based Motion Compensation");
	      	System.out.println("2. Removing Moving Objects (based on IDB)");
	      	System.out.println("3. Image Retrieval");
	      	System.out.println("4. Quit");
	      	System.out.print("\nPlease enter the task number [1-4]: ");
	      	i4 = scan.nextInt();
	      	switch(i4){
	      	case 1: {
	      		int n = 0, p = 0;
	      		Image ref = null, tar = null;
	      		String fileName;
	      		
	      		while( n != 8 && n != 16 && n != 32){
		      		System.out.println("Enter n (8, 16, 32) : ");
		      		n = scan.nextInt();
	      		}
	      		while( p != 4 && p != 8 && p != 12){
	      			System.out.println("Enter p (4, 8, 12) : ");
	      			p = scan.nextInt();
	      		}
	      		while( tar == null || tar.getSize() == 0 ){
	      			System.out.println("Enter file name for TARGET image:\n(Example: Walk_002.ppm)");
	      			fileName = scan.next();
	      			tar = new Image(fileName);
	      		}
	      		while( ref == null || ref.getSize() == 0 ){
	      			System.out.println("Enter file name for REFERENCE image:\n(Example: Walk_001.ppm)");
	      			fileName = scan.next();
	      			ref = new Image(fileName);
	      		}
		      	System.out.println("Settings: n = " + n + ", p = " + p);
	      		MC Hammer = new MC(n, p,  ref,  tar);
	      		Image err = new  Image(ref.getW(), ref.getH());
	      		Hammer.mv2txt(err, ref, tar);
		      	break;
	      	}
	      	case 2 : {
	      		Image ref = null, tar = null;
	      		int n = 0;
	      		String fileName = "Walk_";
	      		while( n < 19 || n > 179 ){
	      			System.out.println("Enter TARGET frame (between 19 and 179): ");
	      			n = scan.nextInt();
	      			if(n < 100)
	      				fileName += "0";
	      			tar = new Image(fileName + n + ".ppm");
	      		}
	      		if(n == 100 || n == 101)
	      			fileName = "Walk_";
	      		ref = new Image(fileName + Integer.toString(n-2) + ".ppm");
	      		MC Hammer = new MC(16, 4, ref, tar);
	      		
	    		Image fifth = new Image("Walk_005.ppm");
	      		Hammer.rmMovingObj(fifth);
	      		
	      		break;
	      	}
	      	
	      	case 3: {
	      		Image ref = null, tar = null;
	      		int n = 0;
	      		String fileName = "Walk_";
	      		while( n < 1 || n > 194 ){
	      			System.out.println("Enter TARGET frame: ");
	      			n = scan.nextInt();
	      		}
	      		
      			if(n < 100 && n > 9)
      				fileName += "0";
      			else if(n < 10)
      				fileName += "00";
      			tar = new Image(fileName + n + ".ppm");

	      		MC Hammer;
	      		String refFileName;
	      		List<Frame> stats = new ArrayList<Frame>();
	      		
	      		for(int i = 1; i < 194; i++){
	      			if(i != n){
		      			refFileName = "Walk_";
		      			if(i < 10)
		      				refFileName += "00";
		      			else if (i < 100)
		      				refFileName += "0";
		      			else ;
		      			ref = new Image(refFileName + i + ".ppm");
		      			Hammer = new MC(16, 4, ref, tar);
		      			int stat = Hammer.similarity() ;
		      			
		      			stats.add(new Frame(i, stat));
	      			}
	      		}

	      		Collections.sort(stats, new Comparator<Frame>(){
	      		    public int compare(Frame a, Frame b) {
	      		        return a.getError() - b.getError();
	      		    }
	      		});
	      		
	      		int maxVal = stats.get(stats.size()-1).getError();
	      		
	      		System.out.println("\nInput image name: " + tar.getFileName() +".ppm");
	      		System.out.println("Half-pixel Accuracy: Off");
	      		System.out.println("Top 3 similar images in IDB");
	      		
	      		for(int f = 0; f < 3; f++){
	      			int val = stats.get(f).getError();
	      			int frame = stats.get(f).getIndex();
	      			
	      			double percent = 1 - (val / (double) maxVal );
	      			fileName = "Walk_";
	      			if(frame < 10)
	      				fileName += "00";
	      			else if (frame < 100)
	      				fileName += "0";
	      			else ;
	      			System.out.println("\t" + fileName + frame + ".ppm : "  + "\t\t" + percent * 100 + "%");
	      			
	      		}	
	      		break;
	      	}

	    	case 4 : System.out.println("Quitting.");
    			break;
	    	default : System.out.println("Invalid Task Number.");
    			break;
	      	}

      	}
	} while (i1 != 4 && i2 != 3 && i4 != 4);
    	scan.close();
        System.out.println("--Good Bye--");
    

    System.exit(0);
  }

  
  public static void usage()
  {
    System.out.println("\nUsage: java CS451_Miyamoto [homework number] [inputfile]\n");
  }
}
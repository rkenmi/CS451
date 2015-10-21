import java.util.ArrayList;
import java.util.List;
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
  	int i1= -1, i2 = -1;
  	    
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
	} while (i1 != 4 && i2 != 3);
    	scan.close();
        System.out.println("--Good Bye--");
    

    System.exit(0);
  }

  public static void usage()
  {
    System.out.println("\nUsage: java CS451_Miyamoto [homework number] [inputfile]\n");
  }
}
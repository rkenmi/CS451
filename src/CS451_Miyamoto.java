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
    if(args.length != 2)
    {
      usage();
      System.exit(1);
    }

    
    if(args[0].equals("1")){
    	Scanner scan = new Scanner(System.in);
    	
    	int i = -1;
    	do{
    	    System.out.println("--Welcome to Multimedia Software System--");
	    	ImageQuantization img = new ImageQuantization(args[1]);
	
	    	if (img.getSize() != 0){
		    	System.out.println("Main Menu-----------------------------------");
		    	System.out.println("1. Conversion to Gray-scale Image (24bits->8bits)");
		    	System.out.println("2. Conversion to N-level Image");
		    	System.out.println("3. Conversion to 8bit Indexed Color Image using Uniform Color Quantization (24bits ->8bits)");
		    	System.out.println("4. Quit");
		    	System.out.print("\nPlease enter the task number [1-4]: ");
		    	
		    	i = scan.nextInt();
		    	switch(i){
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
		    	if (i > 0 && i < 4) // only display image after some work is complete
		    		img.display(args[1] + "-out");
		        System.out.println("End of Menu---------------------------------\n");
	    	}
    	} while (i != 4);
    	scan.close();
        System.out.println("--Good Bye--");
    }else{
    	System.err.println("Invalid Homework Number!");
    }
    System.exit(0);
  }

  public static void usage()
  {
    System.out.println("\nUsage: java CS451_Miyamoto [homework number] [inputfile]\n");
  }
}
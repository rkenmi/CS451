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
    if(args.length == 0 || args.length > 2)
    {
      usage();
      System.exit(1);
    }
    System.out.println("--Welcome to Multimedia Software System--");
    
    if(args[0].equals("1")){
    	Scanner scan = new Scanner(System.in);
    	
    	ImageQuantization img = new ImageQuantization(args[1]);
    	System.out.println("Main Menu-----------------------------------");
    	System.out.println("1. Conversion to Gray-scale Image (24bits->8bits)");
    	System.out.println("2. Conversion to N-level Image");
    	System.out.println("3. Conversion to 8bit Indexed Color Image using Uniform Color Quantization (24bits ->8bits)");
    	System.out.println("4. Quit");
    	System.out.println("\nPlease enter the task number [1-4]");
    	
    	int i = scan.nextInt();
    	switch(i){
    	case 1 : img.Threshold();
    		break;
    	case 2 : {
    		System.out.println("Enter N = ");
    		int n= scan.nextInt();
    		if (n == 2 || n == 4 || n == 8 || n == 16)
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
    }
    System.out.println("--Good Bye--");
  }

  public static void usage()
  {
    System.out.println("\nUsage: java CS451_Main [homework number] [inputfile]\n");
  }
}
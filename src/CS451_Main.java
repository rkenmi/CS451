/*******************************************************
 CS451 Multimedia Software Systems
 @ Author: Elaine Kang
 *******************************************************/

// Template Code

public class CS451_Main
{
  public static void main(String[] args)
  {
    if(args.length != 1)
    {
      usage();
      System.exit(1);
    }

    System.out.println("--Welcome to Multimedia Software System--");

    ImageQuantization img = new ImageQuantization(args[0]);

    //img.display(args[0]+"-out");
    //img.write2PPM("out.ppm");
    //img.Grayscale();
    img.UCQuant();

    System.out.println("--Good Bye--");
  }

  public static void usage()
  {
    System.out.println("\nUsage: java CS451_Main [inputfile]\n");
  }
}
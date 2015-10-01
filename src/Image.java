/*******************************************************
 CS451 Multimedia Software Systems
 @ Author: Elaine Kang

 This image class is for a 24bit RGB image only.
 *******************************************************/

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

import javax.swing.*;

public class Image
{
  private String fileName;
  private int width;				// number of columns
  private int height;				// number of rows
  private int pixelDepth=3;			// pixel depth in byte
  BufferedImage img;				// image array to store rgb values, 8 bits per channel
  private int[][] LUT;					// look up table

  public Image(int w, int h)
  // create an empty image with width and height
  {
	width = w;
	height = h;

	img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	System.out.println("Created an empty image with size " + width + "x" + height);
  }

  public Image(String fileName)
  // Create an image and read the data from the file
  {
	  this.fileName = fileName.substring(0, fileName.indexOf(".ppm") );
	  readPPM(fileName);
	  System.out.println("Created an image from " + fileName+ " with size "+width+"x"+height);
  }

  public int getW()
  {
	return width;
  }

  public int getH()
  {
	return height;
  }

  public int getSize()
  // return the image size in byte
  {
	return width*height*pixelDepth;
  }

  public void setPixel(int x, int y, byte[] rgb)
  // set rgb values at (x,y)
  {
	int pix = 0xff000000 | ((rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | (rgb[2] & 0xff);
	img.setRGB(x,y,pix);
  }

  public void setPixel(int x, int y, int[] irgb)
  // set rgb values at (x,y)
  {
	byte[] rgb = new byte[3];

	for(int i=0;i<3;i++)
	  rgb[i] = (byte) irgb[i];

	setPixel(x,y,rgb);
  }

  public void getPixel(int x, int y, byte[] rgb)
  // retreive rgb values at (x,y) and store in the array
  {
  	int pix = img.getRGB(x,y);

  	rgb[2] = (byte) pix;
  	rgb[1] = (byte)(pix>>8);
  	rgb[0] = (byte)(pix>>16);
  }


  public void getPixel(int x, int y, int[] rgb)
  // retreive rgb values at (x,y) and store in the array
  {
	int pix = img.getRGB(x,y);

	byte b = (byte) pix;
	byte g = (byte)(pix>>8);
	byte r = (byte)(pix>>16);

    // converts singed byte value (~128-127) to unsigned byte value (0~255)
	rgb[0]= (int) (0xFF & r);
	rgb[1]= (int) (0xFF & g);
	rgb[2]= (int) (0xFF & b);
  }

  public void displayPixelValue(int x, int y)
  // Display rgb pixel value at (x,y)
  {
	int pix = img.getRGB(x,y);

	byte b = (byte) pix;
	byte g = (byte)(pix>>8);
	byte r = (byte)(pix>>16);

    System.out.println("RGB Pixel value at ("+x+","+y+"):"+(0xFF & r)+","+(0xFF & g)+","+(0xFF & b));
   }

  public void UCQuant(){
	  int rgb[] = new int[3];
	  int matchIndex = 0;
	  int i = 0;
	  
	  buildLUT();
	  displayLUT();
	  
	  for(int x = 0; x < width; x++){
		  for(int y = 0; y < height; y++){
			  getPixel(x, y, rgb);
			  //displayPixelValue(x, y);
			  /*
			  if(x == 243 && y == 130){
				  System.out.println("rgb[0] = " + rgb[0]);
				 System.out.println("rgb[1] = " + rgb[1]);
				 System.out.println("rgb[2] = " + rgb[2]);
				  // 230 | 164 | 0
			  }
			  */
			  i = 0;
			  while(i < 256){
				  //System.out.println("rgb[0] = " + rgb[0]);
				 // System.out.println("rgb[1] = " + rgb[1]);
				 // System.out.println("rgb[2] = " + rgb[2]);
				  //System.out.println(i + "----->  " + LUT[0][i] + ", " + LUT[1][i] + ", " + LUT[2][i]);
				  if(rgb[2] < 255){
					  /*
				  System.out.println(rgb[0] + " :         " +  (LUT[0][i] - 16 )  + "-" + (LUT[0][i] + 16 ) );
				  System.out.println(rgb[1] + " :         " +  (LUT[1][i] - 16 )  + "-" + (LUT[1][i] + 16 ) );
				  System.out.println(rgb[2] + " :         " +  (LUT[2][i] - 32 )  + "-" + (LUT[2][i] + 32 ) );
				  System.out.println("i : " + i + "\n");
				  */
				  }
				  if ( (rgb[0] < LUT[0][i] + 16 && rgb[0] >= LUT[0][i] - 16)
						  && (rgb[1] < LUT[1][i] + 16 && rgb[1] >= LUT[1][i] - 16)
						  && (rgb[2] < LUT[2][i] + 32 && rgb[2] >= LUT[2][i] - 32) ){
					  matchIndex = i;
					  break;
				  }
				  i++;
			  }
			  
			  //System.out.println("x : " + x + "\ny : " + y + "\nMatchIndex : " + matchIndex);
			  rgb[0] = matchIndex;
			  rgb[1] = matchIndex;
			  rgb[2] = matchIndex;
			  setPixel(x, y, rgb);
			  
		  }
	  }
	  
	  for (int x = 0; x < width; x++){
		  for (int y = 0; y < height; y++){
			  rgb = new int[3];
			  getPixel(x, y, rgb);
			  

			  int currIndex = rgb[0];
			  rgb[0] = LUT[0][currIndex];
			  rgb[1] = LUT[1][currIndex];
			  rgb[2] = LUT[2][currIndex];
			  
			  setPixel(x, y, rgb);
		  }
	  }
	  
	  write2PPM(fileName+"_8bitUCQuant.ppm");
  }
  
  public void buildLUT(){
	  LUT = new int[3][256]; // 3 colors (RGB) to choose from and LUT index (max index is 2 to the pow of 8 = 256 choices within a byte)
	  
	  for (int i = 0; i < 256; i++){ // i = LUT index
		  		
			  String bS = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
			  String r = bS.substring(0, 3);
			  String g = bS.substring(3, 6);
			  String b = bS.substring(6, 8);
			  
			  if(r.charAt(0) == '1' || r.charAt(1) == '1' || r.charAt(2) == '1')
				  LUT[0][i] = 16 + (32 * (4 * Character.getNumericValue(r.charAt(0)) + 2 * Character.getNumericValue(r.charAt(1)) + 1 * Character.getNumericValue(r.charAt(2))) )  ;
			  else
				  LUT[0][i] = 16;

			  if(g.charAt(0) == '1' || g.charAt(1) == '1' || g.charAt(2) == '1')
				  LUT[1][i] = 16 + (32 * (4 * Character.getNumericValue(g.charAt(0)) + 2 * Character.getNumericValue(g.charAt(1)) + 1 * Character.getNumericValue(g.charAt(2))) )   ;
			  else
				  LUT[1][i] = 16;
			  
			  if(b.charAt(0) == '1' || b.charAt(1) == '1')
				  LUT[2][i] = 32 + (64 * (2 * Character.getNumericValue(b.charAt(0)) + (1 * Character.getNumericValue(b.charAt(1))))  )   ;
			  else
				  LUT[2][i] = 32;
	  }
	  
  }
  
  public void displayLUT(){
	  String color = "hollow";
	  
	  for(int i = 0; i < 256; i++){
		  System.out.println("i = " + i);
		  for(int c = 0; c < 3; c++){
			  switch(c){
			  case 0 : color = "Red";
			  	break;
			  case 1 : color = "Green";
			  	break;
			  case 2 : color = "Blue";
			  	break;
			  }
			  System.out.println("          " + color + " ->    " + LUT[c][i]);
		  }
	  }
  }
  
  public void readPPM(String fileName)
  // read a data from a PPM file
  {
	FileInputStream fis = null;
	DataInputStream dis = null;

	try{
		fis = new FileInputStream(fileName);
		dis = new DataInputStream(fis);

		System.out.println("Reading "+fileName+"...");

		// read Identifier
		if(!dis.readLine().equals("P6"))
		{
			System.err.println("This is NOT P6 PPM. Wrong Format.");
			System.exit(0);
		}

		// read Comment line
		String commentString = dis.readLine();

		// read width & height
		String[] WidthHeight = dis.readLine().split(" ");
		width = Integer.parseInt(WidthHeight[0]);
		height = Integer.parseInt(WidthHeight[1]);

		// read maximum value
		int maxVal = Integer.parseInt(dis.readLine());

		if(maxVal != 255)
		{
			System.err.println("Max val is not 255");
			System.exit(0);
		}

		// read binary data byte by byte
		int x,y;
		//fBuffer = new Pixel[height][width];
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		byte[] rgb = new byte[3];
		int pix;

		for(y=0;y<height;y++)
		{
	  		for(x=0;x<width;x++)
			{
				rgb[0] = dis.readByte();
				rgb[1] = dis.readByte();
				rgb[2] = dis.readByte();
				setPixel(x, y, rgb);
			}
		}
		dis.close();
		fis.close();

		System.out.println("Read "+fileName+" Successfully.");

	} // try
	catch(Exception e)
	{
		System.err.println(e.getMessage());
	}
  }

  public void write2PPM(String fileName)
  // wrrite the image data in img to a PPM file
  {
	FileOutputStream fos = null;
	PrintWriter dos = null;

	try{
		fos = new FileOutputStream(fileName);
		dos = new PrintWriter(fos);

		System.out.println("Writing the Image buffer into "+fileName+"...");

		// write header
		dos.print("P6"+"\n");
		dos.print("#CS451"+"\n");
		dos.print(width + " "+height +"\n");
		dos.print(255+"\n");
		dos.flush();

		// write data
		int x, y;
		byte[] rgb = new byte[3];
		for(y=0;y<height;y++)
		{
			for(x=0;x<width;x++)
			{
				getPixel(x, y, rgb);
				fos.write(rgb[0]);
				fos.write(rgb[1]);
				fos.write(rgb[2]);

			}
			fos.flush();
		}
		dos.close();
		fos.close();

		System.out.println("Wrote into "+fileName+" Successfully.");

	} // try
	catch(Exception e)
	{
		System.err.println(e.getMessage());
	}
  }

  public void display(String title)
  // display the image on the screen
  {
     // Use a label to display the image
      JFrame frame = new JFrame();
      JLabel label = new JLabel(new ImageIcon(img));
      frame.add(label, BorderLayout.CENTER);
      frame.setTitle(title);
      frame.pack();
      frame.setVisible(true);
  }

} // Image class
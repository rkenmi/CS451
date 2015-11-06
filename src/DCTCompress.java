
public class DCTCompress extends Image {

	  public DCTCompress(String fileName){
		  super(fileName);
	  }
	
	  public void DCT (double input[][], double output[][]){
		  double C_u, C_v;
		  
		  for(int u = 0; u < 8; u++){
			  if (u == 0)
				  C_u = 1 / Math.sqrt(2);
			  else
				  C_u = 1;
			 
			  for(int v = 0; v < 8; v++){
				  if (v == 0)
					  C_v = 1 / Math.sqrt(2);
				  else
					  C_v = 1;

				  output[u][v] = 0;
				  
				  for(int x = 0; x < 8; x++)
					  for(int y = 0; y < 8; y++)
						 output[u][v] += (input[x][y] * Math.cos( ((2*x + 1) * u * Math.PI)  / 16 ) * Math.cos( ((2 * y + 1) * v * Math.PI)  / 16) );
				  
				  output[u][v] *= C_u * C_v * (1 / (float) 4);
				  if(output[u][v] > 1024)
					  output[u][v] = 1024;
				  else if (output[u][v] < -1024)
					  output[u][v] = -1024;
			  }
		  }
	  }
	  
	  public void InverseDCT (double input[][], double output[][]){
		  double C_u, C_v;
		  
		  for(int x = 0; x < 8; x++){
			  for(int y = 0; y < 8; y++){
				  output[x][y] = 0;
				  
				  for(int u = 0; u < 8; u++){
					  
					  if (u == 0)
						  C_u = 1 / Math.sqrt(2);
					  else
						  C_u = 1;
					  
					  for(int v = 0; v < 8; v++){
						  
						  if (v == 0)
							  C_v = 1 / Math.sqrt(2);
						  else
							  C_v = 1;
						  
						 output[x][y] += C_u * C_v * (input[u][v] * Math.cos( ((2*x + 1) * u * Math.PI)  / 16 ) * Math.cos( ((2 * y + 1) * v * Math.PI)  / 16) );
					  }
				  }
				  output[x][y] *= (1 / (float) 4);
			  }
		  }

	  }
	  
	  public double [][][] ColorSpaceTransform() {
		  
		  int rgb[] = new int[3];
		  double [][][] YCbCr = new double[getH()][getW()][3];
		  
		  for(int y = 0; y < getH(); y++)
			  for(int x = 0; x < getW(); x++){
				  getPixel(x, y, rgb);
				  
				  // Get Y, Cb, and Cr
				  YCbCr[y][x][0] = (rgb[0] * 0.2990) + (rgb[1] * 0.5870) + (rgb[2] * 0.1140); // Y
				  YCbCr[y][x][1] = (rgb[0] * -0.1687) + (rgb[1] * -0.3313) + (rgb[2] * 0.5); // Cb
				  YCbCr[y][x][2] = (rgb[0] * 0.5) + (rgb[1] * -0.4187) + (rgb[2] * -0.0813); // Cr
				  
				  // Truncate if necessary
				  for(int i = 1; i < 3; i++){
					  if(YCbCr[y][x][i] < -127.5 ){
						  YCbCr[y][x][i] = -127.5;
					  } else if (YCbCr[y][x][i] > 127.5){
						  YCbCr[y][x][i] = 127.5;
					  }
				  }

				  //Subtract 128 from Y, 0.5 from Cb and Cr
				  YCbCr[y][x][0] -= 128;
				  YCbCr[y][x][1] -= 0.5;
				  YCbCr[y][x][2] -= 0.5;
				  
				  System.out.println(YCbCr[y][x][0]);
				  
			  }

		  
		  return null;
	  }
	
}

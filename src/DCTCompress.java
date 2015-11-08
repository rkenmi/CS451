import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DCTCompress extends Image {
		Image oldImg, newImg;
		double [][][] YCbCr; // original YCbCr after transformation from RGB
		double [][] newY;
		double [][] newCb; // subsampled Cb
		double [][] newCr; // subsampled Cr
		int subsampleW = 0, subsampleH = 0, n = 0;

	  public DCTCompress(String fileName, int n){
		  super(fileName);
		  this.n = n;
		  resize();
		  			//resizeBack();
		  CStransform();
		  subsample();
		  		//supersample();
		  		//CSdetransform();
		  DivideBoxes();
		  //test();

	  }
	
	  public void test(){
		  List<Map.Entry<Integer ,Integer>> pairList= new ArrayList<>();
		  
		  double [][] exampleDCT = { 
				  {-72, -7, -2, -1, 0, 0, 0, 0},
				  {-1, 8, -8, -1, 0, 0, 0, 0},
				  {-8, 12, 1, -2, -1, -1, 0, 0},
				  {-4, 1, 2, 0, 0, 0, 0, 0},
				  {1, 1, 0, 1, 0, 0, 0, 0},
				  {0, 0, 1, 0, 0, 0, 0, 0},
				  {0, 0, 0, 0, 0, 0, 0, 0},
				  {0, 0, 0, 0, 0, 0, 0, 0}
		  };
		  
		  // AC coefficients
	  		int totalBits = 0, y = 0, x = 1, c = 0, valCount = 0;
	  		
	  		double val = -9999; // only for init
	  		
		  		while(y >= 0 && y < 8){
					  while(x >= 0 && x < 8 && y >= 0 && y < 8){
						  if (val == -9999) {
							  val = exampleDCT[y][x];
							  valCount = 1;
						  } 
						  else if( exampleDCT[y][x] != val ){
							  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
							  valCount = 1;
							  val = exampleDCT[y][x];
						  } else
							  valCount++;
						  
						  //System.out.print(val + "    ");
						  y++; 
						  x--;
					  }
					  if(x == 5 && y == 8){
						  
						  if( exampleDCT[7][7] != val ){
							  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
							  valCount = 1;
							  val = exampleDCT[7][7];
							  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
						  }else {
							  valCount++;
							  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
						  }
						  //System.out.println("\n" + val + "    ");
						  break;
					  }
					  
					  if(x > 0)
						  c++;

					  x = 0 + c;
					  if(c > 0){
						  x++;
						  y = 7;
					  }
					  
					  if(y == 8){
						  x = 1;
						  y = 7;
						  c = 1;
					  }
					  
					  //System.out.println();
					  while(x >= 0 && x < 8 && y >= 0 && y < 8){
						  if( exampleDCT[y][x] != val ){
							  
							  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
							  valCount = 1;
							  val = exampleDCT[y][x];
						  } else
							  valCount++;
						  
						  //System.out.print(val + "    ");
						  y--; 
						  x++;
					  }
					  if(y > 0)
						  c++;
					  
					  y = 0 + c;
					  if(c > 0){
						  y++;
						  x = 7;
					  }
					  //System.out.println();
		  		}
		  		

		  		int DCbits = 10, ACbits = 10 - n, RLbits = 6;

		  		int total8x8Bits = 0 + DCbits;
		  		for(int i  = 0 ; i < pairList.size(); i ++){
		  			total8x8Bits += (ACbits + RLbits);
		  			System.out.println(pairList.get(i));
		  		}
		  		System.out.println(pairList.size() + " entries");

	  }
	  
	  public void setN(int n){
		  this.n = n;
	  }
	  
	  public void DivideBoxes(){
		  int totalBits = 0, totalY = 0, totalCb = 0, totalCr = 0;
		  
		  for(int y = 0; y < newImg.getH(); y+=8){ // newImg.getH()
			  for(int x = 0; x < newImg.getW(); x+=8){ // newImg.getW()
				  double [][] inY = new double[8][8], outY = new double[8][8], inCb = new double[8][8], outCb = new double[8][8], inCr = new double[8][8], outCr = new double[8][8];
				  
				  for(int j = 0, v = y; v < y + 8; v++, j++){
					  for(int i = 0, u = x; u < x + 8; u++, i++){
						  //System.out.println("Y -> j = " + j + ", i = " + i);
						  inY[j][i] = newY[v][u];
						  //System.out.println("For Y : (" + v + ", " + u + ")");
					  }
				  }
					calcDCT(inY, outY);
				  
				
					if(y < subsampleH && x < subsampleW){
						  for(int j = 0, v = y; v < y + 8; v++, j++)
							  for(int i = 0, u = x; u < x + 8; u++, i++){
								  //System.out.println("[" + v + ", " + u + "] " + "CbCr -> j = " + j + ", i = " + i);
								  //System.out.println("x/2 : " + x/2);
								  inCb[j][i] = newCb[v][u];
								  inCr[j][i] = newCr[v][u];
								  //System.out.println("For CbCr : (" + v + ", " + u + ")");
						  }
					}
					calcDCT(inCb, outCb);	
					calcDCT(inCr, outCr);

					double inverseDCT_Y [][] = new double[8][8], inverseDCT_Cb [][] = new double[8][8], inverseDCT_Cr [][] = new double[8][8];
					decalcDCT(outY, inverseDCT_Y);
					decalcDCT(outY, inverseDCT_Cb);
					decalcDCT(outY, inverseDCT_Cr);
					
					double [][][] DCT = new double[3][8][8];
					DCT[0] = outY;
					DCT[1] = outCb;
					DCT[2] = outCr;
					double [][][] quantizedDCT = Quantization(DCT, this.n);
					double [][][] restoredDCT = Dequantization(quantizedDCT, this.n);
					
					/*
					for(int i = 0; i < 8; i ++)
						for(int j = 0; j < 8; j++){
							System.out.println("DCT = " + DCT[0][i][j]);
							System.out.println("Quantized DCT = " + quantizedDCT[0][i][j]);
						}
					*/
					
					for(int k = 0; k < 3; k++){
						switch(k){
						case 0 : totalY += CompressRatio(k, quantizedDCT, this.n);
							break;
						case 1 : totalCb += CompressRatio(k, quantizedDCT, this.n);
							break;
						case 2 : totalCr += CompressRatio(k, quantizedDCT, this.n);
							break;
						}
						//System.out.println("(" + y + ", " + x + ") @ k = " + k + " --> " + CompressRatio(k, quantizedDCT, this.n) );
						//totalBits += CompressRatio(k, quantizedDCT, n);
					}
			  }
		  }
		  System.out.println("\nFor quantization level n = " + this.n);
		  System.out.println("Original image cost is " + img.getWidth() * img.getHeight() * 24 + " bits");
		  //System.out.println("Old  image HxW : " + getH() + " " + getW());
		  System.out.println("The  Y values cost is " + totalY + " bits");
		  System.out.println("The  Cb values cost is " + totalCb + " bits");
		  System.out.println("The  Cr values cost is " + totalCr + " bits");
		  totalBits = totalY + totalCb + totalCr;
		  System.out.println("Total compressed image cost : " + totalBits + " bits");
		  System.out.println("Compression Ratio  : " + (img.getWidth() * img.getHeight() * 24) / (double) (totalBits));
	  }
	  
	  public int CompressRatio(int k, double [][][] quantizedTable, int n){
		  
		  List<Map.Entry<Integer ,Integer>> pairList= new ArrayList<>();

		 // pretty8x8display(quantizedTable[k]);
		  // AC coefficients
		  		int totalBits = 0, y = 0, x = 1, c = 0, valCount = 0;
		  		
		  		double val = -9999; // only for init
		  		
			  		while(y >= 0 && y < 8){
						  while(x >= 0 && x < 8 && y >= 0 && y < 8){
							  if (val == -9999) {
								  val = quantizedTable[k][y][x];
								  valCount = 1;
							  } 
							  else if( quantizedTable[k][y][x] != val ){
								  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
								  valCount = 1;
								  val = quantizedTable[k][y][x];
							  } else
								  valCount++;
							  
							  //System.out.print(val + "    ");
							  y++; 
							  x--;
						  }
						  if(x == 5 && y == 8){
							  
							  if( quantizedTable[k][7][7] != val ){
								  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
								  valCount = 1;
								  val = quantizedTable[k][7][7];
								  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
							  }else {
								  valCount++;
								  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
							  }
							  //System.out.println("\n" + val + "    ");
							  break;
						  }
						  
						  if(x > 0)
							  c++;
	
						  x = 0 + c;
						  if(c > 0){
							  x++;
							  y = 7;
						  }
						  
						  if(y == 8){
							  x = 1;
							  y = 7;
							  c = 1;
						  }
						  
						  //System.out.println();
						  while(x >= 0 && x < 8 && y >= 0 && y < 8){
							  if( quantizedTable[k][y][x] != val ){
								  
								  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
								  valCount = 1;
								  val = quantizedTable[k][y][x];
							  } else
								  valCount++;
							  
							  //System.out.print(val + "    ");
							  y--; 
							  x++;
						  }
						  if(y > 0)
							  c++;
						  
						  y = 0 + c;
						  if(c > 0){
							  y++;
							  x = 7;
						  }
						  //System.out.println();
			  		}
			  		

			  		int DCbits = 10, ACbits = 10 - n, RLbits = 6;
			  		if(k != 0){
			  			DCbits--;
			  			ACbits--;
			  		}
			  		int total8x8Bits = 0 + DCbits;
			  		for(int i  = 0 ; i < pairList.size(); i ++){
			  			total8x8Bits += (ACbits + RLbits);
			  			//System.out.println(pairList.get(i));
			  		}
			  		//System.out.println("For k = " + k + " : " + total8x8Bits);
			  		//totalBits += total8x8Bits;
		  		
		  		//System.out.println("For this 8x8 Block, total bits : " + totalBits);
		  		return total8x8Bits;

	  }
	  
	  public double[][][] Dequantization(double[][][] quantizedTable, int n){
		  double [][] Y_qTable = {
				  {4, 4, 4, 8, 8, 16, 16, 32},
				  {4, 4, 4, 8, 8, 16, 16, 32},
				  {4, 4, 8, 8, 16, 16, 32, 32},
				  {8, 8, 8, 16, 16, 32, 32, 32},
				  {8, 8, 16, 16, 32, 32, 32, 32},
				  {16, 16, 16, 32, 32, 32, 32, 32},
				  {16, 16, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32, 32}
		  };
		  double [][] C_qTable = {
				  {8, 8, 8, 16, 32, 32, 32, 32},
				  {8, 8, 8, 16, 32, 32, 32, 32},
				  {8, 8, 16, 32, 32, 32, 32, 32},
				  {16, 16, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32, 32}
		  };
		  double [][][] dequantizedTable = new double[3][8][8];
		  
		  for(int k = 0; k < 3; k++){
			  for(int i = 0; i < 8; i++)
				  for(int j = 0; j < 8; j++){
					 double quantizedVal;
					 if( k == 0 ){
						 Y_qTable[i][j] *= Math.pow(2, n);
						 dequantizedTable[0][i][j] = quantizedTable[0][i][j] * Y_qTable[i][j];
					 } else{
						 C_qTable[i][j] *= Math.pow(2, n);
						 dequantizedTable[k][i][j] = quantizedTable[k][i][j] * C_qTable[i][j];
					 }
					 //System.out.println(quantizedVal);
				  }
		  }
		 
		  return dequantizedTable;
	  }
	  
	  public double[][][] Quantization(double[][][] dctTable, int n){
		  double [][] Y_qTable = {
				  {4, 4, 4, 8, 8, 16, 16, 32},
				  {4, 4, 4, 8, 8, 16, 16, 32},
				  {4, 4, 8, 8, 16, 16, 32, 32},
				  {8, 8, 8, 16, 16, 32, 32, 32},
				  {8, 8, 16, 16, 32, 32, 32, 32},
				  {16, 16, 16, 32, 32, 32, 32, 32},
				  {16, 16, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32, 32}
		  };
		  double [][] C_qTable = {
				  {8, 8, 8, 16, 32, 32, 32, 32},
				  {8, 8, 8, 16, 32, 32, 32, 32},
				  {8, 8, 16, 32, 32, 32, 32, 32},
				  {16, 16, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32, 32}
		  };
		  double [][][] quantizedTable = new double[3][8][8];
		  
		  for(int k = 0; k < 3; k++){
			  for(int i = 0; i < 8; i++)
				  for(int j = 0; j < 8; j++){
					 double quantizedVal;
					 if( k == 0 ){
						 Y_qTable[i][j] *= Math.pow(2, n);
						 quantizedVal = Math.round(dctTable[0][i][j] / Y_qTable[i][j]);
					 } else{
						 C_qTable[i][j] *= Math.pow(2, n);
						 quantizedVal = Math.round(dctTable[k][i][j] / C_qTable[i][j]);
					 }
					 quantizedTable[k][i][j] = quantizedVal;
					 //System.out.println(quantizedVal);
				  }
		  }
		  
		  return quantizedTable;
		  
	  }
	  
	  public void subsample(){
		  int newH = newImg.getH() / 2, newW = newImg.getW() / 2;
		  newY = new double[newImg.getH()][newImg.getW()];
		  
		  // Copy YCbCr[][][0] to newY
		  for(int y = 0; y < newImg.getH(); y++){
			  for(int x = 0; x < newImg.getW(); x++){
				  newY[y][x] = YCbCr[y][x][0];
			  }
		  }
		  
		  // Get avg 2x2 blocks for Cb, Cr
		  
		  for(int y = 0; y < newImg.getH(); y+=8)
			  for(int x = 0; x < newImg.getW(); x+=8){
				  //System.out.println("Old         y = " + y + " x = " + x + " -> " + YCbCr[y][x][1]);
				  
				  for(int v = y; v < y + 8; v+=2)
					  for(int u = x; u < x + 8; u+=2){
						  double avgCbBlock = (YCbCr[v][u][1] + YCbCr[v][u+1][1] + YCbCr[v+1][u][1] + YCbCr[v+1][u+1][1]) / (float) 4;
						  double avgCrBlock = (YCbCr[v][u][2] + YCbCr[v][u+1][2] + YCbCr[v+1][u][2] + YCbCr[v+1][u+1][2]) / (float) 4;

						 YCbCr[v/2][u/2][1] = avgCbBlock;
						  YCbCr[v/2][u/2][2] = avgCrBlock;
					  }
				  //System.out.println("New         y = " + y + " x = " + x + " -> " + YCbCr[y][x][1]);
			  }
		  
		  // If new Cb, Cr size is not divisible by 8, pad with zeroes
		  
		  if(newImg.getH() / 2 % 8 != 0){
			  newH = (newImg.getH() / 2) + 4;
			  
		  }
		  if(newImg.getW() / 2 % 8 != 0){
			  newW = (newImg.getW() / 2) + 4;
		  }
		  
		  //newImg.setH(newH);
		  //newImg.setW(newW);
		  System.out.println("New Cb/Cr width: " + newW + "  vs " + newImg.getW() / 2);
		  System.out.println("New Cb/Cr height: " + newH + "  vs " + newImg.getH() / 2);
		  subsampleW = newW;
		  subsampleH = newH;
		  
		  
		  // Create newCb, newCr with the appropriate max indices
		  
		  newCb = new double[newH][newW];
		  newCr = new double[newH][newW];
		  
		  // Fill newCb, newCr using YCbCr
		  
		  if(newW != newImg.getW() / 2 || newH != newImg.getH() / 2){
			  for(int y = 0; y < newH; y++){
				  for(int x = 0; x < newW; x++){
					  if(x > newImg.getW() / 2 || y > newImg.getH() / 2){
						  newCb[y][x] = 0;
						  newCr[y][x] = 0;
					  } else{
						  newCb[y][x] = YCbCr[y][x][1];
						  newCr[y][x] = YCbCr[y][x][2];
					  }
					  //System.out.println("Subsampled Cb at y = " + y + " and x = " + x + ": " + newCb[y][x]);
				  }
			  }
		  }
	  }

	  public void supersample(){
		  for(int y = 0; y < getH(); y+=2){
			  for(int x = 0; x < getW(); x+=2){
				  
				  double avgCb = newCb[y/2][x/2];
				  YCbCr[y][x][1] = avgCb;
				  YCbCr[y+1][x][1] = avgCb;
				  YCbCr[y][x+1][1] = avgCb;
				  YCbCr[y+1][x+1][1] = avgCb;
				  
				  double avgCr = newCr[y/2][x/2];
				  YCbCr[y][x][2] = avgCr;
				  YCbCr[y+1][x][2] = avgCr;
				  YCbCr[y][x+1][2] = avgCr;
				  YCbCr[y+1][x+1][2] = avgCr;

				  //System.out.println("Supersampled Cb @ (" + y + ", " + x + ")" + YCbCr[y][x][1]);
						  
			  }
		  }
	  }
	  
	  public void resize(){
		  int oldX = 0, newX = 0, oldY = 0, newY = 0;
		  
		  if(getH() % 8 != 0){
			  System.out.println(getH() % 8);
			  oldY = getH();
			  newY = getH() - (getH() % 8) + 8;
			  System.out.println(newY);
		  } else {
			  newY = oldY;
		  }

		  if(getW() % 8 != 0){
			  System.out.println(getW() % 8);
			  oldX = getW();
			  newX = getW() - (getW() % 8) + 8;
		  } else {
			  newX = oldX;
		  }
		  
		  if (oldX != newX || oldY != newY){
			  newImg = new Image(newX, newY);
			  for(int y = 0; y < newY; y++){
				  for(int x = 0; x < newX; x++){
					  
					  int rgb[] = new int[3];
					  
					  if(x < oldX && y < oldY){
						  getPixel(x, y, rgb);
						  newImg.setPixel(x,  y,  rgb);
					  } else{
						  for(int i = 0; i < 3; i++)
							  rgb[i] = 0;
						  
						  newImg.setPixel(x,  y, rgb);
					  }
				  }
			  }
		  } else{
			  newImg = this;
		  }
		  
		  newImg.display(getFileName() + "_Resized");
		  newImg.write2PPM(getFileName()  + "_Resized.PPM");
		  System.out.println(newImg.getH() + " " + newImg.getW());
		  YCbCr = new double[newImg.getH()][newImg.getW()][3];
	  }
	  
	  public void resizeBack(){
		  oldImg = new Image(getW(), getH());
		  for(int y = 0; y < getH(); y++){
			  for(int x = 0; x < getW(); x++){
				  int rgb[] = new int[3];
				  
				  newImg.getPixel(x, y, rgb);
				  oldImg.setPixel(x, y, rgb);
			  }
		  }
		  oldImg.display(getFileName() + "_ResizedBack");
		  oldImg.write2PPM("getFileName()"+"_ResizedBack" + ".PPM");
	  }
	  
	  public void calcDCT (double input[][], double output[][]){
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
	  
	  public void decalcDCT (double input[][], double output[][]){
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
	  
	  public double [][][] CStransform() { 		  // Color Space Transformation
		  int rgb[] = new int[3];
		  
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
			  }
		  return YCbCr;
	  }
	
	  
	  public void CSdetransform(){ 		  // Inverse Color Space Transformation
		  int rgb[] = new int[3];
		  
		  for(int y = 0; y < getH(); y++)
			  for(int x = 0; x < getW(); x++){
				  YCbCr[y][x][0] += 128;
				  YCbCr[y][x][1] += 0.5;
				  YCbCr[y][x][2] += 0.5;
				  
				  // Get Y, Cb, and Cr
				  rgb[0] = (int) Math.round( (1.00 * YCbCr[y][x][0]) + (0) + (1.4020 * YCbCr[y][x][2]) );
				  rgb[1] = (int) Math.round( (1.00 * YCbCr[y][x][0]) + (-0.3441 * YCbCr[y][x][1]) + (-0.7141 * YCbCr[y][x][2]) );
				  rgb[2] = (int) Math.round( (1.00 * YCbCr[y][x][0]) + (1.7720 * YCbCr[y][x][1]) + (0) );
				  
				  // Truncate if necessary
				  for(int i = 0; i < 3; i++){
					  if(rgb[i] > 255)
						  rgb[i] = 255;
					  else if (rgb[i] < 0)
						  rgb[i] = 0;
				  }
				  setPixel(x, y, rgb);
			  }
		  
		  display("pepe");
		  write2PPM("detransformers.PPM");
	  }
	  
	  public void pretty8x8display (double[][] table){
		  System.out.println("\nLet's pretty up that table for ya.\n");
		  for(int i = 0; i < 8; i++){
			  for(int j = 0; j < 8; j++){
				  System.out.print((int)table[i][j] + "\t");
			  }
			  System.out.println();
		  }
	  }
}

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DCTCompress {
		int origH, resizedH, origW, resizedW, ss_CbCrW = 0, ss_CbCrH = 0, n = 0, sum = 0, Ysum = 0, Cbsum = 0, Crsum = 0;

	  public DCTCompress(String fileName, int n){
		  Image img = new Image (fileName);
		  this.n = n;

		  // Encoding Steps
		  Image resizedImg = resize(img);
		  resizedImg.display("After resize");
		  resizedImg.write2PPM("(Resized) " + fileName);
		  double [][][] YCbCr = CStransform(resizedImg);
		  double [][][] subsampled_YCbCr = subsample(YCbCr);
		  double [][][] postDCT = DCT(subsampled_YCbCr);
		  double [][][] quantizedDCT = Quantization(postDCT);
		  
		  CompressRatio(quantizedDCT);
		  
		  // Decoding Steps
		  double [][][] dequantizedDCT = Dequantization(quantizedDCT);
		  double [][][] preDCT = inverseDCT(dequantizedDCT);
		  YCbCr = supersample(preDCT);
		  Image detransformImg = CSdetransform(YCbCr);
		  Image restoredImg = resizeBack(detransformImg);
		  restoredImg.display("Resized back");
		  restoredImg.write2PPM("(Restored) "+ fileName);
		  
		  // Output
		  System.out.println("\nFor quantization level n = " + this.n);
		  System.out.println("Original image cost is " + origW * origH * 24 + " bits");
		  System.out.println("The  Y values cost is " + Ysum + " bits");
		  System.out.println("The  Cb values cost is " + Cbsum + " bits");
		  System.out.println("The  Cr values cost is " + Crsum + " bits");
		  sum = Ysum + Cbsum + Crsum;
		  System.out.println("Total compressed image cost : " + sum + " bits");
		  System.out.println("Compression Ratio  : " + (origW * origH * 24) / (double) (sum));
		  
	  }

	  public void print1stRow(double [][][] arr){
		  for(int i = 0; i < 8; i++){
			  System.out.println((int)arr[0][0][i]);
		  }
		  System.out.println();
	  }
	  
	  public void setN(int n){
		  this.n = n;
	  }
	  
	  public double [][][] inverseDCT(double [][][] postDCT){
		  double [][][] preDCT = new double[3][resizedH][resizedW];
		  
		  for(int y = 0; y < resizedH; y+=8){
			  for(int x = 0; x < resizedW; x+=8){ 
				  double [][] preDCT_Y = new double[8][8], postDCT_Y = new double[8][8], preDCT_Cb = new double[8][8], postDCT_Cb = new double[8][8], preDCT_Cr = new double[8][8], postDCT_Cr = new double[8][8];
				  
				  for(int j = 0, v = y; v < y + 8; v++, j++){
					  for(int i = 0, u = x; u < x + 8; u++, i++){
						  postDCT_Y[j][i] = postDCT[0][v][u];
					  }
				  }
				  decalcDCT(postDCT_Y, preDCT_Y);
					
				  if(y < ss_CbCrH && x < ss_CbCrW){
					  for(int j = 0, v = y; v < y + 8; v++, j++)
						  for(int i = 0, u = x; u < x + 8; u++, i++){
							  postDCT_Cb[j][i] = postDCT[1][v][u];
							  postDCT_Cr[j][i] = postDCT[2][v][u];
						  }
				  }
				  decalcDCT(postDCT_Cb, preDCT_Cb);	
				  decalcDCT(postDCT_Cr, preDCT_Cr);
				  
				  for(int j = 0, v = y; v < y + 8; v++, j++){
					  for(int i = 0, u = x; u < x + 8; u++, i++){
						  preDCT[0][v][u] = preDCT_Y[j][i]; // postDCT[y-axis][x-axis][0 = Y, 1 = Cb, 2 = Cr]
						  preDCT[1][v][u] = preDCT_Cb[j][i];
						  preDCT[2][v][u] = preDCT_Cr[j][i];
					  }
				  }
			  }
		  }
		  
		  return preDCT;
	  }
	  
	  public double [][][] DCT(double [][][] YCbCr){
		  //int totalBits = 0, totalY = 0, totalCb = 0, totalCr = 0;
		  double [][][] postDCT = new double[3][resizedH][resizedW];
		  
		  for(int y = 0; y < resizedH; y+=8){
			  for(int x = 0; x < resizedW; x+=8){ 
				  double [][] preDCT_Y = new double[8][8], postDCT_Y = new double[8][8], preDCT_Cb = new double[8][8], postDCT_Cb = new double[8][8], preDCT_Cr = new double[8][8], postDCT_Cr = new double[8][8];
				  
				  for(int j = 0, v = y; v < y + 8; v++, j++){
					  for(int i = 0, u = x; u < x + 8; u++, i++){
						  preDCT_Y[j][i] = YCbCr[0][v][u];
					  }
				  }
					calcDCT(preDCT_Y, postDCT_Y);
				  
				
					if(y < ss_CbCrH && x < ss_CbCrW){
						  for(int j = 0, v = y; v < y + 8; v++, j++)
							  for(int i = 0, u = x; u < x + 8; u++, i++){
								  //System.out.println("[" + v + ", " + u + "] " + "CbCr -> j = " + j + ", i = " + i);
								  //System.out.println("x/2 : " + x/2);
								  preDCT_Cb[j][i] = YCbCr[1][v][u];
								  preDCT_Cr[j][i] = YCbCr[2][v][u];
								  //System.out.println("For CbCr : (" + v + ", " + u + ")");
						  }
					}
					calcDCT(preDCT_Cb, postDCT_Cb);	
					calcDCT(preDCT_Cr, postDCT_Cr);

					/*
					double inverseDCT_Y [][] = new double[8][8], inverseDCT_Cb [][] = new double[8][8], inverseDCT_Cr [][] = new double[8][8];
					decalcDCT(postDCT_Y, inverseDCT_Y);
					decalcDCT(postDCT_Y, inverseDCT_Cb);
					decalcDCT(postDCT_Y, inverseDCT_Cr);
					
					if(y == 0 && x == 0){
						;//pretty8x8display(preDCT_Y);
						//pretty8x8display(inverseDCT_Y);
						//pretty8x8display(postDCT_Y);
					}
					*/

					  
					  for(int j = 0, v = y; v < y + 8; v++, j++){
						  for(int i = 0, u = x; u < x + 8; u++, i++){
							  postDCT[0][v][u] = postDCT_Y[j][i]; // postDCT[y-axis][x-axis][0 = Y, 1 = Cb, 2 = Cr]
							  postDCT[1][v][u] = postDCT_Cb[j][i];
							  postDCT[2][v][u] = postDCT_Cr[j][i];
						  }
					  }
							

					/*
					double [][][] DCT = new double[3][8][8];
					DCT[0] = postDCT_Y;
					DCT[1] = postDCT_Cb;
					DCT[2] = postDCT_Cr;

					double [][][] quantizedDCT8x8 = Quantization(DCT, this.n);
					//double [][][] restoredDCT = Dequantization(quantizedDCT, this.n);

					
					
					for(int k = 0; k < 3; k++){
						switch(k){
						case 0 : totalY += CompressRatio(k, quantizedDCT8x8, this.n);
							break;
						case 1 : totalCb += CompressRatio(k, quantizedDCT8x8, this.n);
							break;
						case 2 : totalCr += CompressRatio(k, quantizedDCT8x8, this.n);
							break;
						}
						
						//System.out.println("(" + y + ", " + x + ") @ k = " + k + " --> " + CompressRatio(k, quantizedDCT, this.n) );
						//totalBits += CompressRatio(k, quantizedDCT, n);
					}
					*/
			  }
		  }
		  return postDCT;
	  }
	  
	  public double[][][] Quantization(double [][][] DCT){
		  double [][][] DCT8x8 = new double [3][8][8], quantizedDCT = new double [3][resizedH][resizedW];
		  for(int y = 0; y < resizedH; y+=8){
			  for(int x = 0; x < resizedW; x+=8){ 
				  
				  for(int k = 0; k < 3; k++)
					  for(int j = 0, v = y; v < y + 8; j++, v++)
						  for(int i = 0, u = x; u < x + 8; i++, u++){
							  DCT8x8[k][j][i] = DCT[k][v][u];
						  }

				  double [][][] quantizedDCT8x8 = quantize8x8(DCT8x8, this.n);
				  
				  for(int k = 0; k < 3; k++)
					  for(int j = 0, v = y; v < y + 8; j++, v++)
						  for(int i = 0, u = x; u < x + 8; i++, u++){
							  quantizedDCT[k][v][u] = quantizedDCT8x8[k][j][i];
						  }
			  }
		  }

		  return quantizedDCT;
	  }
	  
	  public double[][][] Dequantization(double [][][] quantizedDCT){
		  double [][][] DCT8x8 = new double [3][8][8], restoredDCT = new double [3][resizedH][resizedW];
		  for(int y = 0; y < resizedH; y+=8){
			  for(int x = 0; x < resizedW; x+=8){ 
				  
				  for(int k = 0; k < 3; k++)
					  for(int j = 0, v = y; v < y + 8; j++, v++)
						  for(int i = 0, u = x; u < x + 8; i++, u++){
							  DCT8x8[k][j][i] = quantizedDCT[k][v][u];
						  }

				  double [][][] restoredDCT8x8 = dequantize8x8(DCT8x8, this.n);
				  
				  for(int k = 0; k < 3; k++)
					  for(int j = 0, v = y; v < y + 8; j++, v++)
						  for(int i = 0, u = x; u < x + 8; i++, u++){
							  restoredDCT[k][v][u] = restoredDCT8x8[k][j][i];
						  }
			  }
		  }

		  return restoredDCT;
	  }
	  
	  public void CompressRatio(double [][][] quantizedTable){
		  double [][][] quantized8x8 = new double [3][8][8];
		  
		  for(int y = 0; y < resizedH; y+=8){
			  for(int x = 0; x < resizedW; x+=8){ 
				  for(int k = 0; k < 3; k++){
					  for(int j = 0, v = y; v < y + 8; j++, v++)
						  for(int i = 0, u = x; u < x + 8; i++, u++){
							  quantized8x8[k][j][i] = quantizedTable[k][v][u];
						  }
					  
					  switch(k){
						  case 0 : Ysum += Zigzag8x8(k, quantized8x8);
						  	break;
						  case 1 : Cbsum += Zigzag8x8(k, quantized8x8);
						  	break;
						  case 2 : Crsum += Zigzag8x8(k, quantized8x8);
						  	break;
					  }
					  
				  }

			  }
		  }
	  }
	  
	  public int Zigzag8x8(int k, double [][][] quantized8x8){
		  
		  List<Map.Entry<Integer ,Integer>> pairList= new ArrayList<>();

		 // pretty8x8display(quantizedTable[k]);
		  // AC coefficients
		  		int totalBits = 0, y = 0, x = 1, c = 0, valCount = 0;
		  		
		  		double val = -9999; // only for init
		  		
			  		while(y >= 0 && y < 8){
						  while(x >= 0 && x < 8 && y >= 0 && y < 8){
							  if (val == -9999) {
								  val = quantized8x8[k][y][x];
								  valCount = 1;
							  } 
							  else if( quantized8x8[k][y][x] != val ){
								  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
								  valCount = 1;
								  val = quantized8x8[k][y][x];
							  } else
								  valCount++;
							  
							  //System.out.print(val + "    ");
							  y++; 
							  x--;
						  }
						  if(x == 5 && y == 8){
							  
							  if( quantized8x8[k][7][7] != val ){
								  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
								  valCount = 1;
								  val = quantized8x8[k][7][7];
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
							  if( quantized8x8[k][y][x] != val ){
								  
								  pairList.add(new AbstractMap.SimpleEntry<>((int) val, valCount));
								  valCount = 1;
								  val = quantized8x8[k][y][x];
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
			  		

			  		int DCbits = 10, ACbits = 10 - this.n, RLbits = 6;
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
	  
	  public double[][][] dequantize8x8(double[][][] quantizedTable, int n){
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
	  
	  public double[][][] quantize8x8(double[][][] dctTable, int n){
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
	  
	  public double[][][] subsample(double [][][] YCbCr){
		  double [][] newY = new double[resizedH][resizedW];
		  
		  // Copy YCbCr[][][0] to newY
		  for(int y = 0; y < resizedH; y++){
			  for(int x = 0; x < resizedW; x++){
				  newY[y][x] = YCbCr[0][y][x];
			  }
		  }
		  
		  // Get avg 2x2 blocks for Cb, Cr
		  
		  for(int y = 0; y < resizedH; y+=8)
			  for(int x = 0; x < resizedW; x+=8){
				  for(int v = y; v < y + 8; v+=2)
					  for(int u = x; u < x + 8; u+=2){
						  double avgCbBlock = (YCbCr[1][v][u] + YCbCr[1][v][u+1] + YCbCr[1][v+1][u] + YCbCr[1][v+1][u+1]) / (float) 4;
						  double avgCrBlock = (YCbCr[2][v][u] + YCbCr[2][v][u+1] + YCbCr[2][v+1][u] + YCbCr[2][v+1][u+1]) / (float) 4;

						 YCbCr[1][v/2][u/2] = avgCbBlock;
						  YCbCr[2][v/2][u/2] = avgCrBlock;
					  }
			  }
		  
		  // If new Cb, Cr size is not divisible by 8, pad with zeroes
		  int CbCrH = resizedH / 2, CbCrW = resizedW / 2;
		  
		  if(CbCrH % 8 != 0){
			  CbCrH = (CbCrH) + 4;
		  }
		  if(CbCrW % 8 != 0){
			  CbCrW = (CbCrW) + 4;
		  }
		  
		  ss_CbCrW = CbCrW;
		  ss_CbCrH = CbCrH;
		  
		  
		  // Create newCb, newCr with the appropriate max indices
		  
		  double [][] newCb = new double[CbCrH][CbCrW];
		  double [][] newCr = new double[CbCrH][CbCrW];
		  
		  // Fill newCb, newCr using YCbCr
		  
		  if ( (CbCrW != resizedW / 2 || CbCrH != resizedH / 2) ){
			  for(int y = 0; y < CbCrH; y++){
				  for(int x = 0; x < CbCrW; x++){
					  if(x > resizedW / 2 || y > resizedH / 2){
						  newCb[y][x] = 0;
						  newCr[y][x] = 0;
					  } else{
						  newCb[y][x] = YCbCr[1][y][x];
						  newCr[y][x] = YCbCr[2][y][x];
					  }
				  }
			  }
		  } else {
			  for(int y = 0; y < CbCrH; y++){
				  for(int x = 0; x < CbCrW; x++){
					  newCb[y][x] = YCbCr[1][y][x];
					  newCr[y][x] = YCbCr[2][y][x];
				  }
			  }
		  }
		  
		  double [][][] newYCbCr = new double [3][resizedH][resizedW];
		  newYCbCr[0] = newY;
		  newYCbCr[1] = newCb;
		  newYCbCr[2] = newCr;
		  return newYCbCr;
	  }

	  public double [][][] supersample(double[][][] YCbCr){
		  double [][][] newYCbCr = new double[3][resizedH][resizedW];

		  for(int y = 0; y < resizedH; y++){
			  for(int x = 0; x < resizedW; x++){
				  double avgCb = YCbCr[1][y/2][x/2];
				  double avgCr = YCbCr[2][y/2][x/2];

				  newYCbCr[0][y][x] = YCbCr[0][y][x];
				  newYCbCr[1][y][x] = avgCb;
				  newYCbCr[2][y][x] = avgCr; 
			  }
		  }
		  return newYCbCr;
	  }
	  
	  public Image resize(Image img){
		  
		  origH = img.getH();
		  if(img.getH() % 8 != 0){
			  resizedH = img.getH() - (img.getH() % 8) + 8;
		  } else {
			  resizedH = origH;
		  }

		  origW = img.getW();
		  if(img.getW() % 8 != 0){
			  resizedW = img.getW() - (img.getW() % 8) + 8;
		  } else {
			  resizedW = origW;
		  }
		  
		  if (resizedW != origW || resizedH != origH){
			  Image newImg = new Image(resizedW, resizedH);
			  for(int y = 0; y < resizedH; y++){
				  for(int x = 0; x < resizedW; x++){
					  
					  int rgb[] = new int[3];
					  
					  if(x < origW && y < origH){
						  img.getPixel(x, y, rgb);
						  newImg.setPixel(x,  y,  rgb);
					  } else{
						  for(int i = 0; i < 3; i++)
							  rgb[i] = 0;
						  
						  newImg.setPixel(x,  y, rgb);
					  }
				  }
			  }
			  img = newImg;
		  }
		  return img;
	  }
	  
	  public Image resizeBack(Image img){
		  Image newImg = new Image(origW, origH);
		  for(int y = 0; y < origH; y++){
			  for(int x = 0; x < origW; x++){
				  int rgb[] = new int[3];
				  
				  img.getPixel(x, y, rgb);
				  newImg.setPixel(x, y, rgb);
			  }
		  }
		  return newImg;
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
						  
						 output[x][y] += (1 / (float) 4) * C_u * C_v * (input[u][v] * Math.cos( ((2*x + 1) * u * Math.PI)  / 16 ) * Math.cos( ((2 * y + 1) * v * Math.PI)  / 16) );
					  }
				  }

			  }
		  }

	  }
	  
	  public double[][][] CStransform(Image img) { 		  // Color Space Transformation
		  int rgb[] = new int[3];
		  double[][][] YCbCr = new double[3][resizedH][resizedW];
		  
		  for(int y = 0; y < resizedH; y++)
			  for(int x = 0; x < resizedW; x++){
				  img.getPixel(x, y, rgb);
				  
				  // Get Y, Cb, and Cr
				  YCbCr[0][y][x] = (rgb[0] * 0.2990) + (rgb[1] * 0.5870) + (rgb[2] * 0.1140); // Y
				  YCbCr[1][y][x] = (rgb[0] * -0.1687) + (rgb[1] * -0.3313) + (rgb[2] * 0.5); // Cb
				  YCbCr[2][y][x] = (rgb[0] * 0.5) + (rgb[1] * -0.4187) + (rgb[2] * -0.0813); // Cr
				  
				  // Truncate if necessary
				  for(int i = 1; i < 3; i++){
					  if(YCbCr[i][y][x] < -127.5 ){
						  YCbCr[i][y][x] = -127.5;
					  } else if (YCbCr[i][y][x] > 127.5){
						  YCbCr[i][y][x] = 127.5;
					  }
				  }
				  
				  //Subtract 128 from Y, 0.5 from Cb and Cr
				  YCbCr[0][y][x] -= 128;
				  YCbCr[1][y][x] -= 0.5;
				  YCbCr[2][y][x] -= 0.5;
			  }
		  	return YCbCr;
	  }
	
	  
	  public Image CSdetransform(double[][][] YCbCr){ 		  // Inverse Color Space Transformation
		  int rgb[] = new int[3];
		  Image img = new Image(resizedW, resizedH);
		  
		  for(int y = 0; y < resizedH; y++)
			  for(int x = 0; x < resizedW; x++){
				  YCbCr[0][y][x] += 128;
				  YCbCr[1][y][x] += 0.5;
				  YCbCr[2][y][x] += 0.5;
				  
				  // Get Y, Cb, and Cr
				  rgb[0] = (int) Math.round( (1.00 * YCbCr[0][y][x]) + (0) + (1.4020 * YCbCr[2][y][x]) );
				  rgb[1] = (int) Math.round( (1.00 * YCbCr[0][y][x]) + (-0.3441 * YCbCr[1][y][x]) + (-0.7141 * YCbCr[2][y][x]) );
				  rgb[2] = (int) Math.round( (1.00 * YCbCr[0][y][x]) + (1.7720 * YCbCr[1][y][x]) + (0) );
				  
				  // Truncate if necessary
				  for(int i = 0; i < 3; i++){
					  if(rgb[i] > 255)
						  rgb[i] = 255;
					  else if (rgb[i] < 0)
						  rgb[i] = 0;
				  }
				  img.setPixel(x, y, rgb);
			  }
		  return img;
	  }
	  
	  public void pretty8x8display (double[][] table){
		  System.out.println("\nPretty Table (values rounded to int) \n");
		  for(int i = 0; i < 8; i++){
			  for(int j = 0; j < 8; j++){
				  System.out.print((int)table[i][j] + "\t");
			  }
			  System.out.println();
		  }
	  }
}

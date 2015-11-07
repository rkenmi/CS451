
public class DCTCompress extends Image {
		Image oldImg, newImg;
		double [][][] YCbCr; // original YCbCr after transformation from RGB
		double [][] newY;
		double [][] newCb; // subsampled Cb
		double [][] newCr; // subsampled Cr
		double [][][] DCT;
		double [][][] quantizedDCT;

	  public DCTCompress(String fileName){
		  super(fileName);
		  resize();
		  //resizeBack();
		  CStransform();
		  //printCb10();
		  subsample();
		  	//supersample();
		  	//CSdetransform();
		  DCT();
		  
		  printCb10();
		  
		  //display("yoloSwag");
	  }
	
	  public double[][][] Quantization(double[][][] dctTable){
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
				  {16, 16, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32},
				  {32, 32, 32, 32, 32, 32, 32}
		  };
		  double [][][] quantizedTable = new double[3][8][8];
		  
		  for(int k = 0; k < 3; k++){
			  for(int i = 0; i < 8; i++)
				  for(int j = 0; j < 8; j++){
					  double quantizedVal;
					 if( k == 0 ){
						 quantizedVal = Math.round(dctTable[0][i][j] / Y_qTable[i][j]);
					 } else{
						 quantizedVal = Math.round(dctTable[k][i][j] / C_qTable[i][j]);
					 }
					 quantizedTable[k][i][j] = quantizedVal;
					 
				  }
		  }
		  
		  return quantizedTable;
		  
	  }
	  
	  public void DCT(){
		  
		  for(int y = 0; y < newImg.getH(); y+=8){
			  for(int x = 0; x < newImg.getH(); x+=8){
				  double [][] inY = new double[8][8], outY = new double[8][8], inCb = new double[8][8], outCb = new double[8][8], inCr = new double[8][8], outCr = new double[8][8];
				  
				  for(int j = 0, v = y; v < y + 8; v++, j++)
					  for(int i = 0, u = x; u < x + 8; u++, i++)
						  inY[j][i] = newY[v][u];
					calcDCT(inY, outY);

				  for(int j = 0, v = y/2; v < y/2 + 8; v++, j++)
					  for(int i = 0, u = x/2; u < x/2 + 8; u++, i++){
						  inCb[j][i] = newCb[v][u];
						  inCr[j][i] = newCr[v][u];
					  }
				  
					calcDCT(inCb, outCb);	
					calcDCT(inCr, outCr);	
					
					DCT = new double[3][8][8];
					DCT[0] = outY;
					DCT[1] = outCb;
					DCT[2] = outCr;
					
					quantizedDCT = Quantization(DCT);
					
					
					for(int i = 0; i < 8; i++)
						for(int j = 0; j < 8; j++)
							System.out.println(outY[i][j]);
			  }
		  }
	  }
	  
	  public void inverseDCT(){
		  for(int y = 0; y < newImg.getH(); y+=8){
			  for(int x = 0; x < newImg.getH(); x+=8){
				  double [][] inY = new double[8][8], outY = new double[8][8], inCb = new double[8][8], outCb = new double[8][8], inCr = new double[8][8], outCr = new double[8][8];
				  
				  for(int j = 0, v = y; v < y + 8; v++, j++)
					  for(int i = 0, u = x; u < x + 8; u++, i++)
						  inY[j][i] = newY[v][u];
					calcDCT(inY, outY);

				  for(int j = 0, v = y/2; v < y/2 + 8; v++, j++)
					  for(int i = 0, u = x/2; u < x/2 + 8; u++, i++)
						  inCb[j][i] = newCb[v][u];
					calcDCT(inCb, outCb);	
					
				  for(int j = 0, v = y/2; v < y/2 + 8; v++, j++)
					  for(int i = 0, u = x/2; u < x/2 + 8; u++, i++)
						  inCr[j][i] = newCr[v][u];
					calcDCT(inCr, outCr);	
					
					
					for(int i = 0; i < 8; i++)
						for(int j = 0; j < 8; j++)
							System.out.println(outY[i][j]);
			  }
		  }
	  }
	  
	  public void printCb10(){ // debugging function
		  for(int y = getH() - 10; y < getH() - 9; y++)
			  for(int x = getW() - 10; x < getW(); x++){
				  System.out.println("At (" + y + ", " + x + ") : " + YCbCr[y][x][1]);
			  }
		  System.out.println("End.");
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
		  
		  newImg.setH(newH);
		  newImg.setW(newW);
		  //System.out.println("New Cb/Cr width: " + newW);
		  //System.out.println("New Cb/Cr height: " + newH);
		  
		  // Create newCb, newCr with the appropriate max indices
		  
		  newCb = new double[newH][newW];
		  newCr = new double[newH][newW];
		  
		  // Fill newCb, newCr using YCbCr
		  
		  if(newW != newImg.getW() || newH != newImg.getH()){
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
			  
			  newImg.display("YoloSwag420");
			  newImg.write2PPM("yolo.PPM");
			  YCbCr = new double[newImg.getH()][newImg.getW()][3];
			  
		  }
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
		  oldImg.display("YoloSwag420");
		  oldImg.write2PPM("yolo-old.PPM");
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
}

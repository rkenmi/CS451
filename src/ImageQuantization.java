
public class ImageQuantization extends Image {
	  private int[][] LUT;					// look up table
	
	  public ImageQuantization(int choice, String fileName){
		  super(choice, fileName);
	  }
	  public ImageQuantization(String fileName){
		  super(fileName);
	  }
	  
	  public int Grayscale(){
		  int rgb[] = new int[3];
		  int grayAvg = 0; // average gray-scale value of all input pixels
		  int gray = -1;
		  
		  for(int x = 0; x < getW(); x++){
			  for(int y = 0; y < getH(); y++){
				  getPixel(x, y, rgb);
				  gray = (int) (Math.round(0.299 * rgb[0]) + (0.587 * rgb[1]) + (0.114 * rgb[2]));
				  if (gray < 0 || gray > 255){
					  System.out.println("error!");
					  System.exit(1);
				  }

				  rgb[0] = gray;
				  rgb[1] = gray;
				  rgb[2] = gray;
				  grayAvg += gray;
				  
				  setPixel(x, y, rgb);
			  }
		  }
		  write2PPM(getFileName() + "_8bit_Grayscale.ppm");
		  
		  return Math.round(grayAvg / (getW() * getH()) );
	  }
	  
	  public void Threshold(){
		  int rgb[] = new int[3];
		  int grayAvg = Grayscale();
		  //System.out.println(grayAvg);
		  int newColor = -1;
		  
		  for (int x = 0; x < getW(); x++){
			  for (int y = 0; y < getH(); y++){
				  getPixel(x, y, rgb);
				  
				  if(rgb[0] > grayAvg) // can be compared to rgb[1] or rgb[2] as well, doesn't matter
					  newColor = 255;
				  else
					  newColor = 0;

					  
				  rgb[0] = newColor;
				  rgb[1] = newColor;
				  rgb[2] = newColor;
					  
				  setPixel(x, y, rgb);
			  }
		  }
		  write2PPM(getFileName() + "_Bi-level_Threshold.ppm");
	  }

	  public void ErrorDiffusion(float[][] pixelTable, int x, int y, float qError){
		  float right = ((float)7/16), bottomLeft = ((float)3/16), bottom = ((float)5/16), bottomRight = ((float)1/16);
		  
		  if (x > 0 && y + 1 < getH())
			  pixelTable[y+1][x-1] += (qError * (bottomLeft));

		  if (y + 1 < getH())
			  pixelTable[y+1][x] += (qError * (bottom));
		  
		  if (x + 1 < getW())
			  pixelTable[y][x + 1] += (qError * (right));
		  
		  if (x + 1 < getW() && y + 1 < getH())
			  pixelTable[y + 1][x + 1] += (qError * (bottomRight));
		  
	  }
	  public void N_Level(int n){
		  float pixelTable[][] = new float[getH()][getW()];
		  int rgb[] = new int[3];
		  this.Grayscale();
		  float q = -1, qError = -1;
		 
		  // quantize grayscale values depending on N
		  float n_section[] = new float[n];
		  for(int i = 0; i < n_section.length; i++){
			  n_section[i] = ((float)255 / (n-1))  * i;
			  //System.out.println(n_section[i]);
		  }
		  
		  //init pixelTable
		  for(int y = 0; y < getH(); y++){
			  for(int x = 0; x < getW(); x++){
				  getPixel(x, y, rgb);
				  pixelTable[y][x] = (float)rgb[0];
			  }
		  }

		  float diff = 0, min;
		  for(int y = 0; y < getH(); y++){
			  for(int x = 0; x < getW(); x++){
				  min = Float.MAX_VALUE;
				  for(int i = 0; i < n_section.length; i++){
					  diff = Math.abs(pixelTable[y][x] - n_section[i]);
					  if (diff < min){
						  min = diff;
						  q = n_section[i];
					  }
				  }

				  qError = pixelTable[y][x] - q; // can be negative!
				  
				  for(int i = 0; i < 3; i++)
					  rgb[i] = Math.round(q);
				 
				  ErrorDiffusion(pixelTable, x, y, qError);
				  setPixel(x,  y, rgb);
			  }
		  }
		  write2PPM(getFileName() + "_n=" + n + "_" + "ErrorDiffusion.ppm");
	  }
	  public void MedianCut(){
		  
	  }
	  
	  
	  public void UCQuant(){
		  int rgb[] = new int[3];
		  int matchIndex = 0;
		  int i = 0;
		  
		  buildLUT();
		  displayLUT();
		  
		  Image indexImg = new Image(getW(), getH());
		  
		  for(int x = 0; x < getW(); x++){
			  for(int y = 0; y < getH(); y++){
				  getPixel(x, y, rgb);
				  
				  i = 0;
				  while(i < 256){
					  if ( (rgb[0] < LUT[0][i] + 16 && rgb[0] >= LUT[0][i] - 16)
							  && (rgb[1] < LUT[1][i] + 16 && rgb[1] >= LUT[1][i] - 16)
							  && (rgb[2] < LUT[2][i] + 32 && rgb[2] >= LUT[2][i] - 32) ){
						  matchIndex = i;
						  break;
					  }
					  i++;
				  }
				  
				  for(int j = 0; j < 3; j ++)
					  rgb[j] = matchIndex;
				  
				  indexImg.setPixel(x, y, rgb);
			  }
		  }
		  indexImg.write2PPM(getFileName() + "-index.ppm");
		  
		  for(int x = 0; x < getW(); x++){
			  for(int y = 0; y < getH(); y++){
				  indexImg.getPixel(x,  y, rgb);
				  matchIndex = rgb[0];
				  for(int j = 0; j < 3; j ++)
					  rgb[j] = LUT[j][matchIndex]; 
				  
				  this.setPixel(x, y, rgb);
			  }
		  }
		  
		  this.write2PPM(getFileName() +"_8bit_UCQuant.ppm");
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
		  System.out.println("Index        R        G        B      ");
		  System.out.println("______________________________");
		  for(int i = 0; i < 256; i++){
			  System.out.print(i + "   ");
			  for(int c = 0; c < 3; c++){
				  System.out.print("        " + LUT[c][i]);
			  }
			  System.out.println();
		  }
	  }
	
	  public class ColorBox {
		  private int r_min, r_max, g_min, g_max, b_min, b_max;
		  
		  public ColorBox(){}
		  
		  public Integer getAxisSize(char color){
			  int colorRange = 0;
			  
			  switch(color){
				  case 'r': colorRange = r_max - r_min;
				  	break;
				  case 'g': colorRange = g_max - g_min;
				  	break;
				  case 'b': colorRange = b_max - b_min;
				  	break;
			  }
			  return colorRange;
		  }
		  
		  public char getLongestAxis(){
			  if ( (r_max - r_min) > (g_max - g_min) && (r_max - r_min) > (b_max - b_min) )
				  return 'r';
			  else if ( (g_max - g_min) > (r_max - r_min) && (g_max - g_min) > (b_max - b_min) )
				  return 'g';
			  else
				  return 'b';
		  }
		  
		  public Integer getAvgRed(){
			  return (r_max - r_min) / 2;
		  }
		  
		  public Integer getAvgGreen(){
			  return (g_max - g_min) / 2;
		  }
		  
		  public Integer getAvgBlue(){
			  return (b_max - b_min) / 2;
		  } 
	  }
}

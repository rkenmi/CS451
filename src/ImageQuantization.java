
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
		  write2PPM(getFileName() + "_8bitGrayscale.ppm");
		  
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

	  public void ErrorDiffusion(int x, int y, int qError){
		  int rgb[] = new int[3];
		  float right = ((float)7/16);
		  float bottomLeft = ((float)3/16);
		  float bottom = ((float)5/16);
		  float bottomRight = ((float)1/16);
		  
		  int numer = 0, denom = 16;
		  
		  if (x - 1 > 0 && y + 1 < getH()){
			  getPixel(x - 1, y + 1, rgb);
			  for(int i = 0; i < 3; i++)
				  rgb[i] += Math.round((qError * (bottomLeft)));
			  setPixel(x - 1, y + 1, rgb);
		  }
		  else {
			  right *= Math.pow(1 - bottomLeft, -1);
			  bottom *= Math.pow(1 - bottomLeft, -1);
			  bottomRight *= Math.pow(1 - bottomLeft, -1);
		  }
			  
		  
		  if (y + 1 < getH()){
			  getPixel(x, y + 1, rgb);
			  for(int i = 0; i < 3; i++)
				  rgb[i] += Math.round((qError * (bottom)));
			  setPixel(x, y + 1, rgb);
		  }
		  else{
			  right *= Math.pow(1 - bottom, -1);
			  bottomRight *= Math.pow(1 - bottom,  -1);
		  }
		  
		  if (x + 1 < getW()){
			  getPixel(x + 1, y, rgb);
			  for(int i = 0; i < 3; i++)
				  rgb[i] += Math.round((qError * (right)));
			 setPixel(x + 1, y, rgb);
		  }
		  else {
			  bottomRight *= Math.pow(1 - right, -1);
		  }
		  
		  if (x + 1 < getW() && y + 1 < getH()){
			  getPixel(x + 1, y + 1, rgb);
			  for(int i = 0; i < 3; i++)
				  rgb[i] += Math.round((qError * (bottomRight)));
			  setPixel(x + 1, y + 1, rgb);
		  }
	  }
	  public void N_Level(int n){
		  int A_rgb[] = new int[3], B_rgb[] = new int[3];
		  Grayscale();
		  Image imgB = new Image(getW(), getH());
		  int q = -1, qError = -1;
		  
		  for(int x = 0; x < getW(); x++){
			  for(int y = 0; y < getH(); y++){
				  getPixel(x, y, A_rgb);
				  
				  if(n == 2) // bi level
					  if(A_rgb[0] > 127) // can be compared to rgb[1] or rgb[2] as well, doesn't matter
						  q = 255;
					  else
						  q = 0;
				  else if (n == 4){
					  ;
				  }
				  
				  for(int i = 0; i < 3; i++)
					  B_rgb[i] = q;
				  
				  imgB.setPixel(x,  y, B_rgb);
				  qError = A_rgb[0] - B_rgb[0]; // can be negative! ex: if rgb[0] = 128, 128 - 255 = -127
				  
				  if (x > 10 && x < 40 && y == 200){
					  System.out.println("x : " + x + ", y : " + y + " \nQ Error : " + qError);
					  System.out.println("     This value = " + A_rgb[0]);
					  int jihad[] = new int[3];
					  getPixel(x+1, y, jihad);
					  System.out.println("            Next value = " + jihad[0]);
				  }

				  ErrorDiffusion(x, y, qError);
				  
				  if (x > 10 && x < 40 && y == 200){
					  int jihad[] = new int[3];
					  getPixel(x+1, y, jihad);
					  System.out.println("            Next value (updated) = " + jihad[0]);
				  }
			  }
		  }
		  imgB.write2PPM(getFileName() + "_Bi-level_ErrorDiffusion.ppm");
	  }
	  public void MedianCut(){
		  
	  }
	  
	  
	  public void UCQuant(){
		  int rgb[] = new int[3];
		  int matchIndex = 0;
		  int i = 0;
		  
		  buildLUT();
		  displayLUT();
		  
		  for(int x = 0; x < getW(); x++){
			  for(int y = 0; y < getH(); y++){
				  getPixel(x, y, rgb);
				  //displayPixelValue(x, y);
				  
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

				  rgb[0] = LUT[0][matchIndex];
				  rgb[1] = LUT[1][matchIndex];
				  rgb[2] = LUT[2][matchIndex];
				  
				  setPixel(x, y, rgb);
			  }
		  }
		  write2PPM(getFileName() +"_8bitUCQuant.ppm");
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

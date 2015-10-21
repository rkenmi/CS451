
public class Aliasing extends Image {
	private Integer m, n, k;
	
	  public Aliasing (Integer m, Integer n, Integer k){
		  super(512, 512);
		  this.m = m;
		  this.n = n;
		  this.k = k;
		  format_toWhite();
		  drawCircle(m, n);
		  /*
		  resize_NoFilter(k);
		  resize_AvgFilter(k);
		  resize_3x3Filter(k, 1);
		  resize_3x3Filter(k, 2);
		  */
	  }
	  
	  public void format_toWhite(){ // Format all pixels to white in image
		  int rgb[] = new int[3];
		  for(int x = 0; x < getW(); x++){
			  for(int y = 0; y < getH(); y++){
				  getPixel(x, y, rgb);
				  for(int i = 0; i < 3; i++)
					  rgb[i] = 255;  
				  setPixel(x, y, rgb);
			  }
		  }
	  }
	  
	  public void drawCircle(Integer thickness, Integer radius){
		  int rgb[] = new int[3];
		  Integer cX = getW() / 2, cY = getH() / 2;
		  Integer init_radius = radius;
		  int thickStep;
		  
		  while(cX + (radius) < getW()){
			  for(int deg = 0; deg < 360 * radius; deg++){
				  thickStep = thickness;
				  getPixel(cX + (int)(radius * Math.cos(deg)), cY + (int)(radius * Math.sin(deg)), rgb);

				  for(int i = 0; i < 3; i++)
					  rgb[i] = 0;
				  while(thickStep > 0 && cX + (radius + thickStep) < getW() ){
					  setPixel(cX + (int)((radius + thickStep) * Math.cos(deg)), cY + (int)((radius + thickStep) * Math.sin(deg)), rgb);
					  thickStep--;
				  }
			  }
		  	radius += init_radius;
		  }
		  write2PPM("circles_m"+m+"_n"+n+".PPM");
		  display("circles_m"+m+"_n"+n);
	  }
	  
	  public void resize_NoFilter(Integer k){
		  Image newImg = new Image(getW() / k, getH() / k);
		  int rgb[] = new int[3];
		  
		  for(int x = 0; x < getW(); x += k){
			  for(int y = 0; y < getH(); y += k){
				  getPixel(x, y, rgb);
				  newImg.setPixel(Math.round(x/(float)k), Math.round(y/(float)k), rgb);
			  }
		  }
		  newImg.write2PPM("circles_m"+m+"_n"+n+"_k"+k+"_"+"NoFilter.PPM");
		  newImg.display("circles_m"+m+"_n"+n+"_k"+k+"_"+"NoFilter");
	  }
	  
	  public void resize_AvgFilter(Integer k){
		  Image newImg = new Image(getW() / k, getH() / k);
		  int rgb[] = new int[3];
		  double avgRgb[];
		  
		  for(int y = 0; y < getH(); y += k){
			  for(int x = 0; x < getW(); x += k){
				  avgRgb = box_AvgFilter(x, y, k);
				  for(int i = 0; i < 3; i++)
					  rgb[i] = (int)avgRgb[i];
				  newImg.setPixel(x/k, y/k, rgb);
			  }
		  }
		  newImg.write2PPM("circles_m"+m+"_n"+n+"_k"+k+"_"+"AvgFilter.PPM");
		  newImg.display("circles_m"+m+"_n"+n+"_k"+k+"_"+"AvgFilter");
	  }
	  
	  public void resize_3x3Filter(Integer k, Integer choice){
		  Image newImg = new Image(getW() / k, getH() / k);
		  double pixelTable[][][] = new double [getH()/k][getW()/k][3];
		  double filter[] = new double[9];
		  
		  if (choice == 1){ // Filter 1
			  for(int i = 0; i < filter.length; i++)
				  filter[i] = 1 / (double) 9;
		  } else { // Filter 2
			  filter[0] = 1 / (double) 16;
			  filter[1] = 2 / (double) 16;
			  filter[2] = filter[0];
			  filter[3] = filter[1];
			  filter[4] = 4 / (double) 16;
			  filter[5] = filter[1];
			  filter[6] = filter[0];
			  filter[7] = filter[1];
			  filter[8] = filter[0];
		  }

		  for(int y = 0; y < getH(); y += k){
			  for(int x = 0; x < getW(); x += k){
				  pixelTable[x/k][y/k] = formula_3x3(x, y, filter);
			  }
		  }
		  
		  int rgb[] = new int[3];
		  for(int y = 0; y < getH()/k ; y++)
			  for(int x = 0; x < getW()/k; x++){
				  for(int i = 0; i < 3; i++)
					  rgb[i] = (int) pixelTable[x][y][i];
				  newImg.setPixel(x,  y, rgb);
			  }
		  newImg.write2PPM("circles_m"+m+"_n"+n+"_k"+k+"_"+"3x3Filter"+"_"+choice+".PPM");
		  newImg.display("circles_m"+m+"_n"+n+"_k"+k+"_"+"3x3Filter"+"_"+choice);

	  }
	  
	  public double[] formula_3x3(Integer x, Integer y, double filter[]){
		  double box[][] = new double[9][3];

		  box[0] = multiplyPixel(x-1, y-1, filter[0]);
		  box[1] = multiplyPixel(x, y-1, filter[1]);
		  box[2] = multiplyPixel(x+1, y-1, filter[2]);
		  box[3] = multiplyPixel(x-1, y, filter[3]);
		  box[4] = multiplyPixel(x, y, filter[4]); // P(x, y) * C
		  box[5] = multiplyPixel(x+1, y, filter[5]);
		  box[6] = multiplyPixel(x-1, y+1, filter[6]);
		  box[7] = multiplyPixel(x, y+1, filter[7]);
		  box[8] = multiplyPixel(x+1, y+1, filter[8]);
		  
		  double filteredPixel[] = new double[3];
		  
		  for(int i = 0; i < 9; i++){ // 3x3 = 9
			  if(box[i] != null){
				  for(int j =0 ; j < 3; j++)
					  filteredPixel[j] += box[i][j];
			  }
		  }
	
		  return filteredPixel;
	  }
	  
	  private double[] multiplyPixel(Integer x, Integer y, double constant){ // multiply a pixel value with a constant
		  int rgb[] = new int[3];
		  double newRgb[] = new double[3]; 
		  
		  if(x < 0 || x >= getW() || y < 0 || y >= getH())
			  return null;
		  
		  getPixel(x, y, rgb);
		  for(int i = 0; i < 3; i ++){
			  newRgb[i] = rgb[i] * constant;
		  }
		  
		  return newRgb;
	  }
	  
	  
	  public double[] box_AvgFilter(Integer x, Integer y, Integer k){
		  int rgb[] = new int[3];
		  double avgRgb[] = {0, 0, 0};
		  int xMax = x + k, yMax = y + k;
		  
		  while (y < yMax){
			  while(x < xMax){
				  getPixel(x, y, rgb);
				  for(int i = 0; i < 3; i++)
					  avgRgb[i] += rgb[i];
				  x++;
			  }
			  y++;
			  x-=k;
		  }

		  for(int i = 0; i < 3; i++)
			  avgRgb[i] /= (k*k) ;
		  
		  return avgRgb;
	  }
}

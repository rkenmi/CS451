
public class Aliasing extends Image {
	private Integer m, n, k;
	
	  public Aliasing (Integer m, Integer n, Integer k){
		  super(512, 512);
		  this.m = m;
		  this.n = n;
		  this.k = k;
		  whiteImage();
		  n = 50;
		  drawCircle(m, n);
		  this.write2PPM("testbaby.PPM");
	  }
	  
	  public void whiteImage(){
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
		  
		  while(cX + (radius) < getW()){
			  for(int deg = 0; deg < 360 * radius; deg++){
				  getPixel(cX + (int)(radius * Math.cos(deg)), cY + (int)(radius * Math.sin(deg)), rgb);
				  for(int i = 0; i < 3; i++)
					  rgb[i] = 0;
				  setPixel(cX + (int)(radius * Math.cos(deg)), cY + (int)(radius * Math.sin(deg)), rgb);
			  }
		  	radius += init_radius;
		  }
	  }
	  
	  public void resize_NoFilter(){
		  ;
	  }
	  
	  public void resize_AvgFilter(){
		  ;
	  }
}


public class Aliasing extends Image {
	private Integer m, n, k;
	
	  public Aliasing (Integer m, Integer n, Integer k){
		  super(512, 512);
		  this.m = m;
		  this.n = n;
		  this.k = k;
		  whiteImage();
		  drawCircle(m, n);
	  }
	  
	  public void whiteImage(){
		  int rgb[] = new int[3];
		  for(int x = 0; x < img.getWidth(); x++){
			  for(int y = 0; y < img.getHeight(); y++){
				  getPixel(x, y, rgb);
				  for(int i = 0; i < 3; i++)
					  rgb[i] = 255;  
				  setPixel(x, y, rgb);
			  }
		  }
	  }
	  
	  public void drawCircle(Integer thickness, Integer radius){
		  int rgb[] = new int[3];
		  Integer centerX = img.getWidth() / 2, centerY = img.getHeight() / 2;
		  for(int deg = 0; deg < 360; deg++){
			  getPixel(centerX + (int)(radius * Math.cos(deg)), centerY + (int)(radius * Math.sin(deg)), rgb);
			  for(int i = 0; i < 3; i++)
				  rgb[i] = 0;
			  setPixel(centerX + (int)(radius * Math.cos(deg)), centerY + (int)(radius * Math.sin(deg)), rgb);
		  }
		  ;
	  }
}

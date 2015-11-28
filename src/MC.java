import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MC {
	
	private List <MacroBlock> targetBlocks;
	private int n, p;
	
	class MacroBlock {
		public int n, residual, x, y, dx, dy; // positionX and positionY are based on x and y coordinate of Image
		private int errors[][], valuesGray[][], valuesRGB[][][];
		private Image src;
		
		public MacroBlock(int x, int y, int n, Image based) {
			src = based;
			
			this.n = n;
			this.x = x;
			this.y = y;
			this.dx = -99999;
			this.dy = -99999;
			this.valuesGray = new int[n][n];
			this.valuesRGB = new int[n][n][3];
			fill();
		}
		
		public void fill(){
			for(int v = 0, j = y; j < y + n; j++, v++){
				for(int u = 0, i = x; i < x + n; i++, u++){
					if(i < src.getW() && i > 0 && j < src.getH() && j > 0){
						int rgb [] = new int [3];
						
						src.getPixel(i, j, rgb);
						
						valuesRGB[u][v] = rgb;
						
						int gray = (int) Math.round(0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
						valuesGray[u][v] = gray;
				
					}
				}
			}
		}
		
		public void compose(Image empty){
			if (valuesRGB != null){
				for(int v = 0, j = y; j < y + n; j++, v++){
					for(int u  = 0, i = x; i < x + n; i++, u++){
						if(i < empty.getW() && i > 0 && j < empty.getH() && j > 0){
							int rgb [] = valuesRGB[u][v];
							
							empty.setPixel(i, j, rgb);
						}
					}
				}
			}
		}
		
		public void composeError(Image err){
			if(errors != null){
				for(int v = 0, j = y; j < y + n; j++, v++){
					for(int u = 0, i = x; i < x + n; i++, u++){
						if(i < err.getW() && i > 0 && j < err.getH() && j > 0){
							int rgb [] = new int [3];
							
							if(errors[u][v] > 255)
								errors[u][v] = 255;
							else if (errors[u][v] < 0)
								errors[u][v] = 0;
							
							for(int m = 0; m < 3; m++){
								rgb[m] = errors[u][v];
							}
							
							err.setPixel(i,  j, rgb);
						}
					}
				}
			}
		}
		
		public double stats(){
			double errorSum = 0, distance = 0;
			
			if(this.errors != null && this.dx != -99999 && this.dy != -99999){
				distance = this.dx * this.dx + this.dy * this.dy;
				distance = Math.sqrt(distance);
				for(int i = 0; i < this.n; i++){
					for(int j = 0; j < this.n; j++){
						errorSum += (this.errors[i][j] / (double)256);
					}
				}
				//System.out.println(errorSum + "\t" + distance + "\t = " + errorSum * distance);
			}

			return (errorSum * distance);	
		}
		
	}
	
	public MC(int n, int p, Image ref, Image tar) {
		Image errorImg = new Image(ref.getW(), ref.getH());
		this.n = n;
		this.p = p;
		
		targetBlocks = new ArrayList<MacroBlock>();
		makeMacroBlocks(tar, n, p, targetBlocks);
		
		for(int i = 0; i < targetBlocks.size(); i++){
			fullSearch( targetBlocks.get(i), n, p, ref );	
			targetBlocks.get(i).composeError(errorImg);
		}
	}
	
	public int similarity(){
		int sum = 0;
		
		if(targetBlocks == null)
			System.err.println("Something is wrong; targetBlocks is not filled. (Is the constructor called properly?)");
		else{
			for(int i = 0; i < targetBlocks.size(); i++){
				sum += targetBlocks.get(i).stats();
			}
			//System.out.println("Error sum : " + sum);	
		}
		return sum;
	}
	
	public void rmMovingObj(Image fifth){
		int dynM = -1, staM = -1; // matching index pairs (closest corresponding static block to dynamic block)
		int width = fifth.getW(), height = fifth.getH();
		
		// init 5th frame macro block
		List<MacroBlock> fifthBlocks = new ArrayList<MacroBlock>();
		makeMacroBlocks(fifth, n, p, fifthBlocks);
		
		// Dynamic = dyn
		for(int dyn = 0; dyn < targetBlocks.size(); dyn++){
			if (targetBlocks.get(dyn).dx != 0 || targetBlocks.get(dyn).dy != 0){
				double minDist = 999999;
				
				// Static = sta
				for(int sta = 0; sta < targetBlocks.size(); sta++){
					if (targetBlocks.get(sta).dx == 0 && targetBlocks.get(sta).dy == 0){
						int deltaX= targetBlocks.get(dyn).x - targetBlocks.get(sta).x;
						int deltaY = targetBlocks.get(dyn).y - targetBlocks.get(sta).y;
						deltaX = deltaX * deltaX;
						deltaY = deltaY * deltaY;
						double dist = Math.sqrt(deltaX + deltaY);
						if(dist < minDist){
							minDist = dist;
							dynM = dyn;
							staM = sta;
						}
					}
				}
				
				// 5th frame
				fifthBlocks.get(dyn);
				
				// replace block
				targetBlocks.get(dynM).valuesRGB = targetBlocks.get(staM).valuesRGB;
			}
		}
		
		Image rm1 = new Image(width, height);
		for(int i = 0; i < targetBlocks.size(); i++){
			targetBlocks.get(i).compose(rm1);
		}
		rm1.write2PPM("removed_DynamicStatic.PPM");
		
		for(int dyn = 0; dyn < targetBlocks.size(); dyn++){
			if (targetBlocks.get(dyn).dx != 0 || targetBlocks.get(dyn).dy != 0){
				// replace block
				targetBlocks.get(dyn).valuesRGB = fifthBlocks.get(dyn).valuesRGB;
			}
		}
		
		Image rm2 = new Image(width, height);
		for(int i = 0; i < targetBlocks.size(); i++){
			targetBlocks.get(i).compose(rm2);
		}
		rm2.write2PPM("removed_5th.PPM");
	}
	
	public void mv2txt(Image err, Image ref, Image tar){
		BufferedWriter bw = null;
		try{
			int curRow = 0;
			File txt = new File("mv.txt");
			bw = new BufferedWriter(new FileWriter(txt));
			bw.write("# Name: Rick Miyamoto");
			bw.newLine();
			bw.write("# Target image name: " + tar.getFileName() + ".ppm");
			bw.newLine();
			bw.write("# Reference image name: " + ref.getFileName() + ".ppm");
			bw.newLine();
			bw.write("# Number of target macro blocks: " + tar.getW() / n + " x " + tar.getH() / n + " (image size is " + tar.getW() + " x " + tar.getH() + ")");
			bw.newLine();
			bw.newLine();
			for(int i = 0; i < targetBlocks.size(); i++){
				
				if(targetBlocks.get(i).y > curRow){
					curRow = targetBlocks.get(i).y;
					bw.newLine();
				}
				bw.write("["+targetBlocks.get(i).dx+","+targetBlocks.get(i).dy+"] ");
				targetBlocks.get(i).composeError(err);
			}
			System.out.println("Successfully wrote to mv.txt");
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		err.write2PPM("errorImg.PPM");
		err.display("test");
	}
	
	public void makeMacroBlocks(Image target, int n, int p, List<MacroBlock> container){
		
		for(int j = 0; j < target.getH(); j+=n){
			for(int i = 0; i < target.getW(); i+=n){
				container.add(new MacroBlock(i, j, n, target));
			}
		}
	}
	
	// matchingCriteria		= 		MSD
	
	public int fullSearch (MacroBlock tarBl, int n, int p, Image refImg) {
		double cur_msd = -1, min_msd = 9999999;
		int u = -1, v = -1;
		MacroBlock matchBl = null; // best matched block
		
		for(int x1 = -p; x1 < p; x1++){
			for(int y1 = -p; y1 < p; y1++){
				
				MacroBlock refBl = new MacroBlock(tarBl.x + x1, tarBl.y + y1, n, refImg);
				cur_msd = msd(refBl, tarBl);
				if (cur_msd < min_msd){
					min_msd = cur_msd;
					u = x1;	/* 		get coords for mv 	*/
					v = y1;
					matchBl = refBl;
				}
				
			}
		}
		tarBl.dx = -u; 	// tar.x - (tar.x + x1) = tar.x - tar.x - x1 = - x1 = u
		tarBl.dy = -v;		// tar.y - (tar.y + y1) = tar.y - tar.y - y1 = - y1 = v
		tarBl.errors = residual(matchBl, tarBl);
		
		return 0;
	}
	
	public int[][] residual(MacroBlock match, MacroBlock tar){
		int[][] errors = new int [match.n][match.n];
		
		for(int j = 0; j < match.n; j++){
			for(int i = 0; i < match.n; i++){
				errors[i][j] = Math.abs(tar.valuesGray[i][j] - match.valuesGray[i][j]);
			}
		}
		
		return errors;
	}
	
	public double msd (MacroBlock ref, MacroBlock tar){
		double diff = 0, total = 0;
		int n = ref.n;
		
		for(int y = 0; y < n; y++){
			for(int x = 0; x < n; x++){
				diff = ref.valuesGray[x][y] - tar.valuesGray[x][y];
				total += (diff * diff);
			}
		}
		
		return (total / (n * n));
	}

}

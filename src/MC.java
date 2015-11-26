import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MC {
	
	private List <MacroBlock> macroBlocks;
	private Image ref, tar, err;
	private int n, p;
	
	class MacroBlock {
		public int n, residual, x, y, dx, dy; // positionX and positionY are based on x and y coordinate of Image
		private int values[][], errors[][];
		private Image src;
		
		public MacroBlock(int x, int y, int n, char mode) {
			if(mode == 'r')
				src = ref;
			else if (mode == 't')
				src = tar;
			
			this.n = n;
			this.x = x;
			this.y = y;
			this.values = new int[n][n];
			fill();
		}
		
		public void fill(){
			for(int v = 0, j = y; j < y + n; j++, v++){
				for(int u = 0, i = x; i < x + n; i++, u++){
					if(i < src.getW() && i > 0 && j < src.getH() && j > 0){
						int rgb [] = new int [3];
						
						src.getPixel(i, j, rgb);
						int gray = (int) Math.round(0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
						values[u][v] = gray;
				
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
							
							for(int a = 0; a < 3; a++)
								rgb[a] = errors[u][v];
							
							
							err.setPixel(i,  j, rgb);

						}
					}
				}
			}
		}
		
		public void setValue(int i, int j, int val){
			this.values[i][j] = val;
		}
		
		public int getValue(int i, int j){
			return values[i][j];
		}
		
		public int getMacroBlockSize(){
			return n;
		}
		
		
	}
	
	// Assume i1 and i2 is converted to grayscale already
	public MC(int n, int p) {
		int curRow = 0;
		ref = new Image("Walk_059.ppm");
		tar = new Image("Walk_060.ppm");
		err = new Image(ref.getW(), ref.getH());
		this.n = n;
		this.p = p;
		
		makeMacroBlocks(ref, tar, n, p);
		BufferedWriter bw = null;
		try{
			File txt = new File("mv.txt");
			bw = new BufferedWriter(new FileWriter(txt));
			bw.write("# Name: Rick Miyamoto");
			bw.newLine();
			bw.write("# Target image name: ");
			bw.newLine();
			bw.write("# Reference image name: ");
			bw.newLine();
			bw.write("# Number of target macro blocks: " + tar.getW() / n + " x " + tar.getH() / n + " (image size is " + tar.getW() + " x " + tar.getH() + ")");
			bw.newLine();
			for(int i = 0; i < macroBlocks.size(); i++){
				fullSearch( macroBlocks.get(i), n, p );
				//System.out.print(macroBlocks.get(i).y + " ");
				
				if(macroBlocks.get(i).y > curRow){
					curRow = macroBlocks.get(i).y;
					System.out.println();
					bw.newLine();
				}
				System.out.print("["+macroBlocks.get(i).dx+","+macroBlocks.get(i).dy+"] ");
				bw.write("["+macroBlocks.get(i).dx+","+macroBlocks.get(i).dy+"] ");
				macroBlocks.get(i).composeError(err);
			}
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
	
	public void makeMacroBlocks(Image reference, Image target, int n, int p){
		macroBlocks = new ArrayList<MacroBlock>();
		
		for(int j = 0; j < target.getH(); j+=n){
			for(int i = 0; i < target.getW(); i+=n){
				macroBlocks.add(new MacroBlock(i, j, n, 't'));
				//fullSearch(reference, target, p, i, j, n); 
			}
		}
	}
	
	// matchingCriteria		= 		MSD
	
	public int fullSearch (MacroBlock tar, int n, int p) {
		double cur_msd = -1, min_msd = 9999999;
		int u = -1, v = -1;
		MacroBlock match = null; // best matched block
		
		for(int x1 = -p; x1 < p; x1++){
			for(int y1 = -p; y1 < p; y1++){
				
				MacroBlock ref = new MacroBlock(tar.x + x1, tar.y + y1, n, 'r');
				cur_msd = msd(ref, tar);
				//System.out.println(cur_msd);
				if (cur_msd < min_msd){
					min_msd = cur_msd;
					u = x1;	/* 		get coords for mv 	*/
					v = y1;
					match = ref;
				}
				
			}
		}
		//System.out.println(min_msd);
		//System.out.println("Best matching mv, u : " + u + " v : " + v);
		tar.dx = u; 	// tar.x - (tar.x + x1) = tar.x - tar.x - x1 = - x1 = u
		tar.dy = v;		// tar.y - (tar.y + y1) = tar.y - tar.y - y1 = - y1 = v
		tar.errors = residual(match, tar);
		
		return 0;
	}
	
	public int[][] residual(MacroBlock match, MacroBlock tar){
		int[][] errors = new int [match.n][match.n];
		
		for(int j = 0; j < match.n; j++){
			for(int i = 0; i < match.n; i++){
				errors[i][j] = Math.abs(tar.getValue(i, j) - match.getValue(i, j));
				//System.out.println(errors[i][j]);
			}
		}
		
		return errors;
	}
	
	public double msd (MacroBlock ref, MacroBlock tar){
		double diff = 0, total = 0;
		int n = ref.getMacroBlockSize();
		
		for(int y = 0; y < n; y++){
			for(int x = 0; x < n; x++){
				diff = ref.getValue(x, y) - tar.getValue(x, y);
				total += (diff * diff);
			}
		}
		
		return (total / (n * n));
	}

}

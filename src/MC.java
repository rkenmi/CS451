import java.util.ArrayList;
import java.util.List;


public class MC {
	
	private List <MacroBlock> macroBlocks;
	private Image ref, tar;
	private int n, p;
	
	class MacroBlock {
		public int n, x, y; // positionX and positionY are based on x and y coordinate of Image
		private int values[][];
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
		ref = new Image("Walk_070.ppm");
		tar = new Image("Walk_071.ppm");
		this.n = n;
		this.p = p;
		
		makeMacroBlocks(ref, tar, n, p);
		
		for(int i = 0; i < macroBlocks.size(); i++){
			fullSearch( macroBlocks.get(i), n, p);
		}
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
		int u = 0, v = 0;
		for(int x1 = -p; x1 < p; x1++){
			for(int y1 = -p; y1 < p; y1++){
				
				MacroBlock ref = new MacroBlock(tar.x + x1, tar.y + y1, n, 'r');
				cur_msd = msd(ref, tar);
				//System.out.println(cur_msd);
				if (cur_msd < min_msd){
					min_msd = cur_msd;
					u = x1;	/* 		get coords for mv 	*/
					v = y1;
					System.out.println("u : " + u + " v : " + v);
				}
				
			}
		}
		System.out.println(min_msd);
		
		return 0;
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

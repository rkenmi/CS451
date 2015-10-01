
public class test{
	public static void main(String[] args) {
		Integer a = 0x04;
		Integer b = 0xff;
		Byte[] rgb = new Byte[3];
		rgb[0] = 0;
		rgb[1] = -1;
		rgb[2] = 0;
		
		String s1 = String.format("%8s", Integer.toBinaryString(a)).replace(' ', '0');
		String binB = Integer.toBinaryString(b);
		//System.out.println(binA);
		//System.out.println(binB);
		
		int pix = 0xff000000 | ((rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | (rgb[2] & 0xff);
		System.out.println(s1);
		if(s1.charAt(7) == '1' || s1.charAt(6) == '1')
			System.out.println("yes");
	
		System.out.println(a & 0xff);
	 }
}

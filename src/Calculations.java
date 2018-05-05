
public class Calculations {

	public static final double TO_RADIANS = Math.PI / 180;
	public static final double TO_DEGREES = 180 / Math.PI;
	public static final double FOV = TO_RADIANS * 70;
	
	// Takes a number in a range, and returns the equivalent in a different range
	public static double map(double value, double min1, double max1, double min2, double max2) {
		return value / (max1-min1) * (max2-min2) + min2;
	}
	
	// Converts ARGB values to a hexadecimal number
	public static int RGBtoHex(int r, int g, int b) {
		return ((r&0xff) << 16) | ((g&0xff) << 8) | (b&0xff);
	}
	
	// Returns a random number in hexadecimal format
	public static int randomColor() {
		return Calculations.RGBtoHex((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
	}
	
	// Returns the red channel of a given color in hexadecimal format
	public static int red(int hex) {
	    return (hex & 0xFF0000) >> 16;
	}
	
	// Returns the green channel of a given color in hexadecimal format
	public static int green(int hex) {
	    return (hex & 0xFF00) >> 8;
	}
	
	// Returns the blue channel of a given color in hexadecimal format
	public static int blue(int hex) {
	    return (hex & 0xFF);
	}
	
	
}

import java.awt.Color;

public class Triangle {

	public Vector a, b, c;
	private Main main;
	Color color;
	Vector normal = new Vector(1, 1, 1);
	
	// rotates points of triangle
	public void rotate(Vector p, Vector p1, double amount) {
		a.rotate(p, p1, amount);
		b.rotate(p, p1, amount);
		c.rotate(p, p1, amount);
	}
	
	// Constructor
	public Triangle(Vector a, Vector b, Vector c, Color color, Main main) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.main = main;
		this.color = color;
	}
	
	// Returns the depth at a given point on the triangle
	public double depth(Vector a, Vector b, Vector c, double x, double y) {	
		Vector ac = a.subtract(c);
		Vector bc = b.subtract(c);
		normal = ac.crossProduct(bc);
		
		double d = normal.x;
		double e = normal.y;
		double f = normal.z;
		double g = -(d * c.x) - (e * c.y) - (f * c.z);
		return -(((d * x) + (e * y) + g) / f);
	}
	
	// Separates triangle into 2 triangles with horizontal bases
	Runnable render = () -> {
		int shade = getShade(20);
		int highlight = getHighlight();
			Vector a = this.a.project();
			Vector b = this.b.project();
			Vector c = this.c.project();
			
			if (a.y == b.y) rasterize(a, b, c, shade, highlight);
			else if (b.y == c.y) rasterize(b, c, a, shade, highlight);
			else if (a.y == c.y) rasterize(a, c, b, shade, highlight);
			else {
				Vector high = a, mid = b, low = c;
				
				if (a.y < b.y) {
					if (b.y < c.y) {
						high = a;
						mid = b;
						low = c;
					} else {
						if (a.y < c.y) {
							high = a;
							mid = c;
							low = b;
						} else {
							high = c;
							mid = a;
							low = b;
						}
					}
				} else {
					if (b.y < c.y) {
						if (a.y < c.y) {
							high = b;
							mid = a;
							low = c;
						} else {
							high = b;
							mid = c;
							low = a;
						}
					} else {
						high = c;
						mid = b;
						low = a;
					}
				}
				
				double mx = (low.x - high.x) / (low.y - high.y);
				double mz = (low.z - high.z) / (low.y - high.y); 
				
				Vector p = new Vector(
					high.x + (mx * (mid.y - high.y)),
					mid.y,
					high.z + (mz * (mid.y - high.y))
				);
				
				rasterize(mid, p, high, shade, highlight);
				rasterize(mid, p, low, shade, highlight);
			}
	};
	
	// Goes through pixels of horizontal triangle, calculates shade and fills color
	public void rasterize(Vector a, Vector b, Vector c, int shade, int highlight) {
		
		int ydir = c.y > a.y ? 1 : -1;
		int xdir = b.x > a.x ? 1 : -1;
		
		a.y = Math.round(a.y);
		a.x = Math.round(a.x);
		b.y = Math.round(b.y);
		b.x = Math.round(b.x);
		c.y = Math.round(c.y);
		c.x = Math.round(c.x);
		
		double l = (c.x - a.x)/(c.y - a.y);
		double r = (c.x - b.x)/(c.y - b.y);
		
		for (int row = (int)a.y; row != c.y; row += ydir){
			int cy = row + Main.HEIGHT/2;
			if (cy >= 0 && cy < Main.HEIGHT) {
				// Vectors a and b are inline with eachother
				for (int col = (int)(a.x + (row - a.y) * l); col != (int)(b.x + (row - a.y) * r) + xdir; col += xdir) {
					int cx = col + Main.WIDTH/2;
					
					if (cx >= 0 && cx < Main.WIDTH) {
						int index = cy * Main.WIDTH + cx;
						double depth = depth(a, b, c, col, row);
						if (main.zBuffer[index] > depth) {
							main.zBuffer[index] = depth;
							// just diffuse
//							main.pixels[index] = Main.RGBtoHex(color.getRed(), color.getGreen(), color.getBlue());
							
							float[] value = Color.RGBtoHSB((int)(color.getRed() * Main.map(shade, 0.0, 255.0, 0.0, 1.0)),
														   (int)(color.getGreen() * Main.map(shade, 0.0, 255.0, 0.0, 1.0)),
														   (int)(color.getBlue() * Main.map(shade, 0.0, 255.0, 0.0, 1.0)), null);
							value[1] = (float) Main.map(highlight, 0.0, 255.0, value[1], 0.0); // saturation
							value[2] = (float) Main.map(highlight, 0.0, 255.0, value[2], 1.0); // brightness
							
							Color result = Color.getHSBColor(value[0], value[1], value[2]);
							main.pixels[index] = Main.RGBtoHex(result.getRed(), result.getGreen(), result.getBlue());
							
//							 old color calculation (incorrect highlight color)
//							int lightValue = Math.min(Math.max(shade + highlight, 0), 255);
//							main.pixels[index] = Main.RGBtoHex(0xFF000000, (int)(color.getRed() * Main.map(lightValue, 0, 255, 0, 1)),
//																				(int)(color.getGreen() * Main.map(lightValue, 0, 255, 0, 1)), 
//																				(int)(color.getBlue() * Main.map(lightValue, 0, 255, 0, 1)));					
						}
					}
				}
			}
		}
	}
	
	public void getNormal() {
		Vector ab = a.subtract(b);
		Vector bc = b.subtract(c);
		normal = ab.crossProduct(bc);
	}
	
	
	
	
	// returns the shade value of a triangle based on its normal vector
	public int getHighlight() {
		Vector center = new Vector((a.x + b.x + c.x) / 3.0, (a.y + b.y + c.y) / 3.0, (a.z + b.z + c.z) / 3.0);
		
		// Highlight based lighting
		Vector light = center.subtract(main.lightSource);
		light.normalize(0, 0, 0);
		center.normalize(0, 0, 0);
		normal.normalize(0, 0, 0);
		light.rotate(new Vector(0, 0, 0), normal, Math.PI);
		double angle2 = Main.TO_DEGREES * (Math.acos(center.dotProduct3D(light)));
		int highlight = (int)(255.0/(1 + 0.004 * Math.pow(angle2, 2)));
//		int highlight = (int)(255.0 * Math.pow(Math.E, -0.07 * angle2));
		
//		int highlight = (int)(255.0 / (1.0 + (Math.pow(Math.E, 0.07 * (angle2 - 70)))));
//		int highlight = (int)Math.max((-0.03148 * Math.pow(angle2, 2) + 255), 0);
//		int highlight = (int)Math.min(Math.max(((-0.0014 * (Math.pow(angle2 - 45, 3))) + 127), 0), 255);

//		int highlight = (int)Math.max(((-255.0/90.0) * angle2 + 255.0), 0);
		return highlight;
		
	}
	
	public int getShade(int ambient) {
		// Old lighting
		Vector center = new Vector((a.x + b.x + c.x) / 3.0, (a.y + b.y + c.y) / 3.0, (a.z + b.z + c.z) / 3.0);
		double magnitude = main.lightSource.distance(0, 0, 0) * normal.distance(0, 0, 0);
		Vector light = center.subtract(main.lightSource);
		double angle = Main.TO_DEGREES * (Math.acos(light.dotProduct3D(normal) / magnitude));
		return (int)Math.min(Math.max((255.0/(1 + Math.pow(Math.E, 0.15 * (-angle+90)))) + ambient, 0), 255);
	}
	
}

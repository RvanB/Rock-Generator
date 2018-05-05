
public class Triangle {

	public Vector a, b, c;
	private Game game;
	private int color;
	Vector normal = new Vector(1, 1, 1);
	
	int shade;
	
	// rotates points of triangle
	public void rotate(Vector p, Vector p1, double amount) {
		a.rotate(p, p1, amount);
		b.rotate(p, p1, amount);
		c.rotate(p, p1, amount);
	}
	
	// Constructor
	public Triangle(Vector a, Vector b, Vector c, int color, Game game) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.game = game;
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
	
	// Goes through pixels of horizontal triangle, calculates shade and fills color
	public void rasterize(Vector a, Vector b, Vector c) {
		
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
			int cy = row + Game.HEIGHT/2;
			if (cy >= 0 && cy < Game.HEIGHT) {
				// Vectors a and b are inline with eachother
				for (int col = (int)(a.x + (row - a.y) * l); col != (int)(b.x + (row - a.y) * r) + xdir; col += xdir) {
					int cx = col + Game.WIDTH/2;
					
					if (cx >= 0 && cx < Game.WIDTH) {
						int index = cy * Game.WIDTH + cx;
						double depth = depth(a, b, c, col, row);
						if (game.zBuffer[index] > depth) {
							game.zBuffer[index] = depth;
							game.pixels[index] = Calculations.RGBtoHex(0xFF000000, (int)(Calculations.red(color) * Calculations.map(shade, 0, 0xFF, 0, 1)), (int)(Calculations.green(color) * Calculations.map(shade, 0, 0xFF, 0, 1)), (int)(Calculations.blue(color) * Calculations.map(shade, 0, 0xFF, 0, 1)));
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
	
	// Separates triangle into 2 triangles with horizontal bases
	Runnable render = () -> {
		shade = calculateShade();
		
//		if (a.dotProduct3D(normal) < 0) {
			Vector a = this.a.project();
			Vector b = this.b.project();
			Vector c = this.c.project();
			
			if (a.y == b.y) rasterize(a, b, c);
			else if (b.y == c.y) rasterize(b, c, a);
			else if (a.y == c.y) rasterize(a, c, b);
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
				
				rasterize(mid, p, high);
				rasterize(mid, p, low);
			}	
//		}
	};
	
	
	// returns the shade value of a triangle based on its normal vector
	public int calculateShade() {
		Vector center = new Vector((a.x + b.x + c.x) / 3.0, (a.y + b.y + c.y) / 3.0, (a.z + b.z + c.z) / 3.0);
		double magnitude = game.lightSource.distance(0, 0, 0) * normal.distance(0, 0, 0);
		Vector light = center.subtract(game.lightSource);
		// Old lighting
		double angle = Calculations.TO_DEGREES * (Math.acos(light.dotProduct3D(normal) / magnitude));
		int shadow = (int)Math.round((127.0/(1 + Math.pow(Math.E, 0.1 * (-angle+90)))));
		
		// Highlight based lighting
		light.normalize(0, 0, 0);
		center.normalize(0, 0, 0);
		normal.normalize(0, 0, 0);
		light.rotate(new Vector(0, 0, 0), normal, Math.PI);
		double angle2 = Calculations.TO_DEGREES * (Math.acos(center.dotProduct3D(light)));
		int highlight = (int)(255.0/(1 + 0.004 * Math.pow(angle2, 2)));
//		int highlight = (int)(255.0 / (1.0 + (Math.pow(Math.E, 0.07 * (angle2 - 70)))));
//		int shadow = (int)Math.max((-0.03148 * Math.pow(angle2, 2) + 255), 0);
//		int highlight = (int)Math.min(Math.max(((-0.0014 * (Math.pow(angle2 - 45, 3))) + 127), 0), 255);
//		int highlight = (int)(255.0 * Math.pow(Math.E, -0.07 * angle2));
//		int highlight = (int)Math.max(((-255.0/90.0) * angle2 + 255.0), 0);
		return Math.min(Math.max(highlight + shadow, 0), 255);
		
	}
	
}

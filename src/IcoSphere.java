import java.util.ArrayList;

public class IcoSphere {
	
	public ArrayList<Triangle> triangles = new ArrayList<>();
	public Vector center;
	private Game game;
	public int color;
	private Map map;
	int angle = 0;
	double phi = (1 + Math.sqrt(5)) / 2.0;
	Vector[] base = { // initializes icosahedron
			new Vector(-1, phi, 0),
			new Vector(1, phi, 0),
			new Vector(-1, -phi, 0),
			new Vector(1, -phi, 0),
			new Vector(0, -1, phi),
			new Vector(0, 1, phi),
			new Vector(0, -1, -phi),
			new Vector(0, 1, -phi),
			new Vector(phi, 0, -1),
			new Vector(phi, 0, 1),
			new Vector(-phi, 0, -1),
			new Vector(-phi, 0, 1)
	};
	
	// Constructor
	public IcoSphere(Vector center, int color, Game game) {
		this.center = center;
		this.game = game;
		this.color = color;
		for (int i = 0; i < base.length; i++) {
			base[i].x += center.x;
			base[i].y += center.y;
			base[i].z += center.z;
		}
		create();
		map = new Map();
	}
	
	// updates object, applies transformations
	public void update() {
		Vector rotateVector = new Vector(Math.cos(Calculations.TO_RADIANS * 25), Math.sin(Calculations.TO_RADIANS * 25), center.z);
		if (game.rotating) {
			rotate(center, rotateVector, Calculations.TO_RADIANS * 1);
		}
		
	}
	
	// rotates all triangles in the object
	public void rotate(Vector p, Vector p1, double amount) {
		for (int i = 0; i < triangles.size(); i++) {
			Triangle t = triangles.get(i);
			t.rotate(p, p1, amount);
		}
	}
	
	// Creates triangles between icosahedron points
	public void create() {
		Triangle[] t = {
				new Triangle(base[0].copy(), base[11].copy(), base[5].copy(), color, game),
				new Triangle(base[0].copy(), base[5].copy(), base[1].copy(), color, game),
				new Triangle(base[0].copy(), base[1].copy(), base[7].copy(), color, game),
				new Triangle(base[0].copy(), base[7].copy(), base[10].copy(), color, game),
				new Triangle(base[0].copy(), base[10].copy(), base[11].copy(), color, game),
				
				new Triangle(base[1].copy(), base[5].copy(), base[9].copy(), color, game),
				new Triangle(base[5].copy(), base[11].copy(), base[4].copy(), color, game),
				new Triangle(base[11].copy(), base[10].copy(), base[2].copy(), color, game),
				new Triangle(base[10].copy(), base[7].copy(), base[6].copy(), color, game),
				new Triangle(base[7].copy(), base[1].copy(), base[8].copy(), color, game),
				
				new Triangle(base[3].copy(), base[9].copy(), base[4].copy(), color, game),
				new Triangle(base[3].copy(), base[4].copy(), base[2].copy(), color, game),
				new Triangle(base[3].copy(), base[2].copy(), base[6].copy(), color, game),
				new Triangle(base[3].copy(), base[6].copy(), base[8].copy(), color, game),
				new Triangle(base[3].copy(), base[8].copy(), base[9].copy(), color, game),
				
				new Triangle(base[4].copy(), base[9].copy(), base[5].copy(), color, game),
				new Triangle(base[2].copy(), base[4].copy(), base[11].copy(), color, game),
				new Triangle(base[6].copy(), base[2].copy(), base[10].copy(), color, game),
				new Triangle(base[8].copy(), base[6].copy(), base[7].copy(), color, game),
				new Triangle(base[9].copy(), base[8].copy(), base[1].copy(), color, game),
		};
		
		ArrayList<Triangle> st = new ArrayList<Triangle>();
		for (int i = 0; i < t.length; i++) {
			st.add(t[i]);
		}
		triangles = st;
	}
	
	// Subdivides surface triangles, normalizes vectors, applies perlin noise
	public void subdivide() {
		boolean wasRotating = game.rotating;
		game.rotating = false;
		
		ArrayList<Triangle> st = new ArrayList<>();
		for (int i = 0; i < triangles.size(); i++) {
			Triangle l = triangles.get(i);
			
			Vector a = l.a;
			Vector b = l.b;
			Vector c = l.c;
			Vector d = a.average(c);
			Vector e = b.average(a);
			Vector f = c.average(b);
			
			l.a = d;
			l.b = e;
			l.c = f;
			int red = Calculations.red(color);
			int green = Calculations.green(color);
			int blue = Calculations.blue(color);
			int alpha = Calculations.alpha(color);
			st.add(new Triangle(d.copy(), a.copy(), e.copy(), Calculations.RGBtoHex(alpha, red, green, blue), game));
			st.add(new Triangle(b.copy(), f.copy(), e.copy(), Calculations.RGBtoHex(alpha, red, green, blue), game));
			st.add(l);
			st.add(new Triangle(c.copy(), d.copy(), f.copy(), Calculations.RGBtoHex(alpha, red, green, blue), game));
			
		}
		for (int i = 0; i < st.size(); i++) {
			st.get(i).a.perlin(center.x, center.y, center.z, map);
			st.get(i).b.perlin(center.x, center.y, center.z, map);
			st.get(i).c.perlin(center.x, center.y, center.z, map);
//			st.get(i).a.normalize(center.x, center.y, center.z);
//			st.get(i).b.normalize(center.x, center.y, center.z);
//			st.get(i).c.normalize(center.x, center.y, center.z);
			game.subdivideProgress = (float)i / (float)st.size();
		}
		game.subdivideProgress = 0f;
		triangles = st;
		game.rotating = wasRotating;
		
	}
}

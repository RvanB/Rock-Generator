import java.awt.Color;
import java.util.ArrayList;

public class IcoSphere {
	
	public ArrayList<Triangle> triangles = new ArrayList<>();
	public Vector center;
	private Main main;
	public Color color;
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
	public IcoSphere(Vector center, Color color, Main main) {
		this.center = center;
		this.main = main;
		this.color = color;
		for (int i = 0; i < base.length; i++) {
			base[i].x += center.x;
			base[i].y += center.y;
			base[i].z += center.z;
		}
		create();
		map = new Map();
	}
	
	public double getVolume() {
		double volume = 0;
		for (Triangle t : triangles) {
			Vector AB = new Vector(t.a.x - center.x, t.a.y - center.y, t.a.z - center.z);
			Vector AC = new Vector(t.b.x - center.x, t.b.y - center.y, t.b.z - center.z);
			Vector AD = new Vector(t.c.x - center.x, t.c.y - center.y, t.c.z - center.z);
			
			volume += AD.dotProduct3D(AB.crossProduct(AC));
		}
		return volume / 6.0;
	}
	
	// updates object, applies transformations
	public void update() {
		rotate(center, new Vector(1, 0, center.z), main.pitchVelocity);
		rotate(center, new Vector(0, 1, center.z), main.yawVelocity);
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
				new Triangle(base[0].copy(), base[11].copy(), base[5].copy(), color, main),
				new Triangle(base[0].copy(), base[5].copy(), base[1].copy(), color, main),
				new Triangle(base[0].copy(), base[1].copy(), base[7].copy(), color, main),
				new Triangle(base[0].copy(), base[7].copy(), base[10].copy(), color, main),
				new Triangle(base[0].copy(), base[10].copy(), base[11].copy(), color, main),
				
				new Triangle(base[1].copy(), base[5].copy(), base[9].copy(), color, main),
				new Triangle(base[5].copy(), base[11].copy(), base[4].copy(), color, main),
				new Triangle(base[11].copy(), base[10].copy(), base[2].copy(), color, main),
				new Triangle(base[10].copy(), base[7].copy(), base[6].copy(), color, main),
				new Triangle(base[7].copy(), base[1].copy(), base[8].copy(), color, main),
				
				new Triangle(base[3].copy(), base[9].copy(), base[4].copy(), color, main),
				new Triangle(base[3].copy(), base[4].copy(), base[2].copy(), color, main),
				new Triangle(base[3].copy(), base[2].copy(), base[6].copy(), color, main),
				new Triangle(base[3].copy(), base[6].copy(), base[8].copy(), color, main),
				new Triangle(base[3].copy(), base[8].copy(), base[9].copy(), color, main),
				
				new Triangle(base[4].copy(), base[9].copy(), base[5].copy(), color, main),
				new Triangle(base[2].copy(), base[4].copy(), base[11].copy(), color, main),
				new Triangle(base[6].copy(), base[2].copy(), base[10].copy(), color, main),
				new Triangle(base[8].copy(), base[6].copy(), base[7].copy(), color, main),
				new Triangle(base[9].copy(), base[8].copy(), base[1].copy(), color, main),
		};
		
		ArrayList<Triangle> st = new ArrayList<Triangle>();
		for (int i = 0; i < t.length; i++) {
			st.add(t[i]);
		}
		triangles = st;
	}
	
	// Subdivides surface triangles, normalizes vectors, applies perlin noise
	public void subdivide() {
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
			
			st.add(new Triangle(d.copy(), a.copy(), e.copy(), color, main));
			st.add(new Triangle(b.copy(), f.copy(), e.copy(), color, main));
			st.add(l);
			st.add(new Triangle(c.copy(), d.copy(), f.copy(), color, main));
			
		}
		for (int i = 0; i < st.size(); i++) {
//			st.get(i).a.perlin(center.x, center.y, center.z, map);
//			st.get(i).b.perlin(center.x, center.y, center.z, map);
//			st.get(i).c.perlin(center.x, center.y, center.z, map);
			st.get(i).a.normalize(center.x, center.y, center.z);
			st.get(i).b.normalize(center.x, center.y, center.z);
			st.get(i).c.normalize(center.x, center.y, center.z);
			main.subdivideProgress = (float)i / (float)st.size();
		}
		main.subdivideProgress = 0f;
		triangles = st;
		
	}
}

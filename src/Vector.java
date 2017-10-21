
public class Vector {

	public double x, y, z;
	
	// Constructor
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	// Subtracts two vectors
	public Vector subtract(Vector b) {
		return new Vector(x - b.x, y - b.y, z - b.z);
	}
	
	// Projects a 3D vector onto the 2D screen
	public Vector project() {
		return new Vector(x / z * Game.WIDTH, y / z * Game.WIDTH, z);
	}
	
	// Returns the unit vector
	public void normalize(double x, double y, double z) {
		double length = Math.abs(distance(x, y, z));
		this.x -= x;
		this.y -= y;
		this.z -= z;
		
		this.x /= length;
		this.y /= length;
		this.z /= length;

		this.x += x;
		this.y += y;
		this.z += z;
	}
	
	// Applies a magnitude to a vector
	public void perlin(double x, double y, double z, Map map) {
		
		double length = Math.abs(distance(x, y, z));
		
		double scalar = Math.abs(Calculations.map(map.altitude(this.x, this.y, this.z), -1, 1, -3, 3));
		
		this.x -= x;
		this.y -= y;
		this.z -= z;
		
		this.x /= length;
		this.y /= length;
		this.z /= length;
		
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		
		this.x += x;
		this.y += y;
		this.z += z;
	}
	
	// Returns the cross product of another vector
	public Vector crossProduct(Vector v) {
		return new Vector(
			y * v.z - z * v.y,
			z * v.x - x * v.z,
			x * v.y - y * v.x
		);
	}
	
	// Returns a vector that is the average of two vectors
	public Vector average(Vector v) {
		return new Vector((x + v.x) / 2.0, (y + v.y) / 2.0, (z + v.z) / 2.0);
	}
	
	// Returns a copy of the vector
	public Vector copy() {
		return new Vector(x, y, z);
	}
	
	// Returns the distance of the vector to a given x, y, z
	public double distance(double x, double y, double z) {
		return Math.sqrt(
				((this.x - x) * (this.x - x)) + 
				((this.y - y) * (this.y - y)) + 
				((this.z - z) * (this.z - z))
				);
	}
	
	// Returns the 3D dot product of two vectors
	public double dotProduct3D(Vector v) {
		return (x * v.x) + (y * v.y) + (z * v.z);
	}
	
	// Returns the 2D dot product of two vectors
	public double dotProduct2D(Vector v) {
		return (x * v.x) + (y * v.y);
	}
	
	// Rotates a vector around a segment, with a given number of radians
	public void rotate(Vector p, Vector p1, double amount) {
		Vector rotateVector = p1.subtract(p);
		if (rotateVector.distance(0, 0, 0) != 1) rotateVector.normalize(0, 0, 0);
		x -= p.x;
		y -= p.y;
		z -= p.z;
		
		Vector cross = rotateVector.crossProduct(this);
		double dot = rotateVector.dotProduct3D(this);
		
		double cos = Math.cos(amount);
		double sin = Math.sin(amount);
		
		x = ((1 - cos) * dot) * rotateVector.x + (cos * x) + (sin * cross.x);
		y = ((1 - cos) * dot) * rotateVector.y + (cos * y) + (sin * cross.y);
		z = ((1 - cos) * dot) * rotateVector.z + (cos * z) + (sin * cross.z);
		
		x += p.x;
		y += p.y;
		z += p.z;
		
	}
	
	
}

import java.awt.Color;

public class Light extends Vector {

	public Color color;
	
	public Light(double x, double y, double z, Color color) {
		super(x, y, z);
		this.color = color;
	}
	
	public Light(Vector v, Color color) {
		super(v.x, v.y, v.z);
		this.color = color;
	}
	
}

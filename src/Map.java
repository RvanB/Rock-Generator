import java.util.Random;

public class Map {
	double seed = Math.random();
		
	// Calculates the altitude at a given x, y, z using 3D perlin noise
	public double altitude(double x, double y, double z) {
		double altitude = 0;
		
		for (double interval = Math.PI * 4; interval > 0.01; interval /= 2.0) {
			double nx = Math.floor(x / interval) * interval;
			double ny = Math.floor(y / interval) * interval;
			double nz = Math.floor(z / interval) * interval;
			
			double a1 = f(nx, ny, nz);
			double a2 = f(nx + interval, ny, nz);
			double a3 = f(nx, ny, nz + interval);
			double a4 = f(nx + interval, ny, nz + interval);
			double a5 = f(nx, ny + interval, nz);
			double a6 = f(nx + interval, ny + interval, nz);
			double a7 = f(nx, ny + interval, nz + interval);
			double a8 = f(nx + interval, ny + interval, nz + interval);
			double a9 = interpolate(nx, nx + interval, a1, a2, x);
			double a10 = interpolate(nx, nx + interval, a3, a4, x);
			double a11 = interpolate(nx, nx + interval, a5, a6, x);
			double a12 = interpolate(nx, nx + interval, a7, a8, x);
			double a13 = interpolate(nz, nz + interval, a9, a10, z);
			double a14 = interpolate(nz, nz + interval, a11, a12, z);
			double val = interpolate(ny, ny + interval, a13, a14, y);
			
			altitude += interval / 14.0 * val;
		}
		return altitude;
	}
	
	// Interpolates between 2 points
	private double interpolate(double minloc, double maxloc, double minval, double maxval, double loc) {
		double percent = (loc - minloc) / (maxloc - minloc);
		return (1.0 - percent) * minval + percent * maxval;
	}

	
	// Returns a altitude used in perlin noise using seed
	private double f(double x, double y, double z) {
		Random r = new Random((int)(Math.abs(9*Math.sin(x)) * 100 + Math.abs(9 * Math.cos(y)) * 10 + Math.abs(9 * Math.sin(z))));
		String s = x + "" + y + "" + z;
		return (-s.hashCode() % (r.nextDouble() * seed));
		
	}
	
}

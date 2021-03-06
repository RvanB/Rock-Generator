import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Main extends Canvas implements Runnable, KeyListener, MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	private BufferedImage superResolution = new BufferedImage(WIDTH*2, HEIGHT*2, BufferedImage.TYPE_INT_RGB);
	public int[] superResolutionPixels = ((DataBufferInt) superResolution.getRaster().getDataBuffer()).getData();
	public double[] superZBuffer = new double[WIDTH*HEIGHT*4];
	public static int superScalar = 2;
	private BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	public double[] zBuffer = new double[WIDTH*HEIGHT];
	public int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
	public boolean running = false;
	public IcoSphere object;
	public ExecutorService executor;
	public Color backgroundColor = Color.BLACK;
	private Color hudColor = Color.WHITE;
	public ArrayList<Light> lights = new ArrayList<Light>();
	private boolean subdividing = false;
	public float subdivideProgress = 0;
	private boolean mouseDown = false;
	private int mouseX = 0;
	private int mouseY = 0;
	public double yawVelocity = 0;
	public double pitchVelocity = 0;
	public static boolean projectionMethod = false;
	double volume = 0;
	double ambientIntensity = 0.3;
	
	public static final double TO_RADIANS = Math.PI / 180.0;
	public static final double TO_DEGREES = 180.0 / Math.PI;
	
	public static Color additiveMix(Color a, Color b) {
		double red = a.getRed() + b.getRed();
		double green = a.getGreen() + b.getGreen();
		double blue = a.getBlue() + b.getBlue();
		double max = Math.max(red, Math.max(green, blue));
		if (max > 255) {
			red = red / max * 255.0;
			green = green / max * 255.0;
			blue = blue / max * 255.0;
		}
		return new Color((int)red, (int)green, (int)blue);
	}
	
	public static Color subtractiveMix(Color a, Color b) {
		Vector argb = new Vector(a.getRed() / 255.0, a.getGreen() / 255.0, a.getBlue() / 255.0);
		Vector brgb = new Vector(b.getRed() / 255.0, b.getGreen() / 255.0, b.getBlue() / 255.0);
		return new Color((int)(255 * argb.x * brgb.x), (int)(255 * argb.y * brgb.y), (int)(255 * argb.z * brgb.z));
	}
	
	// Sets up window and initializes objects
	public Main() {
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		
		JFrame frame = new JFrame("Rock Generator by Raiden van Bronkhorst and Bryan Mendes");
//		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
		frame.addKeyListener(this);
		frame.setLocationRelativeTo(null);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		object = new IcoSphere(new Vector(0, 0, 3), Color.WHITE, this);
		
//		for (int i = 0; i < Math.random() * 3; i++) {
//			randomLight();
//		}
		
		lights.add(new Light(-.5, .5, -1, Color.GREEN));
		lights.add(new Light(.5, .5, -1, Color.RED));
		lights.add(new Light(0, 0, -1, Color.BLUE));
		randomLight();
		randomLight();
		executor = Executors.newWorkStealingPool();
		
	}
	
	public void randomLight() {
		lights.add(new Light(Math.random() * 200 - 100, Math.random() * 200 - 100, Math.random() * 200 - 100, new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255))));
	}
	
	// Starts the main thread and background color
	public void start() {
		running = true;
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = RGBtoHex(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());
		}
		new Thread(this).start();
	}
	
	// updates depth buffer, pixel background and all objects every frame
	public void update() {
//		lightSource.rotate(object.center, new Vector(object.center.x, object.center.y - 20, object.center.z), Calculations.TO_RADIANS * 1);
		for (int i = 0; i < HEIGHT*superScalar * WIDTH*superScalar; i++) {
			superZBuffer[i] = 50;
			superResolutionPixels[i] = RGBtoHex(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());;
		}
		
		object.update();
		
		
	
		
		if (mouseDown) {
			pitchVelocity = 0;
			yawVelocity = 0;
		} else {
			pitchVelocity *= 0.99;
			yawVelocity *= 0.99;
		}
		ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
		
		for (int j = 0; j < object.triangles.size(); j++) {
			Triangle t = object.triangles.get(j);
			if (t.a.z > 0 && t.b.z > 0 && t.c.z > 0) {
				t.getNormal();
				if (t.a.dotProduct3D(t.normal) < 0) // backface culling
					futures.add(executor.submit(t.render));
			}
		}
		for (Future<?> f : futures) {
			try {
				f.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		futures.clear();
	}
	
	// Renders objects and manages multithreading
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		
		if (subdividing) {
			g.setColor(hudColor);
			g.drawRect(WIDTH / 2 - 100, 50, 200, 10);
			g.fillRect(WIDTH / 2 - 100, 50, (int) (subdivideProgress * 200), 10);
		} else {
			if (superScalar == 1) { 
				for (int i = 0; i < pixels.length; i++) {
					pixels[i] = superResolutionPixels[i];
					zBuffer[i] = superZBuffer[i];
				}
			} else {
				for (int y = 0; y < Main.HEIGHT * superScalar; y += superScalar) {
					for (int x = 0; x < Main.WIDTH * superScalar; x += superScalar) {
						
						double averageR = 0;
						double averageG = 0;
						double averageB = 0;
						double averageZ = 0;
						
						for (int subY = 0; subY < superScalar; subY++) {
							for (int subX = 0; subX < superScalar; subX++) {
								
								int i = (y + subY) * Main.WIDTH * superScalar + (x + subX);
								if ((y + subY < Main.HEIGHT * superScalar) && (x + subX < Main.WIDTH * superScalar)) {
									averageR += red(superResolutionPixels[i]);
									averageR /= 2.0;
									averageG += green(superResolutionPixels[i]);
									averageG /= 2.0;
									averageB += blue(superResolutionPixels[i]);
									averageB /= 2.0;
									
									averageZ += superZBuffer[i];
									averageZ /= 2.0;
								}
								
							}
						}
						
						int imgIndex = y/superScalar * Main.WIDTH + x/superScalar;
						pixels[imgIndex] = RGBtoHex((int)averageR, (int)averageG, (int)averageB);
						zBuffer[imgIndex] = averageZ;
					}
				}
			}
			
			g.drawImage(img, 0, 0, null);
		}
		g.setColor(hudColor);
		g.drawString("Volume: " + volume, 10, 20);
		g.dispose();
		bs.show();
	}
	
	// Takes a number in a range, and returns the equivalent in a different range
		public static double map(double value, double min1, double max1, double min2, double max2) {
			return value / (max1-min1) * (max2-min2) + min2;
		}
		
		// Converts ARGB values to a hexadecimal number
		public static int RGBtoHex(int r, int g, int b) {
			return ((r&0xff) << 16) | ((g&0xff) << 8) | (b&0xff);
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
		
		// Manages main loop
		public void run() {
			long lastTime = System.nanoTime();
			double unprocessed = 0;
			double nsPerTick = 1000000000.0 / 60.0;

			while (running) {
				long now = System.nanoTime();
				unprocessed += (now - lastTime) / nsPerTick;
				lastTime = now;
				while (unprocessed >= 1) {
					update();
					unprocessed -= 1;
				}
				render();
			}
			executor.shutdown();
		}
	
	public static void main(String[] args) {
		new Main().start();
	}

	// Handles key input
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		if (c == KeyEvent.VK_SPACE) {
			if (!subdividing) {
				double y = yawVelocity;
				double p = pitchVelocity;
				yawVelocity = 0;
				pitchVelocity = 0;
				subdividing = true;
				object.subdivide();
				subdividing = false;
				yawVelocity = y;
				pitchVelocity = p;
			}
			volume = object.getVolume();
		} else if (c == KeyEvent.VK_P) {
			projectionMethod = !projectionMethod;
		}
		else if (c == KeyEvent.VK_ESCAPE) System.exit(0);
		
	}
	
	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent e) {}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) {
		
		if (mouseDown) {
	
			int dx = e.getX() - mouseX;
			int dy = e.getY() - mouseY;
			
			yawVelocity = -Math.atan(dx/80.0);
			pitchVelocity = Math.atan(dy/80.0);
			
		}
		mouseX = e.getX();
		mouseY = e.getY();
		
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
	
	public void mousePressed(MouseEvent e) {
		yawVelocity = 0;
		pitchVelocity = 0;
		mouseDown = true;
	}

	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
	}
	
}

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Game extends Canvas implements Runnable, KeyListener {

	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	private static GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
	private BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
	public double[] zBuffer = new double[WIDTH*HEIGHT];
	public int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
	public boolean running = false;
	double lastUpdate = 0;
	double updateInterval = 1000.0 / 60.0;
	public IcoSphere planet;
	public ExecutorService executor;
	public boolean rotating = false;
	private int backgroundColor = 0xFF000000;
	private int planetColor = 0xFFFFFF;
	public Vector lightSource = new Vector(0,-5, 20);
	int currentFPS = 0;
	
	// Manages main loop
	public void run() {
		long lastTime = System.nanoTime();
		double unprocessed = 0;
		double nsPerTick = 1000000000.0 / 60.0;
		int frames = 0;
		long lastTimer1 = System.currentTimeMillis();

		while (running) {
			long now = System.nanoTime();
			unprocessed += (now - lastTime) / nsPerTick;
			lastTime = now;
			while (unprocessed >= 1) {
				update();
				unprocessed -= 1;
			}
			frames++;
			render();
			
			if (System.currentTimeMillis() - lastTimer1 > 1000) {
				lastTimer1 += 1000;
				currentFPS = frames;
				frames = 0;
			}
		}
		executor.shutdown();
	}
	
	// Sets up window and initializes objects
	public Game() {
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		
		JFrame frame = new JFrame();
//		frame.setUndecorated(true);
//		device.setFullScreenWindow(frame);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
		frame.addKeyListener(this);
		addKeyListener(this);
		planet = new IcoSphere(new Vector(0, 0, 20), planetColor, this);
		executor = Executors.newWorkStealingPool();
//		executor = Executors.newFixedThreadPool(1);
	}
	
	// Starts the main thread and background color
	public void start() {
		running = true;
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = backgroundColor;
		}
		new Thread(this).start();
	}
	
	// updates depth buffer, pixel background and all objects every frame
	public void update() {
		for (int row = 0; row < HEIGHT; row++) {
			for (int col = 0; col < WIDTH; col++) {
				int i = row * WIDTH + col;
				zBuffer[i] = 50;
				pixels[i] = backgroundColor;
			}
		}
		
		lightSource.rotate(new Vector(0, 0, 20), new Vector(0, 1, 0), Calculations.TO_RADIANS * 1);
		
		planet.update();
		
		
		
		ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
		
		for (int j = 0; j < planet.triangles.size(); j++) {
			Triangle t = planet.triangles.get(j);
			if (t.a.z > 0 && t.b.z > 0 && t.c.z > 0) {
				t.getNormal();
				if (t.a.dotProduct3D(t.normal) < 0) { // backface culling
					futures.add(executor.submit(t.render));
				}
				
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
		
		
		g.drawImage(img, 0, 0, null);
		g.drawString(currentFPS + "", 10, 20);
		g.dispose();
		bs.show();
	}
	
	public static void main(String[] args) {
		new Game().start();
	}

	// Handles key input
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		if (c == KeyEvent.VK_SPACE) planet.subdivide();
		if (c == KeyEvent.VK_ESCAPE) System.exit(0);
		if (c == KeyEvent.VK_R) rotating = rotating ? false : true;

	}
	
	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent e) {}
	
}

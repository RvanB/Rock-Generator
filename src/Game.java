import java.awt.Canvas;
import java.awt.Color;
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
	public IcoSphere planet;
	public ExecutorService executor;
	public boolean rotating = false;
	private int backgroundColor = 0xFF660000;
	private int planetColor = 0xFFFFFFFF;
	public Vector lightSource = new Vector(-10, -10, 15);
	private boolean subdividing = false;
	public float subdivideProgress = 0;
	
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
		frame.setLocationRelativeTo(null);
		addKeyListener(this);
		planet = new IcoSphere(new Vector(0, 0, 20), planetColor, this);
		executor = Executors.newWorkStealingPool();
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
//		lightSource.rotate(planet.center, new Vector(planet.center.x, planet.center.y - 20, planet.center.z), Calculations.TO_RADIANS * 1);
		for (int row = 0; row < HEIGHT; row++) {
			for (int col = 0; col < WIDTH; col++) {
				int i = row * WIDTH + col;
				zBuffer[i] = 50;
				pixels[i] = backgroundColor;
			}
		}
		
		planet.update();
		
		ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
		
		for (int j = 0; j < planet.triangles.size(); j++) {
			Triangle t = planet.triangles.get(j);
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
			g.setColor(Color.WHITE);
			g.drawRect(WIDTH / 2 - 100, 50, 200, 10);
			g.fillRect(WIDTH / 2 - 100, 50, (int) (subdivideProgress * 200), 10);
		}
		else {
			g.drawImage(img, 0, 0, null);
		}
		g.dispose();
		bs.show();
	}
	
	public static void main(String[] args) {
		new Game().start();
	}

	// Handles key input
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		if (c == KeyEvent.VK_SPACE) {
			if (!subdividing) {
				subdividing = true;
				planet.subdivide();
				subdividing = false;
			}
		}
		if (c == KeyEvent.VK_ESCAPE) System.exit(0);
		if (c == KeyEvent.VK_R) rotating = rotating ? false : true;

	}
	
	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent e) {}
	
}

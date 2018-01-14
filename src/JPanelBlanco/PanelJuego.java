package JPanelBlanco;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PanelJuego extends JPanel implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Sprite
	private ArrayList<Sprite> asteroides;
	private Sprite nave;
	private Sprite bala;
	private final int NUMERO_ASTEROIDES = 6;
	private final String RUTA_IMG_ASTEROIDE = "src//img//golden_star.gif";
	private final String RUTA_IMG_NAVE = "src//img//nave.png";
	private final String RUTA_IMG_FONDO = "src//img//heavy.jpg";

	// Elementos
	Image imgFondo = null, imgFondoReescalada = null;
	private File fondo;
	// Cronometro
	private float tiempoIncial;
	private float tiempoActual;
	private float tiempoJuego;
	// Propiedades
	private final int TAMANIO_ASTEROIDE = 40;
	//Acciones
	private boolean disparoActivo= true;

	/*
	 * Constructor
	 */
	public PanelJuego() {
		// Iniciar listado de sprites
		asteroides = new ArrayList<>();
		// Dibujar fondo
		cargarFondo();
		// Iniciar asteroides
		for (int i = 0; i < NUMERO_ASTEROIDES; i++) {
			asteroides.add(new Sprite(TAMANIO_ASTEROIDE, TAMANIO_ASTEROIDE, 10, 10, aleatorio(-15, 31),
					aleatorio(-15, 31), new Color(255, 100, 100), RUTA_IMG_ASTEROIDE));
		}
		// Iniciar Nave
		nave = new Sprite(45, 40, 300, 300, 0, 0, new Color(255, 200, 200), RUTA_IMG_NAVE);
		// Iniciar Cronometro
		tiempoIncial = System.nanoTime();
		// Anadir listenes
		listened();
		// Iniciar hilo
		new Thread(this).start();
	}// Fin del constructor

	/**
	 * Run del panel
	 */
	@Override
	public void run() {
		while (true) {
			this.repaint();
			// Movimiento bala
			if (bala != null) {
				if (bala.colisionConBordePantalla(getHeight())) {
					bala = null;
					disparoActivo=true;
				} else {
					bala.moverSprite(getWidth(), getHeight());
				}

			}

			// Asteroides
			for (int i = 0; i < asteroides.size(); i++) {
				// Colision por Bala
				if (bala != null && bala.colisionaCon(asteroides.get(i))) {
					asteroides.remove(i);
					bala = null;
					disparoActivo=true;
				} else {
					// Movimiento del asteroide
					asteroides.get(i).moverSprite(getWidth(), getHeight());
				}

			}
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Metodo sobreescrito para pintar el componente, frames por segundo
	 */
	@Override
	protected void paintComponent(Graphics g) {
		actualizarFondo(g);
		pintarAsteroide(g);
		pintarNave(g);
		actualizarTiempo();
		pintarTiempo(g);

		if (bala != null) {
			bala.pintarSprite(g);
		}
	}

	/**
	 * Dibuja el fondo del panel
	 */
	public void cargarFondo() {
		// Fondo
		fondo = new File(RUTA_IMG_FONDO);
		try {
			imgFondo = ImageIO.read(fondo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rellenar fondo
	 * 
	 * @param grafico
	 */
	public void actualizarFondo(Graphics g) {
		g.drawImage(imgFondoReescalada, 0, 0, null);
	}

	/**
	 * Pinta el contador de destruccion de sprites
	 */
	public void pintarTiempo(Graphics g) {
		// Escribir en grafico
		g.setColor(Color.WHITE);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
		// Formato para darle dos decilames
		DecimalFormat format = new DecimalFormat("#.##");
		g.drawString("Time: " + (format.format(tiempoJuego / 1000000000)), 20, 20);

	}

	/**
	 * Pinta el grafico con los datos del sprite
	 * 
	 * @param grafico
	 */
	public void pintarAsteroide(Graphics g) {
		for (int i = 0; i < asteroides.size(); i++) {
			asteroides.get(i).pintarSprite(g);
		}
	}

	public void pintarNave(Graphics g) {
		nave.pintarSprite(g);
	}

	public void pintarBala(Graphics g) {
		bala.pintarSprite(g);

	}

	/**
	 * Cronometro
	 */
	public void actualizarTiempo() {
		tiempoActual = System.nanoTime();
		tiempoJuego = tiempoActual - tiempoIncial;
	}

	/**
	 * Metodo para comprobar colision
	 * 
	 * @param sprite
	 *            que queremos comprobar
	 * @param posicion
	 *            que ocupa en la lista
	 */
	public void comprobarColision(Sprite sprite, int posicion) {
		for (int i = 0; i < asteroides.size(); i++) {
			for (int j = i + 1; j < asteroides.size(); j++) {
				if (asteroides.get(i).colisionaCon(asteroides.get(j))) {
					asteroides.remove(j);
					asteroides.remove(i);
				}

			}
		}

	}

	/**
	 * Obtiene un aleatorio
	 * 
	 * @param minimo
	 * @param maximo
	 * @return aleatorio entre minimo y maximo
	 */
	public int aleatorio(int minimo, int cantidad) {
		Random r = new Random();
		int aleatorio = r.nextInt(cantidad) + minimo;
		return aleatorio;
	}

	/**
	 * Metodo que incia todos los listenes del panel
	 */
	public void listened() {

		// Al presionar el raton
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)&&disparoActivo) {
					bala = new Sprite(16, 40, nave.getPosX() + ((nave.getAncho() / 2) - 4),
							nave.getPosY() + (nave.getAlto() / 2), 0, -10, Color.green, "");
					disparoActivo=false;
				}
			}
		});

		// El sprite de la nave se mov�ra conforme la posicion del raton
		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				nave.setPosX(e.getX() - (nave.getAncho() / 2));
				nave.setPosY(e.getY() - (nave.getAlto() / 2));
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				nave.setPosX(e.getX() - (nave.getAncho() / 2));
				nave.setPosY(e.getY() - (nave.getAlto() / 2));

			}
		});

		// Reescala la imagen de fondo si se hace resize la ventana
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				imgFondoReescalada = imgFondo.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);

			}
		});

	}

}
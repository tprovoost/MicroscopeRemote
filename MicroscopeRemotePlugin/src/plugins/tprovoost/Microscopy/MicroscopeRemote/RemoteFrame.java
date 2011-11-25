package plugins.tprovoost.Microscopy.MicroscopeRemote;

import icy.gui.component.IcyLogo;
import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.plugin.abstract_.Plugin;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeCore;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.StageListener;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.StageMover;

/**
 * This class is the core of the Remote Plugin. Three different threads are
 * running while using this class:
 * <ul>
 * <li>Refreshing the coordinates.</li>
 * <li>Moving the XY Stage</li>
 * <li>Moving the Z Stage.</li>
 * </ul>
 * 
 * @author Thomas Provoost
 */
public class RemoteFrame extends IcyFrame {

	// -------
	// GUI
	// -------
	Plugin plugin;
	private IcyLogo _logo_remote;
	private JSlider _sliderSpeed;
	private JLabel _lblX;
	private JLabel _lblY;
	private JLabel _lblZ;
	private JCheckBox _cbInvertX;
	private JCheckBox _cbInvertY;

	// CONSTANTS
	private static String currentPath = "plugins/tprovoost/Microscopy/MicroscopeRemote/images/";

	// -----------
	// PREFERENCES
	// -----------
	private Preferences _prefs;
	private static final String REMOTE = "prefs_remote";
	private static final String SPEED = "speed";
	private static final String INVERTX = "invertx";
	private static final String INVERTY = "inverty";

	public RemoteFrame(MicroscopeRemotePlugin plugin) {
		super("Remote", false, true, true, true);
		this.plugin = plugin;
		JPanel panelAll = new JPanel();
		panelAll.setLayout(new BoxLayout(panelAll, BoxLayout.Y_AXIS));

		_logo_remote = new IcyLogo("Remote");
		_logo_remote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
		panelAll.add(_logo_remote);

		// -------------
		// MOUSE MOVER
		// ------------
		JPanel panel_mover = GuiUtil.generatePanel("Mouse mover");
		panel_mover.setLayout(new BoxLayout(panel_mover, BoxLayout.X_AXIS));
		panel_mover.add(new PanelMoverXY());
		panel_mover.add(Box.createRigidArea(new Dimension(20, 10)));
		panel_mover.add(new PanelMoverZ());
		panelAll.add(panel_mover);

		// ---------
		// SPEED
		// ---------
		JPanel panel_speed = GuiUtil.generatePanel("Speed");
		panel_speed.setLayout(new BoxLayout(panel_speed, BoxLayout.X_AXIS));
		_sliderSpeed = new JSlider(1, 10, 1);
		final JLabel lbl_value = new JLabel("" + _sliderSpeed.getValue());

		_sliderSpeed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent changeevent) {
				lbl_value.setText("" + _sliderSpeed.getValue());
				_prefs.putInt(SPEED, _sliderSpeed.getValue());
			}
		});
		panel_speed.add(_sliderSpeed);
		panel_speed.add(Box.createRigidArea(new Dimension(20, 10)));
		panel_speed.add(lbl_value);
		panelAll.add(panel_speed);

		// -----------
		// COORDINATES
		// ----------
		_lblX = new JLabel("0.0000 µm");
		_lblY = new JLabel("0.0000 µm");
		_lblZ = new JLabel("0.0000 µm");
		JPanel panel_coords = GuiUtil.generatePanel("Current Position");
		panel_coords.setLayout(new BoxLayout(panel_coords, BoxLayout.X_AXIS));
		panel_coords.add(Box.createHorizontalGlue());
		panel_coords.add(new JLabel("x: "));
		panel_coords.add(_lblX);
		panel_coords.add(new JLabel(" y: "));
		panel_coords.add(_lblY);
		panel_coords.add(new JLabel(" z: "));
		panel_coords.add(_lblZ);
		panel_coords.add(Box.createHorizontalGlue());
		panelAll.add(panel_coords);

		// -------------------
		// INVERT CHEBKBOXES
		// -------------------
		_cbInvertX = new JCheckBox("Invert X-Axis");
		_cbInvertX.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				_prefs.putBoolean(INVERTX, _cbInvertX.isSelected());
				;
			}
		});
		JPanel panel_invertX = new JPanel();
		panel_invertX.add(Box.createHorizontalGlue());
		panel_invertX.add(_cbInvertX);
		panel_invertX.add(Box.createHorizontalGlue());

		_cbInvertY = new JCheckBox("Invert Y-Axis");
		_cbInvertY.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				_prefs.putBoolean(INVERTY, _cbInvertY.isSelected());
			}
		});

		JPanel panel_invertY = new JPanel();
		panel_invertY.add(Box.createHorizontalGlue());
		panel_invertY.add(_cbInvertY);
		panel_invertY.add(Box.createHorizontalGlue());

		JPanel panelInvert = GuiUtil.generatePanel("Axis tranformation");
		panelInvert.setLayout(new BoxLayout(panelInvert, BoxLayout.Y_AXIS));
		panelInvert.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		panelInvert.add(panel_invertX);
		panelInvert.add(panel_invertY);
		panelAll.add(panelInvert);

		add(panelAll);

		setVisible(true);
		validate();
		addToMainDesktopPane();
		refresh();
		center();
		requestFocus();
		loadPreferences();

		StageMover.addListener(new StageListener() {

			@Override
			public void stageMoved(final double x, final double y, final double z) {
				_lblX.setText(StringUtil.toString(x, 2) + " µm");
				_lblY.setText(StringUtil.toString(y, 2) + " µm");
				_lblZ.setText(StringUtil.toString(z, 2) + " µm");
			}
		});
	}

	/**
	 * Load preferences : speed, invertX and invertY.
	 */
	private void loadPreferences() {
		Preferences root = Preferences.userNodeForPackage(getClass());
		_prefs = root.node(root.absolutePath() + "/" + REMOTE);
		_sliderSpeed.setValue(_prefs.getInt(SPEED, 1));
		_cbInvertX.setSelected(_prefs.getBoolean(INVERTX, false));
		_cbInvertY.setSelected(_prefs.getBoolean(INVERTY, false));
	}

	void refresh() {
		pack();
	}

	@Override
	public void onClosed() {
		super.onClosed();
	}

	void setEnable(boolean b) {
		getContentPane().setEnabled(b);
	}

	public void updateCoordinates(double x, double y, double z) {
		String zs = String.valueOf(z);
		int dot_idx = zs.indexOf(".");
		if (zs.length() <= dot_idx + 2)
			zs += "0";
		else if (zs.length() > dot_idx + 2)
			zs = zs.substring(0, dot_idx + 2);
		_lblX.setText(String.valueOf((double) ((int) (-x * 100)) / 100));
		_lblY.setText(String.valueOf((double) ((int) (-y * 100)) / 100));
		_lblZ.setText(zs);
	}

	public class PanelMoverXY extends JPanel implements MouseListener, MouseMotionListener {

		/**
		 * Generated serial UID
		 */
		private static final long serialVersionUID = -5025582239086787935L;

		private static final int SIZE_PANEL_MOVER = 200;

		/** Movement Vector */
		private Point2D vector;
		MoveThread thread;
		private boolean started = false;
		private boolean stopMoving = true;

		Image originalBackground = null;

		public PanelMoverXY() {
			originalBackground = plugin.getImageResource(currentPath + "remote_backgroundXY.png");
			if (originalBackground == null)
				System.out.println("Background image for XY Axes not found.");
			vector = new Point2D.Double(0, 0);
			thread = new MoveThread();
			setDoubleBuffered(true);
			setSize(new Dimension(SIZE_PANEL_MOVER, SIZE_PANEL_MOVER));
			setPreferredSize(new Dimension(SIZE_PANEL_MOVER, SIZE_PANEL_MOVER));
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		@Override
		public void paint(Graphics g) {
			super.paintComponents(g);
			int w = getWidth();
			int h = getHeight();
			Shape shape;
			AffineTransform at;
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (originalBackground != null) {
				g2.drawImage(originalBackground, 0, 0, w, h, null);
				int stickBallDiameter = w / 5;
				Point2D centerBall = new Point2D.Double(w / 2d + vector.getX(), h / 2d + vector.getY());
				g2.setColor(Color.darkGray);
				double normVector = norm(vector);
				if (vector.getX() != 0 && vector.getY() != 0) {
					g2.setColor(Color.blue);
					// ----------
					// draw stick
					// ----------
					Graphics2D g3 = (Graphics2D) g2.create();
					at = AffineTransform.getTranslateInstance(centerBall.getX(), centerBall.getY());
					at.rotate(-vector.getX(), -vector.getY());
					at.translate(0, -stickBallDiameter / 4);
					g3.transform(at);
					g3.setPaint(new GradientPaint(new Point2D.Double(0, 0), Color.BLACK, new Point2D.Double(0, stickBallDiameter / 4), Color.LIGHT_GRAY, true));
					g3.fillRoundRect(0, 0, (int) normVector, stickBallDiameter / 2, stickBallDiameter / 2, stickBallDiameter / 2);
					g3.dispose();
				}
				// ---------
				// draw ball
				// ---------
				Paint defaultPaint = g2.getPaint();
				Point2D centerGradient = new Point2D.Double(centerBall.getX(), centerBall.getY());
				float radiusGradient = stickBallDiameter / 2;
				Point2D focusSpotLightGradient;
				if (Math.abs(vector.getX()) <= 1 && Math.abs(vector.getY()) <= 1) {
					focusSpotLightGradient = new Point2D.Double(centerBall.getX(), centerBall.getY());
				} else {
					focusSpotLightGradient = new Point2D.Double(centerBall.getX() + vector.getX() * (radiusGradient - 5) / normVector, centerBall.getY() + vector.getY() / normVector
							* (radiusGradient - 5));
				}
				float[] dist = { 0.1f, 0.3f, 1.0f };
				Color[] colors = { new Color(0.9f, 0.9f, 0.9f), Color.LIGHT_GRAY, Color.DARK_GRAY };
				RadialGradientPaint p = new RadialGradientPaint(centerGradient, radiusGradient, focusSpotLightGradient, dist, colors, CycleMethod.NO_CYCLE);
				g2.setPaint(p);
				g2.fillOval((int) centerBall.getX() - stickBallDiameter / 2, (int) centerBall.getY() - stickBallDiameter / 2, stickBallDiameter, stickBallDiameter);
				g2.setPaint(defaultPaint);
				g2.setColor(Color.BLACK);
				g2.drawOval((int) centerBall.getX() - stickBallDiameter / 2, (int) centerBall.getY() - stickBallDiameter / 2, stickBallDiameter, stickBallDiameter);
			} else {
				boolean useNormalColors;
				Color colorLookAndFeel = LookAndFeelUtil.getForeground(this);

				// -------------
				// Draw the grid
				// --------------
				// borders
				useNormalColors = colorLookAndFeel.getRed() < 50 && colorLookAndFeel.getGreen() < 50 && colorLookAndFeel.getBlue() < 50;
				if (useNormalColors)
					g2.setColor(Color.black);
				else
					g2.setColor(colorLookAndFeel);
				g2.drawRect(0, 0, w - 1, h - 1);

				// X AXIS
				if (useNormalColors)
					g2.setColor(Color.red);
				at = AffineTransform.getTranslateInstance(w / 2, h / 2);
				shape = at.createTransformedShape(createArrow(w, 10));
				g2.draw(shape);
				g2.drawString("x", w - 10, h / 2 + 10);

				// Y AXIS
				if (useNormalColors)
					g2.setColor(Color.green);
				at = AffineTransform.getTranslateInstance(w / 2, h / 2);
				at.rotate(-Math.PI / 2);
				shape = at.createTransformedShape(createArrow(h, 10));
				g2.draw(shape);
				g2.drawString("y", w / 2 - 10 - 10, 10);

				// if no vector, return if (vector.getX() == 0 && vector.getY()
				// == 0) return;

				// draw the arrow from vector if (useNormalColors)
				g2.setColor(Color.black);
				double translateX = w / 2 + vector.getX() / 2;
				double translateY = h / 2 + vector.getY() / 2;
				at = AffineTransform.getTranslateInstance((int) translateX, (int) translateY);
				at.rotate(vector.getX(), vector.getY());
				double norm = norm(vector) / 4;
				shape = at.createTransformedShape(createArrow((int) norm(vector), norm >= 1 ? (int) norm : 1));
				g2.draw(shape);
				g2.draw(g2.getStroke().createStrokedShape(shape));
			}
			g2.dispose();
		}

		private Path2D.Double createArrow(int length, int barb) {
			double angle = Math.toRadians(20);
			Path2D.Double path = new Path2D.Double();
			path.moveTo(-length / 2, 0);
			path.lineTo(length / 2, 0);
			double x = length / 2 - barb * Math.cos(angle);
			double y = barb * Math.sin(angle);
			path.lineTo(x, y);
			x = length / 2 - barb * Math.cos(-angle);
			y = barb * Math.sin(-angle);
			path.moveTo(length / 2, 0);
			path.lineTo(x, y);
			return path;
		}

		public void applyMovementXY() throws Exception {
			double normV = norm(vector);
			double x = vector.getX() / normV;
			double y = vector.getY() / normV;
			double percent = norm(vector) * _sliderSpeed.getValue();
			if (stopMoving)
				return;
			MicroscopeCore mCore = MicroscopeCore.getCore();
			if (mCore.getAvailablePixelSizeConfigs().size() == 0)
				StageMover.moveXYRelative(x * 0.001 * percent * percent, y * 0.01 * percent * percent, _cbInvertX.isSelected(), _cbInvertY.isSelected());
			else
				StageMover.moveXYRelative(x * 0.001 * mCore.getPixelSizeUm() * percent * percent, y * 0.01 * percent * percent, _cbInvertX.isSelected(), _cbInvertY.isSelected());
		}

		private double norm(Point2D vector) {
			return Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			int width = getWidth();
			int height = getHeight();

			if (x < 0)
				x = 0;
			if (x > width)
				x = width;

			if (y < 0)
				y = 0;
			if (y > height)
				y = height;
			vector.setLocation(x - width / 2, y - height / 2);
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			vector.setLocation(e.getX() - (getWidth() / 2), e.getY() - (getHeight() / 2));
			if (started)
				return;
			started = true;
			stopMoving = false;
			repaint();
			ThreadUtil.bgRun(thread);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			vector.setLocation(0, 0);
			if (!started)
				return;
			thread.setStop(true);
			started = false;
			stopMoving = true;
			repaint();
		}

		public class MoveThread implements Runnable {
			private boolean stop;

			public MoveThread() {
				stop = false;
			}

			@Override
			public void run() {
				stop = false;
				while (!stop) {
					try {
						applyMovementXY();
						repaint();
					} catch (Exception e) {
						break;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					StageMover.stopXYStage();
				} catch (Exception e) {
					e.printStackTrace();
				}
				setStop(false);
			}

			public synchronized void setStop(boolean stop) {
				this.stop = stop;
			}
		}
	}

	public class PanelMoverZ extends JPanel implements MouseListener, MouseMotionListener {

		/**
		 * Generated serial UID
		 */
		private static final long serialVersionUID = -5025582239086787935L;

		// CONSTANTS
		private static final int SIZE_PANEL_MOVERZ_W = 50;
		private static final int SIZE_PANEL_MOVERZ_H = 200;

		private static final double BARS_NUMBER = 200;

		/** Movement Vector */
		int oldY;

		Image originalBackground = null;
		Image originalBar = null;

		private int startPos = 0;

		public PanelMoverZ() {
			originalBackground = plugin.getImageResource(currentPath + "remote_backgroundZ.png");
			originalBar = plugin.getImageResource(currentPath + "singleBarZ.png");
			setDoubleBuffered(true);
			setSize(new Dimension(SIZE_PANEL_MOVERZ_W, SIZE_PANEL_MOVERZ_H));
			setPreferredSize(new Dimension(SIZE_PANEL_MOVERZ_W, SIZE_PANEL_MOVERZ_H));
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponents(g);
			int w = getWidth();
			int h = getHeight();
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (originalBackground != null && originalBar != null) {
				// draw the background + joystick
				g2.drawImage(originalBackground, 0, 0, w, h, null);
				double ecartNormal = (double) h / 8;
				double lastPos = h / 2d + startPos;
				for (int i = 0; i < BARS_NUMBER; ++i) {
					g2.drawImage(originalBar, 0, (int) lastPos, w, originalBar.getHeight(null) * w / originalBar.getWidth(null), null);
					lastPos = lastPos + ecartNormal / (1d + 0.1 * i * i);
					if (lastPos > h)
						break;
				}
				lastPos = h / 2d + startPos;
				for (int i = 0; i < BARS_NUMBER; ++i) {
					g2.drawImage(originalBar, 0, (int) lastPos, w, originalBar.getHeight(null) * w / originalBar.getWidth(null), null);
					lastPos = lastPos - ecartNormal / (1d + 0.1 * i * i);
					if (lastPos < 0)
						break;
				}
			} else {
				// draw the old version of remote
				boolean useNormalColors;
				Color colorLookAndFeel = LookAndFeelUtil.getForeground(this);
				useNormalColors = colorLookAndFeel.getRed() < 50 && colorLookAndFeel.getGreen() < 50 && colorLookAndFeel.getBlue() < 50;

				// borders
				if (useNormalColors)
					g2.setColor(Color.black);
				else
					g2.setColor(colorLookAndFeel);
				g2.drawRect(0, 0, w - 1, h - 1);

				// Draw the line in the middle
				if (useNormalColors)
					g2.setColor(Color.gray);
				g2.drawRect(0, 0, w, h);
				if (useNormalColors)
					g2.setColor(Color.blue);
				double ecartNormal = (double) h / 8;
				double lastPos = h / 2d + startPos;
				for (int i = 0; i < BARS_NUMBER; ++i) {
					g2.drawLine(0, (int) lastPos, w, (int) lastPos);
					lastPos = lastPos + ecartNormal / (1d + 0.1 * i * i);
					if (lastPos > h)
						break;
				}
				lastPos = h / 2d + startPos;
				for (int i = 0; i < BARS_NUMBER; ++i) {
					g2.drawLine(0, (int) lastPos, w, (int) lastPos);
					lastPos = lastPos - ecartNormal / (1d + 0.1 * i * i);
					if (lastPos < 0)
						break;
				}
			}
			g2.dispose();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int y = e.getY();
			if (y < 0)
				y = 0;
			if (y > getHeight())
				y = getHeight();
			int movY = e.getY() - oldY;
			oldY = e.getY();
			double ecartNormal = getHeight() / 8d / 2;
			if (movY > 0) {
				++startPos;
				if (startPos > ecartNormal)
					startPos = (int) -ecartNormal;
			} else {
				--startPos;
				if (startPos < -ecartNormal)
					startPos = (int) ecartNormal;
			}
			int percent = _sliderSpeed.getValue();
			try {
				StageMover.moveZRelative(movY * 0.01 * percent * percent);
			} catch (Exception e1) {
				System.out.println("Error with Z movement");
			}
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			oldY = e.getY();
			setCursor(new Cursor(Cursor.N_RESIZE_CURSOR | Cursor.S_RESIZE_CURSOR));
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			setCursor(Cursor.getDefaultCursor());
			repaint();
		}
	}
}

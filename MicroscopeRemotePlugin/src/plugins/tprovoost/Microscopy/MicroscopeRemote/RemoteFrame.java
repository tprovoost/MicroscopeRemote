package plugins.tprovoost.Microscopy.MicroscopeRemote;

import icy.gui.component.IcyLogo;
import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.system.thread.ThreadUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import icy.util.StringUtil;


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
	//   GUI
	// -------
	private IcyLogo _logo_remote;
	private JSlider _slider_speed;
	private JLabel _lbl_x;
	private JLabel _lbl_y;
	private JLabel _lbl_z;
	private JCheckBox cb_invert_x;
	private JCheckBox cb_invert_y;

	// -----------
	// PREFERENCES
	// -----------
	private Preferences _prefs;	
	private static final String REMOTE = "prefs_remote";
	private static final String SPEED = "speed";
	private static final String INVERTX = "invertx";
	private static final String INVERTY = "inverty";

	public RemoteFrame() {
		super("Remote", true, true, true, true);
		JPanel panel_all = new JPanel();
		panel_all.setLayout(new BoxLayout(panel_all, BoxLayout.Y_AXIS));

		_logo_remote = new IcyLogo("Remote");
		_logo_remote.setPreferredSize(new Dimension(0, 80));
		_logo_remote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
		panel_all.add(_logo_remote);

		// -------------
		// MOUSE MOVER
		// ------------
		JPanel panel_mover = GuiUtil.generatePanel("Mouse mover");
		panel_mover.setLayout(new BoxLayout(panel_mover, BoxLayout.X_AXIS));
		panel_mover.add(new PanelMoverXY());
		panel_mover.add(Box.createRigidArea(new Dimension(20, 10)));
		panel_mover.add(new PanelMoverZ());
		panel_all.add(panel_mover);

		// ---------
		// SPEED
		// ---------
		JPanel panel_speed = GuiUtil.generatePanel("Speed");
		panel_speed.setLayout(new BoxLayout(panel_speed, BoxLayout.X_AXIS));
		_slider_speed = new JSlider(1, 10, 1);
		final JLabel lbl_value = new JLabel("" + _slider_speed.getValue());

		_slider_speed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent changeevent) {
				lbl_value.setText("" + _slider_speed.getValue());
				_prefs.putInt(SPEED, _slider_speed.getValue());
			}
		});

		panel_speed.add(_slider_speed);
		panel_speed.add(Box.createRigidArea(new Dimension(20, 10)));
		panel_speed.add(lbl_value);
		panel_all.add(panel_speed);

		// -----------
		// COORDINATES
		// ----------
		_lbl_x = new JLabel("0.0000 µm");
		_lbl_y = new JLabel("0.0000 µm");
		_lbl_z = new JLabel("0.0000 µm");
		JPanel panel_coords = GuiUtil.generatePanel("Current Position");
		panel_coords.setLayout(new BoxLayout(panel_coords, BoxLayout.X_AXIS));
		panel_coords.add(Box.createHorizontalGlue());
		panel_coords.add(new JLabel("x : "));
		panel_coords.add(_lbl_x);
		panel_coords.add(new JLabel(" y : "));
		panel_coords.add(_lbl_y);
		panel_coords.add(new JLabel(" z : "));
		panel_coords.add(_lbl_z);
		panel_coords.add(Box.createHorizontalGlue());
		panel_all.add(panel_coords);

		// -------------------
		// INVERT CHEBKBOXES
		// -------------------
		cb_invert_x = new JCheckBox("Invert X-Axis");
		cb_invert_x.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				_prefs.putBoolean(INVERTX, cb_invert_x.isSelected());
				;
			}
		});
		JPanel panel_invertX = new JPanel();
		panel_invertX.add(Box.createHorizontalGlue());
		panel_invertX.add(cb_invert_x);
		panel_invertX.add(Box.createHorizontalGlue());

		cb_invert_y = new JCheckBox("Invert Y-Axis");
		cb_invert_y.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				_prefs.putBoolean(INVERTY, cb_invert_y.isSelected());
			}
		});

		JPanel panel_invertY = new JPanel();
		panel_invertY.add(Box.createHorizontalGlue());
		panel_invertY.add(cb_invert_y);
		panel_invertY.add(Box.createHorizontalGlue());

		JPanel panel_invert = GuiUtil.generatePanel("Axis tranformation");
		panel_invert.setLayout(new BoxLayout(panel_invert, BoxLayout.Y_AXIS));
		panel_invert.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		panel_invert.add(panel_invertX);
		panel_invert.add(panel_invertY);
		panel_all.add(panel_invert);

		add(panel_all);

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
				_lbl_x.setText(StringUtil.toString(x, 2) + " µm");
				_lbl_y.setText(StringUtil.toString(y, 2) + " µm");
				_lbl_z.setText(StringUtil.toString(z, 2) + " µm");
			}
		});
	}

	/**
	 * Load preferences : speed, invertX and invertY.
	 */
	private void loadPreferences() {
		Preferences root = Preferences.userNodeForPackage(getClass());
		_prefs = root.node(root.absolutePath() + "/" + REMOTE);
		_slider_speed.setValue(_prefs.getInt(SPEED, 1));
		cb_invert_x.setSelected(_prefs.getBoolean(INVERTX, false));
		cb_invert_y.setSelected(_prefs.getBoolean(INVERTY, false));
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
		_lbl_x.setText(String.valueOf((double) ((int) (-x * 100)) / 100));
		_lbl_y.setText(String.valueOf((double) ((int) (-y * 100)) / 100));
		_lbl_z.setText(zs);
	}

	public class PanelMoverXY extends JPanel implements MouseListener, MouseMotionListener {

		/**
		 * Generated serial UID
		 */
		private static final long serialVersionUID = -5025582239086787935L;

		private static final int SIZE_PANEL_MOVER = 200;

		/**
		 * Movement Vector
		 */
		private Point2D vector;
		MicroscopeCore _core = MicroscopeCore.getCore();
		MoveThread thread;
		private boolean started = false;
		private boolean stopMoving = true;

		public PanelMoverXY() {
			vector = new Point2D.Double(0, 0);
			thread = new MoveThread();
			setDoubleBuffered(true);
			setSize(new Dimension(SIZE_PANEL_MOVER, SIZE_PANEL_MOVER));
			setPreferredSize(new Dimension(SIZE_PANEL_MOVER, SIZE_PANEL_MOVER));
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponents(g);
			int w = getWidth();
			int h = getHeight();
			Shape shape;
			AffineTransform at;
			Graphics2D g2 = (Graphics2D) g.create();
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

			// if no vector, return
			if (vector.getX() == 0 && vector.getY() == 0)
				return;

			// draw the arrow from vector
			if (useNormalColors)
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
			double percent = norm(vector) * _slider_speed.getValue();
			if (stopMoving)
				return;
			MicroscopeCore mCore = MicroscopeCore.getCore();
			if (mCore.getAvailablePixelSizeConfigs().size() == 0)
				StageMover.moveXYRelative(x * 0.001 * percent * percent, y * 0.01 * percent * percent, cb_invert_x.isSelected(), cb_invert_y.isSelected());
			else
				StageMover.moveXYRelative(x * 0.001 * mCore.getPixelSizeUm() * percent * percent, y * 0.01 * percent * percent, cb_invert_x.isSelected(), cb_invert_y.isSelected());
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

		private static final int SIZE_PANEL_MOVERZ_W = 50;
		private static final int SIZE_PANEL_MOVERZ_H = 200;

		/**
		 * Movement Vector
		 */
		private Point2D vector;
		MicroscopeCore _core = MicroscopeCore.getCore();
		MoveThread thread;
		private boolean started = false;
		private boolean stopMoving = true;

		public PanelMoverZ() {
			vector = new Point2D.Double(0, 0);
			thread = new MoveThread();
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

			boolean useNormalColors;
			Color colorLookAndFeel = LookAndFeelUtil.getForeground(this);

			useNormalColors = colorLookAndFeel.getRed() < 50 && colorLookAndFeel.getGreen() < 50 && colorLookAndFeel.getBlue() < 50;

			// borders
			if (useNormalColors)
				g2.setColor(Color.black);
			else
				g2.setColor(colorLookAndFeel);
			g2.drawRect(0, 0, w - 1, h - 1);

			// Draw the line
			if (useNormalColors)
				g2.setColor(Color.gray);
			g2.drawRect(0, 0, w, h);

			if (useNormalColors)
				g2.setColor(Color.blue);
			g2.drawLine(0, h / 2, w, h / 2);
			g2.drawString("z", w / 2, 10);

			if (vector.getX() == 0 && vector.getY() == 0)
				return;
			// draw the arrow
			if (useNormalColors)
				g2.setColor(LookAndFeelUtil.getForeground(this));

			int translateY = h / 2;
			Path2D shape = new Path2D.Double();
			shape.moveTo(0, (int) (translateY + vector.getY()));
			shape.lineTo(getWidth(), (int) (translateY + vector.getY()));
			g2.draw(g2.getStroke().createStrokedShape(shape));
		}

		public void applyMovementZ() throws Exception {
			double percent = norm(vector) * 0.1 * _slider_speed.getValue();
			if (stopMoving)
				return;
			StageMover.moveZRelative(vector.getY() * 0.01 * percent * percent);
		}

		private double norm(Point2D vector) {
			return Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int y = e.getY();
			if (y < 0)
				y = 0;
			if (y > getHeight())
				y = getHeight();
			vector.setLocation(e.getX() - (getWidth() / 2), y - (getHeight() / 2));
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
			ThreadUtil.bgRun(thread);
			repaint();
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
						applyMovementZ();
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
				setStop(false);
			}

			public synchronized void setStop(boolean stop) {
				this.stop = stop;
			}
		}
	}
}

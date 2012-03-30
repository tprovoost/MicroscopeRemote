package plugins.tprovoost.Microscopy.MicroscopeRemote.gui;

import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.util.LookAndFeelUtil;
import icy.system.thread.ThreadUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.vecmath.Point3d;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.StageMover;

public class MemoryButton extends JButton implements MouseListener {

	/** Default serial UID */
	private static final long serialVersionUID = 1L;

	/**
	 * When the button is pressed, the {@link System#nanoTime()} is stored in
	 * this variable.
	 */
	long datePressed = 0;

	// IMAGES FOR DRAWING
	BufferedImage imgMemBtnOn = null;
	BufferedImage imgMemBtnOff = null;

	/** This variable contains the value of the 3D point in this memory button. */
	Point3d memoryButtonPoint;

	CheckButtonRelease cbr = new CheckButtonRelease();

	public MemoryButton(String string) {
		super(string);
		setOpaque(true);
		addMouseListener(this);
	}

	public void setImages(BufferedImage imgMemBtnOn, BufferedImage imgMemBtnOff) {
		this.imgMemBtnOn = imgMemBtnOn;
		this.imgMemBtnOff = imgMemBtnOff;
	}

	private void forgetPoint() {
		memoryButtonPoint = null;
		setSelected(false);
	}

	private void rememberPoint() throws Exception {
		double[] xyz = StageMover.getXYZ();
		if (memoryButtonPoint == null)
			memoryButtonPoint = new Point3d(xyz);
		else
			memoryButtonPoint.set(xyz);
		setSelected(true);
	}

	void gotoPoint() throws Exception {
		if (memoryButtonPoint != null) {
			StageMover.moveXYAbsolute(memoryButtonPoint.x, memoryButtonPoint.y);
			StageMover.moveZAbsolute(memoryButtonPoint.z);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isControlDown()) {
			forgetPoint();
			repaint();
		} else {
			datePressed = System.nanoTime();
			ThreadUtil.bgRun(cbr);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (System.nanoTime() - datePressed < 1500000000L) {
			if (!cbr.stop)
				cbr.setStop(true);
			try {
				gotoPoint();
			} catch (Exception e1) {
				new AnnounceFrame("Error while going to the saved point.");
			}
			repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		boolean selected = isSelected();
		int width = getWidth();
		int height = getHeight();
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// DRAW BUTTON BACKGROUND
		if (imgMemBtnOn == null || imgMemBtnOff == null) {
			Color background;
			Color foreground = LookAndFeelUtil.getForeground(this);
			if (selected) {
				background = LookAndFeelUtil.getActiveColorSheme(this).getSelectionBackgroundColor();
			} else {
				background = LookAndFeelUtil.getBackground(this);
			}
			g2.setColor(background);
			g2.fillRect(0, 0, width, height);
			g2.setColor(foreground);
			g2.drawRect(0, 0, width, height);
		} else {
			if (selected)
				g2.drawImage(imgMemBtnOn, 0, 0, width, height, null);
			else
				g2.drawImage(imgMemBtnOff, 0, 0, width, height, null);
		}
		// DRAW TEXT
		if (selected)
			g2.setColor(Color.black);
		else
			g2.setColor(Color.LIGHT_GRAY);
		g2.setFont(new Font("Arial", Font.BOLD, 20));
		FontMetrics fm = g2.getFontMetrics();
		String toDisplay = getText();
		g2.drawString(toDisplay, width / 2 - fm.charsWidth(toDisplay.toCharArray(), 0, toDisplay.length()) / 2, height / 2 + fm.getHeight() / 3);
		g2.dispose();
	}

	public class CheckButtonRelease implements Runnable {
		private boolean stop;

		public CheckButtonRelease() {
			stop = false;
		}

		@Override
		public void run() {
			setStop(false);
			while (!stop) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				long timeElapsed = System.nanoTime() - datePressed;
				if (timeElapsed >= 1500000000L) {
					try {
						rememberPoint();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					setStop(true);
				}
			}
			setStop(false);
		}

		public synchronized void setStop(boolean stop) {
			this.stop = stop;
		}
	}
}
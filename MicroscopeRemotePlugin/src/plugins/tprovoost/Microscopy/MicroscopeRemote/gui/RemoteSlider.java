package plugins.tprovoost.Microscopy.MicroscopeRemote.gui;

import icy.image.ImageUtil;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JSlider;

public class RemoteSlider extends JSlider implements MouseListener, MouseMotionListener {

	/** */
	private static final long serialVersionUID = 6641870273431549755L;

	BufferedImage sliderKnob;
	Point draggedPoint = null;

	public RemoteSlider(int i, int j, int k) {
		super(i, j, k);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setSliderKnob(BufferedImage sliderKnob) {
		this.sliderKnob = sliderKnob;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		int width = getWidth();
		int height = getHeight();

		if (sliderKnob == null) {
			int heightKnob = 10;
			g2.setColor(new Color(255, 255, 255, 0));
			g2.fillRect(0, 0, width, height);
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRect(((getValue() - 1) * width / getMaximum()), height / 2 - heightKnob / 2, heightKnob * 2, heightKnob);
		} else {
			g2.setColor(new Color(39, 39, 39));
			g2.fillRect(0, 0, width, height);
			g2.setColor(new Color(0, 0, 0));
			g2.fillRect(5, height / 2 - 4, width - 10, 8);
			int heightKnob = (int) (sliderKnob.getHeight(null) / 1.5);
			if (draggedPoint == null)
				g2.drawImage(ImageUtil.scaleImage(sliderKnob, heightKnob * 2, heightKnob), null, ((getValue() - 1) * width / getMaximum()), height / 2 - heightKnob / 2);
			else
				g2.drawImage(ImageUtil.scaleImage(sliderKnob, heightKnob * 2, heightKnob), null, draggedPoint.x - heightKnob, height / 2 - heightKnob / 2);
		}
		g2.dispose();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int w = getWidth();
		int max = getMaximum();
		draggedPoint = e.getPoint();
		Point locationEvent = e.getLocationOnScreen();
		Point currentLocation = getLocationOnScreen();
		if (locationEvent.x <= currentLocation.x + 20) {
			setValue(1);
			draggedPoint = new Point(20, 0);
		} else if (locationEvent.x >= currentLocation.x + w - 20) {
			setValue(getMaximum());
			draggedPoint = new Point(w - 20, 0);
		}
		int value = (int) (max - (((double) currentLocation.x + w - locationEvent.x) / (w / max))) + 1;
		setValue(value);
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		draggedPoint = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
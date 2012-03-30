package plugins.tprovoost.Microscopy.MicroscopeRemote.gui;

import icy.image.ImageUtil;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JSlider;

public class RemoteSlider extends JSlider {

	/** */
	private static final long serialVersionUID = 6641870273431549755L;

	BufferedImage sliderKnob;

	public RemoteSlider(int i, int j, int k) {
		super(i, j, k);
	}

	public void setSliderKnob(BufferedImage sliderKnob) {
		this.sliderKnob = sliderKnob;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		int width = getWidth();
		int height = getHeight();

		if (sliderKnob == null) {
			int heightKnob = 10;
			g2.setColor(new Color(255, 255, 255,0));
//			g2.setColor(new Color(0, 0, 0));
			g2.fillRect(0, 0, width, height);
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRect(((getValue() - 1) * width / getMaximum()), height / 2 - heightKnob / 2, heightKnob * 2, heightKnob);
		} else {
			int heightKnob = sliderKnob.getHeight(null);
			g2.drawImage(ImageUtil.scaleImage(sliderKnob,  heightKnob * 2, heightKnob), null, ((getValue() - 1) * width / getMaximum()), height / 2 - heightKnob / 2);
		}
		g2.dispose();
	}
}
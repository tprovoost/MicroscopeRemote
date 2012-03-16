package plugins.tprovoost.Microscopy.MicroscopeRemote.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JSlider;

public class RemoteSlider extends JSlider {

	/** */
	private static final long serialVersionUID = 6641870273431549755L;

	Image sliderKnob;

	public RemoteSlider(int i, int j, int k) {
		super(i, j, k);
	}

	public void setSliderKnob(Image sliderKnob) {
		this.sliderKnob = sliderKnob;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int width = getWidth();
		int height = getHeight();
		int heightKnob = 10;

		if (sliderKnob == null) {
			g2.setColor(new Color(255, 255, 255, 0));
			g2.fillRect(0, 0, width, height);
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRect(((getValue() - 1) * width / getMaximum()), height / 2 - heightKnob / 2, heightKnob * 2, heightKnob);
		} else {
			g2.drawImage(sliderKnob, ((getValue() - 1) * width / getMaximum()), height / 2 - sliderKnob.getHeight(null) / 2, null);
		}
	}
}
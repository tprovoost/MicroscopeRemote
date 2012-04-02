package plugins.tprovoost.Microscopy.MicroscopeRemote.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;

public class InverterCheckBox extends JCheckBox {

	/** */
	private static final long serialVersionUID = 4893333197103321565L;

	private static int knobLength = 40;
	private static int knobHeight = 20;
	private static int buttonSize = 20;

	// IMAGES
	BufferedImage imgBtnInvertOn;
	BufferedImage imgBtnInvertOff;
	BufferedImage imgLightOn;
	BufferedImage imgLightOff;

	public InverterCheckBox(String string) {
		super(string);
	}

	public void setImages(BufferedImage imgBtnInvertOn, BufferedImage imgBtnInvertOff, BufferedImage imgLightOn, BufferedImage imgLightOff) {
		this.imgBtnInvertOn = imgBtnInvertOn;
		this.imgBtnInvertOff = imgBtnInvertOff;
		this.imgLightOn = imgLightOn;
		this.imgLightOff = imgLightOff;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		boolean selected = isSelected();
		String text = getText();
		Font f = new Font("Arial", Font.BOLD, 16);
		int actualWidthIdx = 30;
		int height = getHeight();

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(f);
		FontMetrics fm = g2.getFontMetrics();
		g2.setColor(new Color(39, 39, 39, 255));
		g2.fillRect(0, 0, getWidth(), height);
		g2.setColor(Color.lightGray);

		// Draw the knob
		if (selected) {
			if (imgBtnInvertOn == null)
				g2.drawLine(actualWidthIdx, getHeight() / 2, actualWidthIdx + knobLength, getHeight() / 2);
			else {
				g2.drawImage(imgBtnInvertOn, actualWidthIdx - 20, height / 2 - knobHeight / 2, knobLength, knobHeight, null);
			}
		} else {
			if (imgBtnInvertOff == null)
				g2.drawLine(actualWidthIdx, getHeight() / 2, actualWidthIdx - 20, getHeight() / 2);
			else {
				g2.drawImage(imgBtnInvertOff, actualWidthIdx - 20, height / 2 - knobHeight / 2, knobLength, knobHeight, null);
			}
		}
		actualWidthIdx += 20;

		// draw the text
		g2.drawString(text, actualWidthIdx += 20, height / 2 + fm.getHeight() / 2);
		actualWidthIdx += 120;

		// draw the light
		if (selected) {
			if (imgLightOn == null) {
				g2.setColor(Color.red);
				g2.fillOval(actualWidthIdx + 20, height / 2 - buttonSize / 2, buttonSize, buttonSize);
			} else {
				g2.drawImage(imgLightOn, actualWidthIdx + 20, height / 2 - buttonSize / 2, buttonSize,buttonSize, null);
			}
		} else {
			if (imgLightOff == null) {
				g2.setColor(Color.lightGray);
				g2.fillOval(actualWidthIdx += 20, height / 2 - buttonSize / 2, buttonSize, buttonSize);
			} else {
				g2.drawImage(imgLightOff, actualWidthIdx + 20, height / 2 - buttonSize / 2, buttonSize,buttonSize, null);
			}
		}
		g2.dispose();
	}
}
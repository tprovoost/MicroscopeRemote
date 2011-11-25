package plugins.tprovoost.Microscopy.MicroscopeRemote;

import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;

import org.micromanager.utils.StateItem;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopePlugin;

public class MicroscopeRemotePlugin extends MicroscopePlugin {

	/** Reference to the frame of the plugin.*/
	private RemoteFrame mainFrame;
	
	@Override
	public void start() {
		// Creation of the frame.
		mainFrame = new RemoteFrame(this);

		// add the plugin
		mainGui.addPlugin(this);
		
		// Add a listener on the frame : when the frame is closed
		// the plugin is removed from the GUI plugin
		mainFrame.addFrameListener(new IcyFrameAdapter() {
			@Override
			public void icyFrameClosed(IcyFrameEvent e) {
				super.icyFrameClosed(e);
				mainGui.removePlugin(MicroscopeRemotePlugin.this);
			}
		});
	}
	
	@Override
	public void notifyConfigAboutToChange(StateItem item) {
	}

	@Override
	public void notifyConfigChanged(StateItem item) {
	}

	@Override
	public void MainGUIClosed() {
		mainFrame.setEnable(false);
	}
}

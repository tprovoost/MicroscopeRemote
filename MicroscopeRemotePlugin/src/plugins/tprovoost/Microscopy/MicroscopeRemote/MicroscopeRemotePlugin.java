package plugins.tprovoost.Microscopy.MicroscopeRemote;

import icy.gui.dialog.MessageDialog;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginImageAnalysis;

public class MicroscopeRemotePlugin extends Plugin implements PluginImageAnalysis {

	@Override
	public void compute() {
		// TODO Auto-generated by Icy4Eclipse
		MessageDialog.showDialog("MicroscopeRemotePlugin is working fine !");
	}

}
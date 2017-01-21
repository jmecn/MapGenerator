package net.jmecn.mabi;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.audio.AudioListenerState;
import com.jme3.light.AmbientLight;

import net.jmecn.mabi.plugin.AniLoader;
import net.jmecn.mabi.plugin.FrmLoader;
import net.jmecn.mabi.plugin.PmgLoader;

public class MabiPE extends SimpleApplication {

	public MabiPE() {
		super(new StatsAppState(), new FlyCamAppState(), new AudioListenerState(), new DebugKeysAppState(),
				new ModelState());
	}

	@Override
	public void simpleInitApp() {
		// Mabinogi plugin
		assetManager.registerLoader(FrmLoader.class, "frm");
		assetManager.registerLoader(PmgLoader.class, "pmg");
		assetManager.registerLoader(AniLoader.class, "ani");
		
		rootNode.addLight(new AmbientLight());
		
		flyCam.setMoveSpeed(10f);
	}

	public static void main(String[] args) {
		MabiPE app = new MabiPE();
		app.start();
	}

}

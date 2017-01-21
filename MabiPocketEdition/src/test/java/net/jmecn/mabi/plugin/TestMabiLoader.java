package net.jmecn.mabi.plugin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.shader.plugins.GLSLLoader;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.texture.plugins.DDSLoader;
import com.jme3.texture.plugins.HDRLoader;
import com.jme3.texture.plugins.PFMLoader;
import com.jme3.texture.plugins.TGALoader;

import net.jmecn.mabi.pack.PackFile;
import net.jmecn.mabi.utils.MabiMT;
import net.jmecn.mabi.utils.MT19337_2;
import net.jmecn.mabi.utils.MersenneTwister;

public class TestMabiLoader {

	static Logger logger = LoggerFactory.getLogger(TestMabiLoader.class);
	
	private AssetManager manager;

	@Before
	public void init() {
		manager = new DesktopAssetManager();
		manager.registerLocator("/", ClasspathLocator.class);

		// Material
		manager.registerLoader(J3MLoader.class, "j3m", "j3md");
		manager.registerLoader(GLSLLoader.class, "vert", "frag", "geom", "tsctrl", "tseval", "glsl", "glsllib");

		// Texture
		manager.registerLoader(AWTLoader.class, "jpg", "bmp", "gif", "png", "jpeg");
		manager.registerLoader(DDSLoader.class, "dds");
		manager.registerLoader(HDRLoader.class, "hdr");
		manager.registerLoader(TGALoader.class, "tga");
		manager.registerLoader(PFMLoader.class, "pfm");
	}

	@After
	public void clean() {
	}

	@Test
	public void loadFrm() {
		manager.registerLoader(FrmLoader.class, "frm");
		manager.loadAsset("pet/mesh/fox/pet_c4_maplestory_fox01_s_framework.frm");
		manager.unregisterLoader(FrmLoader.class);
	}

	@Test
	public void loadPmg() {
		manager.registerLoader(PmgLoader.class, "pmg");
		manager.loadAsset("pet/mesh/fox/pet_c4_maplestory_fox01_s_mesh.pmg");
		manager.unregisterLoader(PmgLoader.class);
	}

	@Test
	public void loadAni() {
		manager.registerLoader(AniLoader.class, "ani");
		manager.loadAsset("pet/anim/maplefox/pet_maplefox_s_attack_01.ani");
		manager.loadAsset("pet/anim/maplefox/pet_maplefox_s_walk.ani");
		manager.loadAsset("pet/anim/maplefox/pet_maplefox_s_natural_sit_01.ani");
		manager.loadAsset("pet/anim/maplefox/pet_maplefox_s_stand_friendly.ani");
		manager.unregisterLoader(AniLoader.class);
	}

	@Test
	public void locatePack() {
		String root = "207_to_208.pack";
		
		PackFile packFile = new PackFile(root);
		packFile.extractFile("db/cookingrecipe.xml");
		
		//packFile.extractFile("local/xml/dungeon_ruin.china.txt");
	}
	
	@Test
	public void testMTR() {
		long seed = ( 123 & 0xFFFFFFFFL << 7 ) ^ 0xA9C36DE1L;
		
		MabiMT mt1 = new MabiMT(seed);
		MT19337_2 mt2 = new MT19337_2(seed);
		MersenneTwister mt3 = new MersenneTwister((int) seed);
		for(int i=0; i<100; i++) {
			long a = mt1.rand();
			long b = mt2.genrand_int32();
			long c = mt3.rand();
			
			System.out.printf("%08X %08X %08X\n", a, b, c);
		}
	}
}

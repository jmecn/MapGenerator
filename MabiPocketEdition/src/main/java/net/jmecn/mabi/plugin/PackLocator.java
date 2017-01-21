package net.jmecn.mabi.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;

import net.jmecn.mabi.pack.PackFile;

/**
 * PackLocator is a locator that looks up resources in a .pack file.
 * @author yanmaoyuan
 *
 */
public class PackLocator implements AssetLocator {
	
	static Logger logger = LoggerFactory.getLogger(PackLocator.class);

	PackFile packFile;
	@Override
	public void setRootPath(String rootPath) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("rawtypes")
	@Override
	public AssetInfo locate(AssetManager manager, AssetKey key) {
		String name = key.getName();
        if(name.startsWith("/"))name=name.substring(1);
		return null;
	}

}

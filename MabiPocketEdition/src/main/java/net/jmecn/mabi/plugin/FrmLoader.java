package net.jmecn.mabi.plugin;

import static net.jmecn.mabi.utils.InputStreamUtil.getString;
import static net.jmecn.mabi.utils.InputStreamUtil.readMatrix4f;
import static net.jmecn.mabi.utils.InputStreamUtil.readQuad;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.util.LittleEndien;

import net.jmecn.mabi.struct.FrmBone;
import net.jmecn.mabi.struct.FrmFile;

public class FrmLoader implements AssetLoader {
	
	static Logger logger = LoggerFactory.getLogger(FrmLoader.class);

	@Override
	public Object load(AssetInfo assetInfo) throws IOException {
		LittleEndien in = new LittleEndien(assetInfo.openStream());

		// 文件头
		String head = getString(in, 4);
		int version = in.readShort();

		if (!"pf!".equals(head) || version != 1) {
			logger.warn("不支持的文件格式 head:{}, ver:{}", head, version);
			throw new RuntimeException("Unknown .frm file");
		}
		
		FrmFile frmFile = new FrmFile();
		
		// 骨骼数量
		frmFile.boneCount = in.readShort();
		frmFile.frmBones = new FrmBone[frmFile.boneCount];
		for (int i = 0; i < frmFile.boneCount; i++) {
			FrmBone frmBone = new FrmBone();
			frmFile.frmBones[i] = frmBone;
			
			frmBone.globalToLocal = readMatrix4f(in);
			frmBone.localToGlobal = readMatrix4f(in);
			frmBone.bindPose = readMatrix4f(in);
			frmBone.name = getString(in, 32);
			frmBone.boneid = in.readByte();
			frmBone.parentid = in.readByte();
			frmBone.empty02 = in.readShort();
			frmBone.quad1 = readQuad(in);
			frmBone.quad2 = readQuad(in);
		}
		in.close();
		
		return frmFile;
	}
}

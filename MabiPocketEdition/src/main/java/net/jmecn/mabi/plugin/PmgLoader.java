package net.jmecn.mabi.plugin;

import static net.jmecn.mabi.utils.InputStreamUtil.getString;
import static net.jmecn.mabi.utils.InputStreamUtil.readMatrix4f;
import static net.jmecn.mabi.utils.InputStreamUtil.skip;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.util.LittleEndien;

import net.jmecn.mabi.struct.BoneAssignment;
import net.jmecn.mabi.struct.PmGeometry;
import net.jmecn.mabi.struct.PmgFile;
import net.jmecn.mabi.struct.Skin;
import net.jmecn.mabi.struct.Vertex;

public class PmgLoader implements AssetLoader {

	static Logger logger = LoggerFactory.getLogger(PmgLoader.class);

	enum MeshType {
		None, Hair, Hat, Robe, Chest, Shoes, Gloves, Head, Tool
	}

	MeshType meshType = MeshType.None;

	@Override
	public Object load(AssetInfo info) throws IOException {
		LittleEndien in = new LittleEndien(info.openStream());

		String pmg = getString(in, 4);
		int version = in.readShort();

		if (!"pmg".equals(pmg) && version != 0x0102) {
			logger.warn("不支持的文件格式 head:{} ver:{}", pmg, version);
			throw new RuntimeException("Unknown .pmg file");
		}

		PmgFile file = new PmgFile();
		
		file.headerSize = in.readInt();// head size
		file.modelName = getString(in, 128);
		file.modelType = in.readInt();// type
		in.read(file.stat);
		file.geomCount = in.readInt();

		int count = file.geomCount;
		
		// 读取每个物件所绑定的骨骼
		file.boneAssignments = new BoneAssignment[count];
		for (int i = 0; i < count; i++) {
			file.boneAssignments[i] = readBoneAssignment(in);
		}

		// 计算头部额外需要跳过的字节数。
		int realHeaderSize = 210 + 204 * count;
		int offset = file.headerSize - realHeaderSize;
		if (offset > 0) {
			skip(in, offset);
		}

		// 读取所有物体
		file.geomDats = new PmGeometry[count];
		for (int i = 0; i < count; i++) {
			file.geomDats[i] = readPmObject(in);
		}

		return file;
	}

	/**
	 * 读取每个物体的骨骼绑定数据
	 * @param in
	 * @param boneAssignment
	 * @throws IOException
	 */
	private BoneAssignment readBoneAssignment(LittleEndien in) throws IOException {
		BoneAssignment boneAssignment = new BoneAssignment();
		
		boneAssignment.headLen = in.readInt();
		boneAssignment.bone1 = getString(in, 32);
		boneAssignment.meshName = getString(in, 128);
		boneAssignment.bone2 = getString(in, 32);
		boneAssignment.partNo = in.readInt();
		boneAssignment.unkonwn = in.readInt();
		// offset 204B
		
		return boneAssignment;
	}
	
	/**
	 * 读取每个物体的网格数据
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected PmGeometry readPmObject(LittleEndien in) throws IOException {
		PmGeometry geomData = new PmGeometry();
		
		String pmHead = getString(in, 4);
		int ver = in.readShort();
		//logger.debug("head:{}, ver:{}", pmHead, ver);

		if (!"pm!".equals(pmHead)) {
			logger.error("Not support this file:{}", pmHead);
			throw new IOException("Not support this file");
		}

		if (ver != 0x0002 && ver != 0x0701) {
			logger.error("Unkonwn version:{}", ver);
			throw new IOException("Unkonwn version");
		}

		geomData.size = in.readInt();
		int pos = 6;

		if (ver == 0x0002) {
			geomData.minorMatrix = readMatrix4f(in);
			geomData.majorMatrix = readMatrix4f(in);
			geomData.partNo = in.readInt();
			in.read(geomData.empty8);
			geomData.count = in.readInt();
			in.read(geomData.empty36);
			geomData.faceVertexCount = in.readInt();
			geomData.faceCount = in.readInt();
			geomData.stripFaceVertexCount = in.readInt();
			geomData.stripFaceCount = in.readInt();
			geomData.vertexCount = in.readInt();
			geomData.skinCount = in.readInt();
			in.read(geomData.empty32);
			geomData.f = in.readInt();
			geomData.faceSize = in.readInt();
			geomData.stripFaceSize = in.readInt();
			geomData.meshSize = in.readInt();
			geomData.skinSize = in.readInt();
			in.read(geomData.empty4);

			pos += 264;

			int strLen;
			strLen = in.readInt();
			geomData.bone1 = getString(in, strLen);
			pos += 4 + strLen;
			strLen = in.readInt();
			geomData.meshName = getString(in, strLen);
			pos += 4 + strLen;
			strLen = in.readInt();
			geomData.bone2 = getString(in, strLen);
			pos += 4 + strLen;
			strLen = in.readInt();
			geomData.stats = getString(in, strLen);
			pos += 4 + strLen;
			strLen = in.readInt();
			geomData.normal = getString(in, strLen);
			pos += 4 + strLen;
			strLen = in.readInt();
			geomData.colorMap = getString(in, strLen);
			pos += 4 + strLen;
			strLen = in.readInt();
			geomData.textureName = getString(in, strLen);
			pos += 4 + strLen;

			skip(in, 64);
			pos += 64;
		} else {// 0x0701
			geomData.bone1 = getString(in, 32);
			geomData.meshName = getString(in, 128);
			geomData.bone2 = getString(in, 32);
			geomData.stats = getString(in, 32);
			geomData.normal = getString(in, 32);
			geomData.colorMap = getString(in, 32);
			geomData.minorMatrix = readMatrix4f(in);
			geomData.majorMatrix = readMatrix4f(in);
			geomData.partNo = in.readInt();
			in.read(geomData.empty8);
			geomData.textureName = getString(in, 32);
			geomData.count = in.readInt();
			in.read(geomData.empty36);
			geomData.faceVertexCount = in.readInt();
			geomData.faceCount = in.readInt();
			geomData.stripFaceVertexCount = in.readInt();
			geomData.stripFaceCount = in.readInt();
			geomData.vertexCount = in.readInt();
			geomData.skinCount = in.readInt();
			skip(in, 120);

			pos += 648;
		}

		geomData.malloc();
		
		// 读取网格数据
		for (int i = 0; i < geomData.faceVertexCount; i++) {
			geomData.indexes[i] = in.readShort();
		}

		for (int i = 0; i < geomData.stripFaceVertexCount; i++) {
			geomData.meshSort2[i] = in.readShort();
		}

		// 顶点数据
		Vertex[] verts = new Vertex[geomData.vertexCount];
		geomData.verts = verts;
		for (int i = 0; i < geomData.vertexCount; i++) {
			Vertex vert = new Vertex();
			vert.x = in.readFloat();
			vert.y = in.readFloat();
			vert.z = in.readFloat();

			vert.nx = in.readFloat();
			vert.ny = in.readFloat();
			vert.nz = in.readFloat();

			vert.b = in.readByte();
			vert.g = in.readByte();
			vert.r = in.readByte();
			vert.a = in.readByte();

			vert.u = in.readFloat();
			vert.v = in.readFloat();
			
			verts[i] = vert;
		}

		// 骨骼蒙皮数据
		Skin[] skins = new Skin[geomData.skinCount];
		geomData.skins = skins;
		for (int i = 0; i < geomData.skinCount; i++) {
			Skin skin = new Skin();
			skin.id = in.readInt();
			skin.a = in.readInt();// default 0
			skin.boneWeight = in.readFloat();// default 0.5f
			skin.b = in.readInt();// default 1

			skins[i] = skin;
		}
		
		pos += geomData.faceVertexCount * 2 + geomData.stripFaceVertexCount * 2 + geomData.vertexCount * 36 + geomData.skinCount * 16;
		int offset = geomData.size - pos;
		if (offset > 0) {
			skip(in, offset);
		}
		
		return geomData;
	}



}

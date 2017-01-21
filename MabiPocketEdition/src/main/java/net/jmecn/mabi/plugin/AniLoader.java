package net.jmecn.mabi.plugin;

import static net.jmecn.mabi.utils.InputStreamUtil.getString;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.util.LittleEndien;

import net.jmecn.mabi.struct.AniFile;
import net.jmecn.mabi.struct.AniFrame;
import net.jmecn.mabi.struct.AniTrack;

/**
 * Mabinogi动画加载器
 * 
 * @author yanmaoyuan
 *
 */
public class AniLoader implements AssetLoader {

	static Logger logger = LoggerFactory.getLogger(AniLoader.class);

	@Override
	public Object load(AssetInfo assetInfo) throws IOException {
		LittleEndien in = new LittleEndien(assetInfo.openStream());

		// 文件头
		String head = getString(in, 4);
		int version = in.readInt();

		if (!"pa!".equals(head) || version != 0x0301) {
			logger.warn("不支持的文件格式 head:{} ver:{}", head, version);
			throw new RuntimeException("Unknown .ani file");
		}

		AniFile aniFile = new AniFile();

		aniFile.assetPath = assetInfo.getKey().getName();

		aniFile.maxTickCount = in.readShort();
		aniFile.tickPerSecond = in.readShort();
		aniFile.framePerTick = in.readShort();
		aniFile.framePerSecond = in.readInt();
		aniFile.boneCount = in.readInt();

		// unknown int[4]
		in.readInt();
		in.readInt();
		in.readInt();
		in.readInt();
		// unknown
		in.readInt();
		// unknown int[2]
		in.readInt();
		in.readInt();
		// unknown float[2]
		in.readFloat();
		in.readFloat();

		aniFile.aniTracks = new AniTrack[aniFile.boneCount];
		for (int i = 0; i < aniFile.boneCount; i++) {
			// BoneTrack

			AniTrack track = new AniTrack();
			aniFile.aniTracks[i] = track;

			track.unknown1 = in.readInt();
			track.frameCount = in.readShort();
			track.unknown2 = in.readShort();
			track.maxFrame = in.readInt();
			track.size = in.readInt();
			track.unknown3 = in.readInt();
			track.unknown3 = in.readInt();

			track.aniFrames = new AniFrame[track.frameCount];
			for (int j = 0; j < track.frameCount; j++) {

				AniFrame frame = new AniFrame();
				track.aniFrames[j] = frame;

				frame.frameNo = in.readInt();

				frame.x = in.readFloat();
				frame.y = in.readFloat();
				frame.z = in.readFloat();
				frame.w = in.readFloat();

				frame.qx = in.readFloat();
				frame.qy = in.readFloat();
				frame.qz = in.readFloat();
				frame.qw = in.readFloat();
			}
		}

		// BoneTrack
		in.readInt();// unknown
		// short
		int mDataCount = in.readShort();// 2
		in.readShort();// unknown
		in.readInt();// mTime
		in.readInt();// mSize
		in.readInt();// unknown
		in.readInt();// unknown

		if (mDataCount > 0) {// unknown anim
			for (int j = 0; j < mDataCount; j++) {
				in.readInt();// frameNo

				in.readFloat();// x
				in.readFloat();// y
				in.readFloat();// z
				in.readFloat();// w

				in.readFloat();// qx
				in.readFloat();// qy
				in.readFloat();// qz
				in.readFloat();// qw
			}
		}
		return aniFile;
	}

}

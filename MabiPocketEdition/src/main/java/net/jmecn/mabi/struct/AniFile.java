package net.jmecn.mabi.struct;

public class AniFile {

	public final String key = "pa!";// 4B
	public final int version = 0x0301;// 4B

	/**
	 * <pre>
	 * maxTrackCount定义了动画的最大tick数，通过它可以计算动画的总时长。
	 * 
	 * <code>动画时间(单位：秒) = (float)maxTickCount / tickPerSecond;</code>
	 * 
	 * 每个AniTrack中的maxFrame值都收到maxTrackCount的限制：
	 * 
	 * <code>aniTrack.maxFrame <= maxTickCount * framePerTick;</code>
	 * 
	 * </pre>
	 */
	public int maxTickCount;// 2B
	/**
	 * tps(Ticks per second), default value is 30. 每秒tick数，3dsmax默认值为30。
	 */
	public int tickPerSecond = 30;// 2B
	/**
	 * fpt(Frame per tick), default value is 160. 每个Tick的帧数，3dsmax默认值位160。
	 */
	public int framePerTick = 160;// 2B
	/**
	 * fps(Frame per second) = fpt * tps, default value is 4800.
	 * 每秒帧数，3dsmax默认值为4800。
	 */
	public int framePerSecond = 4800;
	/**
	 * 此动画对应的骨骼数据量。
	 */
	public int boneCount;

	public int[] unknown1 = new int[4];
	public int unknown2;
	public int[] unknown3 = new int[2];
	public float[] unknown4 = new float[2];

	/**
	 * 每个骨骼对应的动画数据
	 */
	public AniTrack[] aniTracks;// = new AniTrack[boneCount];

	/**
	 * 动画资源路径
	 */
	public String assetPath;
	/**
	 * 将文件路径掐头去尾，获得动画名
	 * @return
	 */
	public String getName() {
		int sptr = assetPath.lastIndexOf('/');
		if (sptr < 0 || sptr == assetPath.length() - 1) {
			sptr = 0;
		} else {
			sptr++;
		}
		int ext = assetPath.lastIndexOf(".");
		if (ext < 0 || ext == assetPath.length() - 1) {
			ext = assetPath.length() - 1;
		}

		String name = assetPath.substring(sptr, ext);

		return name;
	}

	/**
	 * 计算动画时长（单位：秒）
	 * @return
	 */
	public float getLength() {
		return (float) maxTickCount / tickPerSecond;
	}
}

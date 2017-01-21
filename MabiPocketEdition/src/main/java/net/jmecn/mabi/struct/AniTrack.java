package net.jmecn.mabi.struct;

/**
 * 每个骨骼对应的动画数据。
 * 
 * @author yanmaoyuan
 *
 */
public class AniTrack {

	public int unknown1;// 4B
	
	/**
	 * 动画的帧数。
	 */
	public int frameCount;// 2B
	public int unknown2;// 2B

	/**
	 * 动画的最大帧数，等于maxTickCount * framePerTick
	 */
	public int maxFrame;// 4B
	/**
	 * 动画数据的字节数，等于frameCount * 36。
	 */
	public int size;// 4B

	public int unknown3;// 4B
	public int unknown4;// 4B
	
	/**
	 * 关键帧数据
	 */
	public AniFrame[] aniFrames;// = new AniFrame[frameCount];
}

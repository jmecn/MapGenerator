package net.jmecn.mabi.struct;

/**
 * 动画的关键帧
 * 
 * @author yanmaoyuan
 *
 */
public class AniFrame {
	/**
	 * 帧号，根据frameNo可以计算这一帧播放的时间。
	 */
	public int frameNo;
	
	/**
	 * 位移 Translation
	 */
	public float x, y, z, w;
	
	/**
	 * 旋转 Rotation
	 */
	public float qx, qy, qz, qw;
}

package net.jmecn.mabi.struct;

import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;

public class FrmBone {
	public Matrix4f globalToLocal;// 64B
	public Matrix4f localToGlobal;// 64B
	public Matrix4f bindPose;// 64B
	public String name;// 32
	public byte boneid;
	public byte parentid;
	public int empty02;// 0x00*2
	
	public Quaternion quad1;
	public Quaternion quad2;
	
	/**
	 * 骨骼名称中经常会多一些下划线、横线，而pmg文件中记录的骨骼名字是不带这些符号的。
	 * @return
	 */
	public String getSimpleName() {
		String str = name;
		int idx = str.lastIndexOf("_");
		if (idx >= 0) {
			str = str.substring(idx + 1);
		}
		idx = str.lastIndexOf("-");
		if (idx >= 0) {
			str = str.substring(idx + 1);
		}
		
		return str;
	}
}

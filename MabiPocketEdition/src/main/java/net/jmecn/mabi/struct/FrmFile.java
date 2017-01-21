package net.jmecn.mabi.struct;

public class FrmFile {
	public final String key = "pf!";// 4B
	public final int version = 0x0001;// 2B
	
	public int boneCount;// 4B
	
	public FrmBone[] frmBones;// = new FrmBone[boneCount];
}

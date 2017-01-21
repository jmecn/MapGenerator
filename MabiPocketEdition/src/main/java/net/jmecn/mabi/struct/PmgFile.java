package net.jmecn.mabi.struct;

public class PmgFile {
	// FILE HEADER
	public final String key = "pmg";// 4B
	public final int version = 0x0102;// 2B
	public int headerSize;// 4B
	public String modelName;// 128B
	public int modelType;// 4B
	public byte[] stat = new byte[64];// 64B
	public int geomCount;// 4B
	
	public BoneAssignment[] boneAssignments;// geomCount * 204
	public PmGeometry[] geomDats;
}
package net.jmecn.mabi.struct;

import com.jme3.math.Matrix4f;

/**
 * 
 * @author yanmaoyuan
 *
 */
public class PmGeometry {
	String key = "pm!";// 4B
	int version = 0x0002;// 2B

	public int size;
	public Matrix4f minorMatrix;
	public Matrix4f majorMatrix;
	public int partNo;
	public byte[] empty8 = new byte[8];// 0x00*8
	public int count;
	public byte[] empty36 = new byte[36];
	public int faceVertexCount;
	public int faceCount;
	public int stripFaceVertexCount;
	public int stripFaceCount;
	public int vertexCount;
	public int skinCount;
	public byte[] empty32 = new byte[32];// 0x00*32
	public int f;
	public int faceSize;
	public int stripFaceSize;
	public int meshSize;
	public int skinSize;
	public byte[] empty4 = new byte[4];// 0x00*4
	public String bone1 = "";
	public String meshName = "";
	public String bone2 = "";
	public String stats = "";
	public String normal = "";
	public String colorMap = "";
	public String textureName = "";
	//
	public byte[] unknow;
	public float[] whatMatrix;
	//
	public int[] indexes;
	public int[] meshSort2;
	public Vertex[] verts;
	public Skin[] skins;

	public void malloc() {
		indexes = new int[faceVertexCount];
		meshSort2 = new int[stripFaceVertexCount];
		verts = new Vertex[vertexCount];
		if (skinCount > 0) {
			skins = new Skin[skinCount];
		}
	}
}
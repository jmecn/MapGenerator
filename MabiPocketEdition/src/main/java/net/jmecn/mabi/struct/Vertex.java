package net.jmecn.mabi.struct;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class Vertex {
	public float x, y, z;
	public float nx, ny, nz;
	public byte b, g, r, a;
	public float u, v;

	public Vector3f getPosition() {
		return new Vector3f(x, y, z);
	}

	public Vector3f getNormal() {
		return new Vector3f(nx, ny, nz);
	}

	public ColorRGBA getColor() {
		return new ColorRGBA((r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f);
	}

	public Vector2f getTexCoord() {
		return new Vector2f(u, v);
	}
}
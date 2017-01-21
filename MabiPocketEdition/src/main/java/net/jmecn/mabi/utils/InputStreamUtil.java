package net.jmecn.mabi.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.util.LittleEndien;

public class InputStreamUtil {
	/**
	 * 跳过指定字节数
	 * 
	 * @param in
	 * @param count
	 * @throws IOException
	 */
	public static void skip(InputStream in, long count) throws IOException {
		if (count <= 0)
			return;

		do {
			long skip = in.skip(count);
			if (skip > 0) {
				count -= skip;
			}
		} while (count > 0);
	}

	/**
	 * 变换矩阵
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static Matrix4f readMatrix4f(LittleEndien in) throws IOException {
		float[] matrix = new float[16];
		for (int j = 0; j < 16; j++) {
			matrix[j] = in.readFloat();
		}
		Matrix4f mat4 = new Matrix4f();
		mat4.set(matrix, true);
		return mat4;
	}

	public static Quaternion readQuad(LittleEndien in) throws IOException {
		float x = in.readFloat();
		float y = in.readFloat();
		float z = in.readFloat();
		float w = in.readFloat();
		return new Quaternion(x, y, z, w);
	}
	
	/**
	 * 读取定长字符串
	 * 
	 * @param in
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public static String getString(LittleEndien in, int len) throws IOException {
		if (len <= 0)
			return "";

		byte[] buf = new byte[len];
		in.read(buf);

		int i = 0;
		for (; i < len; i++) {
			if (buf[i] == 0)
				break;
		}

		if (i == 0)
			return "";

		if (i == len)
			return new String(buf);

		byte[] sub = Arrays.copyOf(buf, i);
		return new String(sub);
	}
}

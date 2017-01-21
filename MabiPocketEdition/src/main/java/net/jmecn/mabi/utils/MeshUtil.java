package net.jmecn.mabi.utils;

import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;

public class MeshUtil {
	/**
	 * 计算每个顶点的法向量。
	 * 
	 * @return
	 */
	public static Vector3f[] computeNormals(Vector3f[] vertex, int[] indexes) {
		TempVars tmp = TempVars.get();

		Vector3f A;// 三角形的第1个点
		Vector3f B;// 三角形的第2个点
		Vector3f C;// 三角形的第3个点

		Vector3f vAB = tmp.vect1;
		Vector3f vAC = tmp.vect2;
		Vector3f n = tmp.vect4;

		// Here we allocate all the memory we need to calculate the normals
		int nFace = indexes.length / 3;
		int nVertex = vertex.length;
		Vector3f[] tempNormals = new Vector3f[nFace];
		Vector3f[] normals = new Vector3f[nVertex];

		for (int i = 0; i < nFace; i++) {
			A = vertex[indexes[i*3]];
			B = vertex[indexes[i*3+1]];
			C = vertex[indexes[i*3+2]];

			vAB = B.subtract(A, vAB);
			vAC = C.subtract(A, vAC);
			n = vAB.cross(vAC, n);

			tempNormals[i] = n.normalize();
		}

		Vector3f sum = tmp.vect4;
		int shared = 0;

		for (int i = 0; i < nVertex; i++) {
			// 统计每个点被那些面共用。
			for (int j = 0; j < nFace; j++) {
				if (indexes[j*3] == i || indexes[j*3+1] == i || indexes[j*3+2] == i) {
					sum.addLocal(tempNormals[j]);
					shared++;
				}
			}

			// 求均值
			normals[i] = sum.divideLocal((shared)).normalize();

			sum.zero(); // Reset the sum
			shared = 0; // Reset the shared
		}

		tmp.release();
		return normals;
	}
}

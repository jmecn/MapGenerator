package net.jmecn.mabi.utils;

public class MabiMT {

	private static final int N = 624, M = 397;
	private static final int MATRIXA = 0x9908B0DF, UPPER_MASK = 0x80000000, LOWER_MASK = 0x7fffffff;
	private int[] Mag01 = { 0, MATRIXA };

	private int mti = N + 1;
	private long[] mt = new long[N];

	public MabiMT() {
		init(5489L);
	}

	public MabiMT(final long seed) {
		init(seed);
	}

	public void init(final long seed) {
		long x = seed & 0xFFFFFFFFL;
		mt[0] = x;
		for (mti = 1; mti < N; mti++) {
			x = 1812433253L * (x ^ (x >> 30)) + mti;
			x &= 0xFFFFFFFFL;
			mt[mti] = x;
		}
	}

	public long rand() {
		long y;
		if (mti >= N) {

			if (mti == N + 1) /* if init_genrand() has not been called, */
				init(5489L); /* a default initial seed is used */

			int kk;
			for (kk = 0; kk < N - M; kk++) {
				y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
				mt[kk] = mt[kk + M] ^ (y >> 1) ^ Mag01[(int) (y & 0x1)];
			}

			for (; kk < N - 1; kk++) {
				y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
				mt[kk] = mt[kk + (M - N)] ^ (y >> 1) ^ Mag01[(int) (y & 0x1)];
			}

			y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
			mt[N - 1] = mt[M - 1] ^ (y >> 1) ^ Mag01[(int) (y & 0x1)];

			mti = 0;
		}

		y = mt[mti++];

		y ^= (y >> 11);
		y ^= (y << 7) & 0x9D2C5680L;
		y ^= (y << 15) & 0xEFC60000L;
		y ^= (y >> 18);

		return y;
	}

}

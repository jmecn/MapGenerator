package net.jmecn.mabi.pack;

import static net.jmecn.mabi.utils.InputStreamUtil.getString;
import static net.jmecn.mabi.utils.InputStreamUtil.skip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.util.LittleEndien;

import net.jmecn.mabi.utils.MabiMT;
import net.jmecn.mabi.utils.ZLibUtils;

public class PackFile {

	static Logger logger = LoggerFactory.getLogger(PackFile.class);

	static byte[] ValidHeader = { 0x50, 0x41, 0x43, 0x4B, 0x02, 0x01, 0x00, 0x00 };

	class Header {
		/******
		 * 512Bytes
		 */
		/**
		 * this tells the client if it is a valid pack file
		 */
		final String head = "PACK";
		final int version = 0x0102;
		/**
		 * file version
		 */
		int revision = 0;
		/**
		 * number of entries in the file
		 */
		int entryCount;
		/**
		 * ? some kind of timestamp, doesn't seem to be unix
		 */
		long fileTime1;
		/**
		 * ? some kind of timestamp, doesn't seem to be unix
		 */
		long fileTime2;
		/**
		 * 480 bytes string
		 */
		String dataPath;// = new byte[480];

		/**********************
		 * 32B
		 */
		/**
		 * number of files in the package
		 */
		int fileCount;
		/**
		 * size of the info header in bytes
		 */
		int headerSize;
		/**
		 * a defined blank space size, for appending
		 */
		int blankSize;
		/**
		 * size of the content
		 */
		int contentSize;
		/**
		 * ?
		 */
		byte[] zero;

		Header() {
			zero = new byte[16];
		}
	}

	class PackageEntry {
		byte nameType;
		int nameLen;
		String name;

		int Seed;
		int Zero;
		int Offset;
		int CompressedSize;
		int DecompressedSize;
		int IsCompressed;
		long CreationTime;
		long CreationTime2;
		long LastAccessTime;
		long ModifiedTime;
		long ModifiedTime2;

		// int size = (nameType == 5 ? 5:1) + nameLen + 64;
	}

	private File packFile;
	private boolean fileOpen = false;
	private LittleEndien in;
	private Header header;
	private List<PackageEntry> packageEntries;

	public Map<String, PackageEntry> entryMap;

	public PackFile(String name) {
		this.packageEntries = new ArrayList<PackageEntry>();
		this.entryMap = new HashMap<String, PackageEntry>();
		try {
			openPackage(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean readHeader() {
		if (!fileOpen)
			return false;

		try {
			String header = getString(in, 4);
			int version = in.readInt();

			if (header.equals("PACK") && version == 0x0102) {

				Header h = new Header();

				h.revision = in.readInt();
				h.entryCount = in.readInt();
				h.fileTime1 = in.readLong();
				h.fileTime2 = in.readLong();
				h.dataPath = getString(in, 480);

				h.entryCount = in.readInt();
				h.headerSize = in.readInt();
				h.blankSize = in.readInt();
				h.contentSize = in.readInt();
				in.read(h.zero);

				this.header = h;
				return true;
			} else {
				logger.warn("NOT A VALID PACK FILE");
				return false;
			}
		} catch (IOException e) {
			logger.error("Failed to read file header. {}", e);
			return false;
		}
	}

	private boolean readPackageInfos() {
		if (!fileOpen)
			return false;

		try {
			for (int i = 0; i < header.entryCount; i++) {

				// 读取文件名长度
				int nameLen;
				byte nameType = in.readByte();

				switch (nameType) {
				case 0:// 16 = 0x10
				case 1:// 32 = 0x20
				case 2:// 48 = 0x30
				case 3:// 64 = 0x40
					nameLen = 0x10 * (nameType + 1) - 1;
					break;
				case 4:// 96 = 0x60
					nameLen = 0x60 - 1;
					break;
				case 5:// dyn
					nameLen = in.readInt();
					break;
				default:
					logger.warn("Unknown name type:{}", nameType);
					return false;
				}

				String name = getString(in, nameLen);

				// Change the windows style path separators to unix style
				name = name.replaceAll("\\\\", "/");

				// read PackageItemInfo
				PackageEntry entry = new PackageEntry();

				entry.nameType = nameType;
				entry.nameLen = nameLen;
				entry.name = name;

				entry.Seed = in.readInt();
				entry.Zero = in.readInt();
				entry.Offset = in.readInt();
				entry.CompressedSize = in.readInt();
				entry.DecompressedSize = in.readInt();
				entry.IsCompressed = in.readInt();
				entry.CreationTime = in.readLong();
				entry.CreationTime2 = in.readLong();
				entry.LastAccessTime = in.readLong();
				entry.ModifiedTime = in.readLong();
				entry.ModifiedTime2 = in.readLong();

				if (entry.Zero != 0) {
					logger.debug("Entry {} is corrupted!", name);
				} else {
					packageEntries.add(entry);
					entryMap.put(name, entry);
				}

			}

			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private byte[] extractFile(PackageEntry entry) {
		int fileSize = entry.CompressedSize;
		if (entry.IsCompressed == 0)
			fileSize = entry.DecompressedSize;

		FileInputStream in = null;
		try {
			int start = 512 + 32 + header.headerSize;
			
			in = new FileInputStream(packFile);
			byte[] data = new byte[fileSize];
			skip(in, entry.Offset + start);
			in.read(data, 0, fileSize);
			in.close();
			
			// DecompressdSize to BigEndien
//			int isize = entry.DecompressedSize;
//			data[0] = (byte) ((isize & 0xFF000000) >> 24);
//			data[1] = (byte) ((isize & 0xFF0000) >> 16);
//			data[2] = (byte) ((isize & 0xFF00) >> 8);
//			data[3] = (byte) (isize & 0xFF);

			FileOutputStream out = new FileOutputStream("test1");
			out.write(data);
			out.flush();
			out.close();
			
			long seed = ( entry.Seed & 0xFFFFFFFFL << 7 ) ^ 0xA9C36DE1L;
			MabiMT mt = new MabiMT(seed);
			for (int i = 0; i < fileSize; i++) {
				data[i] = (byte) (data[i] ^ mt.rand());
			}

			out = new FileOutputStream("test2");
			out.write(data);
			out.flush();
			out.close();
			
			if (entry.IsCompressed != 0)
				data = ZLibUtils.decompress(data);
			
			return data;
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private PackageEntry findInternalFile(String filename) {
		// if it contains a *, allow a wildcard search
		if (filename.contains("*")) {
			String[] s = filename.split("*");
			if (s.length == 2) {
				for (PackageEntry entry : packageEntries) {
					if (entry.name.startsWith(s[0]) && entry.name.endsWith(s[1]))
						return entry;
				}
			} else {
				logger.debug("could not use wildcard, invalid count {}", filename);
			}
		} else {
			return entryMap.get(filename);
		}
		return null;
	}

	public boolean fileExists(String filename) {
		return findFile(filename).length() > 0;
	}

	public String findFile(String filename) {
		// if it contains a *, allow a wildcard search
		PackageEntry e = findInternalFile(filename);
		if (e != null)
			return e.name;
		return "";
	}

	public byte[] extractFile(String filename) {
		PackageEntry entry = findInternalFile(filename);
		if (entry != null)
			return extractFile(entry);
		return null;
	}

	public List<String> getFileNames(String beginsWith, String endsWith) {
		if (beginsWith == null)
			beginsWith = "";

		if (endsWith == null) {
			endsWith = "";
		}

		List<String> fileNames = new ArrayList<String>();
		for (PackageEntry entry : packageEntries) {
			if (entry.name.startsWith(beginsWith) && entry.name.endsWith(endsWith))
				fileNames.add(entry.name);
		}
		return fileNames;
	}

	public String findTexture(String texture) {
		for (PackageEntry entry : packageEntries) {
			String entryName = entry.name;
			if (entryName.endsWith(".dds")) {
				String[] names = entryName.split("/");
				String textureName = names[names.length - 1];
				if (textureName == texture + ".dds")
					return entryName;
			}
		}
		return "";
	}

	public boolean openPackage(String filename) throws IOException {
		if (fileOpen)
			return true;

		packFile = new File(filename);
		if (packFile.exists()) {
			if (packFile.canRead()) {
				fileOpen = true;

				in = new LittleEndien(new FileInputStream(packFile));
				if (readHeader()) {
					readPackageInfos();
				}
				in.close();
			}
		}
		return fileOpen;
	}
}

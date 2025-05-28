package backend;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;

public class Filetool {

	static RandomAccessFile open(String fileName, long bytes) {
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(fileName, "rw");
			long length = file.length();
			if( length % bytes != 0 ) {
				System.out.println(fileName + "Data alligned error");
				System.exit(-1);
			}
			System.out.println(length/bytes + " items in " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return file;
	}
	static void truncate(RandomAccessFile file) {
		try {
			if (file != null) {
				file.setLength(0);
				file.seek(0);
			} else {
				System.out.println("You need to open the file before truncating it.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	static void close(RandomAccessFile file) {
		try {
			if (file != null) {
				file.close();
			} else {
				System.out.println("You need to open the file before you close it.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static <T extends ReadOneObject> ArrayList<T> readAllObjects(RandomAccessFile file, Supplier<T> factoryMethod) {
		ArrayList<T> objects = new ArrayList<>();
		try {
			file.seek(0);
			while (file.getFilePointer() < file.length()) {
				T object = factoryMethod.get();
				object.readOneObject();
				objects.add(object);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return objects;
	}

	static String readChars(RandomAccessFile file, int length) {
		char[] chars = new char[length];
		try {
			for (int i = 0; i < length; i++) {
				chars[i] = file.readChar();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String(chars).trim();
	}
	static void writeChars(RandomAccessFile file, String string, int length) {
		try {
			char[] chars = Arrays.copyOf(string.toCharArray(), length);
			for (char c : chars) {
				file.writeChar(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

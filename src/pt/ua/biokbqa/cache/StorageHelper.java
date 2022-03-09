package pt.ua.biokbqa.cache;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class StorageHelper {

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T readFromFile(String filename) throws IOException, ClassNotFoundException {
		T object = null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(filename);
			ois = new ObjectInputStream(fis);
			object = (T) ois.readObject();
		} finally {
			try {
				ois.close();
				fis.close();
			} catch (Exception e) {
			}
		}
		return object;
	}

	public static <T extends Serializable> T readFromFileSavely(String filename) {
		T object = null;
		try {
			object = readFromFile(filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	public static <T extends Serializable> void storeToFile(T object, String filename) throws IOException {
		ObjectOutputStream oout = null;
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(filename);
			oout = new ObjectOutputStream(fout);
			oout.writeObject(object);
		} finally {
			try {
				oout.close();
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static <T extends Serializable> boolean storeToFileSavely(T object, String filename) {
		if (object == null) {
			return false;
		}
		try {
			storeToFile(object, filename);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}

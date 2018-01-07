package br.com.mvbos.indexff;

import java.io.File;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class Util {

	public static final Pattern wordTest = Pattern.compile("[a-zA-Z0-9]");

	public static String normalizeChar(char c) {
		return normalizeChar(String.valueOf(c));
	}

	public static String normalizeChar(String s) {
		return Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}

	public static String parentDir(String absolutePath) {
		if (null == absolutePath) {
			return "";
		}

		return absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
	}

	public static String parentDir(Path path) {
		final File f = path.toFile();
		if (f.isDirectory()) {
			return f.getAbsolutePath();
		}

		return parentDir(f.getAbsolutePath());
	}
}

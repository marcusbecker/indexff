package br.com.mvbos.indexff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Core {

	public static final Path ROOT_DIR = FileSystems.getDefault().getPath(".");
	public static final Path FOLDER_DIR;

	protected static final short IDX_WORDS = 0;
	protected static final short IDX_PHRASES = 1;
	protected static final short IDX_LINES = 2;

	private static final Set<IndexffData> indiffData = new HashSet<>(500);
	// private static final Map<String, Set<String>> globalWordsCache = new
	// HashMap<>(500);

	static {
		final File temp = new File(ROOT_DIR.toFile(), "indexff");
		if (!temp.isDirectory()) {
			temp.mkdir();
		}

		FOLDER_DIR = temp.toPath();
	}

	public static void process(final File f, final IndexffData d) throws DefaultErrorException {

		final Set<String> linesCache = new HashSet<>(50);
		final Set<String> wordsCache = new HashSet<>(50);
		final Set<String> phrasesCache = new HashSet<>(50);

		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = null;

			while ((line = br.readLine()) != null) {

				if (line.trim().isEmpty()) {
					continue;
				}

				char[] arr = line.toCharArray();
				StringBuilder sbLine = new StringBuilder(arr.length);
				StringBuilder sbPhrase = new StringBuilder(40);
				StringBuilder sbWord = new StringBuilder(20);

				for (int i = 0; i < arr.length; i++) {
					if (arr[i] == '\t' || arr[i] == '\r' || arr[i] == '\b') {
						continue;
					}

					if (arr[i] != ' ') {
						sbLine.append(arr[i]);
					}

					if (arr[i] == ' ') {
						if (sbPhrase.length() > 0) {
							phrasesCache.add(sbPhrase.toString());
							sbPhrase.delete(0, sbPhrase.length());
						}

					} else {
						sbPhrase.append(arr[i]);
					}

					final String t = Util.normalizeChar(arr[i]);

					if (!Util.wordTest.matcher(t).matches()) {
						if (sbWord.length() > 0) {
							wordsCache.add(sbWord.toString());
							sbWord.delete(0, sbWord.length());
						}
					} else {
						sbWord.append(t);
					}
				}

				linesCache.add(sbLine.toString());
			}

			br.close();

			/*
			 * for (String s : linesCache) { System.out.println(s); }
			 * 
			 * for (String s : wordsCache) { System.out.println(s); }
			 * 
			 * for (String s : phrasesCache) { System.out.println(s); }
			 */

			// if save
			/*
			 * final Set<String> temp =
			 * globalWordsCache.get(f.getAbsolutePath()); if (temp == null) {
			 * globalWordsCache.put(f.getAbsolutePath(), wordsCache); } else {
			 * temp.addAll(wordsCache);
			 * globalWordsCache.put(f.getAbsolutePath(), temp); }
			 */

			final String filePath = Util.parentDir(f.getAbsolutePath());

			if (d != null) {
				final File folderDir = Core.FOLDER_DIR.toFile();
				d.setLastChange(f.lastModified());
				persisteFile(new File(folderDir, d.getStoredFiles()[Core.IDX_WORDS]), wordsCache);
				persisteFile(new File(folderDir, d.getStoredFiles()[Core.IDX_PHRASES]), phrasesCache);
				persisteFile(new File(folderDir, d.getStoredFiles()[Core.IDX_LINES]), linesCache);

				persist(d);

			} else {

				final IndexffData data = new IndexffData(f.getName(), filePath, f.lastModified());
				final String[] filesArray = new String[3];

				File ff;
				ff = File.createTempFile("words", ".idff", FOLDER_DIR.toFile());
				filesArray[Core.IDX_WORDS] = ff.getName();
				persisteFile(ff, wordsCache);

				ff = File.createTempFile("phrases", ".idff", FOLDER_DIR.toFile());
				filesArray[Core.IDX_PHRASES] = ff.getName();
				persisteFile(ff, phrasesCache);

				ff = File.createTempFile("lines", ".idff", FOLDER_DIR.toFile());
				filesArray[Core.IDX_LINES] = ff.getName();
				persisteFile(ff, linesCache);

				data.setStoredFiles(filesArray);

				persist(data);
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new DefaultErrorException(e.getMessage());
		}

	}

	private synchronized static void persist(IndexffData data) throws IOException {
		indiffData.add(data);
		saveData();
		System.out.println("file saved " + data.getFileName() + " total " + indiffData.size() + " at " + new Date());
		// save update words cache
	}

	public synchronized static void delete(IndexffData data) {
		try {
			final File folderDir = Core.FOLDER_DIR.toFile();
			if (folderDir.exists()) {
				new File(folderDir, data.getStoredFiles()[Core.IDX_WORDS]).deleteOnExit();
				new File(folderDir, data.getStoredFiles()[Core.IDX_PHRASES]).deleteOnExit();
				new File(folderDir, data.getStoredFiles()[Core.IDX_LINES]).deleteOnExit();
			}

			indiffData.remove(data);
			saveData();
			System.out.println("file removed " + data.getFileName() + " total " + indiffData.size() + " at " + new Date());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized static void saveData() throws IOException {
		final FileOutputStream file = new FileOutputStream(new File(FOLDER_DIR.toFile(), "diff.data"));
		final ObjectOutputStream out = new ObjectOutputStream(file);

		out.writeObject(indiffData);

		out.close();
		file.close();
	}

	protected synchronized static boolean loadFile() {
		try {
			unload();

			final File temp = new File(FOLDER_DIR.toFile(), "diff.data");
			if (!temp.exists()) {
				return false;
			}

			final FileInputStream file = new FileInputStream(temp);
			final ObjectInputStream in = new ObjectInputStream(file);

			@SuppressWarnings("unchecked")
			final Set<IndexffData> tempData = (Set<IndexffData>) in.readObject();

			in.close();
			file.close();

			if (tempData != null) {
				indiffData.addAll(tempData);

				return true;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return false;

	}

	private static void persisteFile(File ff, Set<String> ss) throws IOException {
		FileWriter fw = new FileWriter(ff, false);
		for (String s : ss) {
			if (!s.trim().isEmpty()) {
				fw.write(s);
				fw.write('\n');
			}
		}

		fw.close();
	}

	public static Set<IndexffData> getDataFile() {
		final Set<IndexffData> temp = new HashSet<>(indiffData.size());
		temp.addAll(indiffData);
		return temp;
	}

	public static <T extends Object> void debug(Set<T> data) {

		if (data.isEmpty()) {
			System.out.println("erro");
		} else {
			for (Object d : data) {
				System.out.println(d);
			}
		}

	}

	public static void unload() {
		indiffData.clear();
	}

}

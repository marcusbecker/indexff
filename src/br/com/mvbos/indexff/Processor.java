package br.com.mvbos.indexff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.mvbos.indexff.ProcessorBuilder.TextMode;

public class Processor {

	public enum IndexMode {
		NONE, NORMAL, FORCE
	}

	private static Set<String> tempFilesChecked;

	public static void reindex(File dir, IndexMode mode) {
		tempFilesChecked = new HashSet<>(50);
		Set<IndexffData> data = Core.getDataFile();

		if (data.isEmpty()) {
			Core.loadFile();
			data = Core.getDataFile();
		}

		final Map<String, IndexffData> map = new HashMap<>(data.size());
		for (IndexffData i : data) {
			map.put(i.getFullPath(), i);
		}

		//if (IndexMode.NORMAL == mode) {
			reIndex(dir, map);
		//}

		for (String k : map.keySet()) {
			if (!tempFilesChecked.contains(k)) {
				IndexffData temp = map.get(k);
				Core.delete(temp);
			}
		}

		tempFilesChecked.clear();
		tempFilesChecked = null;
	}

	private static void reIndex(final File f, final Map<String, IndexffData> cachedFiles) {
		if (f.isDirectory()) {
			for (File ff : f.listFiles()) {
				reIndex(ff, cachedFiles);
			}

		} else {
			final IndexffData temp = cachedFiles.get(f.getAbsolutePath());
			if (temp == null || f.lastModified() != temp.getLastChange()) {

				if (temp == null) {
					System.out.println("New file " + f.getAbsolutePath());
				} else {
					System.out.print("File modified " + f.getAbsolutePath());
					System.out.println(String.format("cache %s - local %s", new Date(temp.getLastChange()).toString(),
							new Date(f.lastModified()).toString()));
				}

				try {
					Core.process(f, temp);
				} catch (DefaultErrorException e) {
					e.printStackTrace();
				}

			}

			if (temp != null) {
				tempFilesChecked.add(f.getAbsolutePath());
			}
		}
	}

	public static IndexffData[] execute(final ProcessorBuilder b) {
		final Set<IndexffData> temp = b.getData();
		List<IndexffData> files = new ArrayList<>(100);

		final String text = b.getText();
		final int total = temp.size();
		final int fraction = Math.round(total / 4f);
		short perc = 0;
		int progress = fraction;

		for (final IndexffData d : temp) {

			if (progress == 0) {
				perc += 25;
				progress = fraction - 1;
				System.out.println(perc + "% complete.");
			} else {
				progress--;
			}

			if (filterAccepted(b, d)) {
				if (testWord(d, text, b.getTextMode())) {
					files.add(d);
				}
			}
		}

		final IndexffData[] array = files.toArray(new IndexffData[0]);
		files.clear();
		files = null;

		return array;
	}

	private static boolean filterAccepted(ProcessorBuilder b, IndexffData d) {
		if (b.getPath() == Core.FOLDER_DIR) {
			return true;

		} else if (b.getPath() != null && Util.parentDir(b.getPath()).equals(d.getFilePath())) {
			return true;
		}

		if (b.getFile() != null && b.getFile().getName().equals(d.getFileName())) {
			return true;
		}

		return false;
	}

	private static boolean testWord(final IndexffData d, final String text, final TextMode mode) {

		boolean isOK = false;
		final String[] storedFiles = d.getStoredFiles();

		final File file;

		switch (mode) {
		case LINE:
			file = new File(Core.FOLDER_DIR.toFile(), storedFiles[Core.IDX_LINES]);
			break;
		case PHRASE:
			file = new File(Core.FOLDER_DIR.toFile(), storedFiles[Core.IDX_PHRASES]);
			break;
		case WORD:
			file = new File(Core.FOLDER_DIR.toFile(), storedFiles[Core.IDX_WORDS]);
			break;
		default:
			return false;
		}

		try {
			final BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;

			while ((line = br.readLine()) != null) {

				if (line.trim().isEmpty()) {
					continue;
				}

				switch (mode) {
				case LINE:
					isOK = line.contains(text);
					break;
				case PHRASE:
					isOK = line.toLowerCase().contains(text);
					break;
				case WORD:
					isOK = line.equals(text);
					break;
				}

				if (isOK) {
					break;
				}

			}

			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return isOK;
	}

	@SuppressWarnings("unused")
	private static boolean testPhrase(IndexffData d, String text) {

		boolean isOK = false;
		final String[] storedFiles = d.getStoredFiles();

		final File file = new File(Core.FOLDER_DIR.toFile(), storedFiles[Core.IDX_PHRASES]);

		try {
			final BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;

			while ((line = br.readLine()) != null) {

				if (line.trim().isEmpty()) {
					continue;
				}

				if (line.contains(text)) {
					isOK = true;
					break;
				}
			}

			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return isOK;
	}

	private static final ExecutorService eService = Executors.newFixedThreadPool(5);

	public static void cacheEachFile(File f) {
		if (f.exists()) {
			cache(f);
		}

	}

	private static void cache(final File f) {
		if (f.isDirectory()) {
			for (File ff : f.listFiles()) {
				cache(ff);
			}

		} else {

			if (eService == null) {
				try {
					Core.process(f, null);
				} catch (DefaultErrorException e) {
					e.printStackTrace();
				}
			} else {
				eService.execute(new Runnable() {

					@Override
					public void run() {
						try {
							Core.process(f, null);
						} catch (DefaultErrorException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}
}

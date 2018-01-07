package br.com.mvbos.indexff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;

public class TheMain {
	private static final String PREFIX_PHRASE = "% ";
	private static final String PREFIX_LINE = "= ";
	private static final String PREFIX_WORD = "* ";

	/**
	 * unique lines unique words unique phrases
	 * 
	 * 
	 * file [unique lines, unique words, unique phrases]
	 * 
	 * unique words[file1, file2...]
	 * 
	 * */

	public static void main(String[] args) {

		File dir = new File("D:\\");

		// String fileName = "teste.htm";
		// File f = new File(fileName);

		Core.loadFile();

		final Set<IndexffData> data = Core.getDataFile();
		// Core.debug(data);

		final Scanner sc = new Scanner(System.in);

		try {
			System.out.println("Enter your command.");
			while (sc.hasNextLine()) {
				final String ln = sc.nextLine();

				if (ln.trim().isEmpty()) {
					continue;
				}

				if ("exit".equals(ln)) {
					Core.unload();
					break;

				} else if ("go cache".equals(ln)) {
					Processor.cacheEachFile(dir);
					continue;

				} else if ("go reindex".equals(ln)) {
					Processor.reindex(dir, Processor.IndexMode.NORMAL);
					continue;

				} else if ("go help".equals(ln)) {
					System.out.println("* -> word = equals (normalized) | % -> phrase = contains (lowercase) | = -> line = contains");
					continue;
				}

				System.out.println("IndexFileFind started " + new Date());

				final ProcessorBuilder b = new ProcessorBuilder();
				b.data(data);

				/*
				 * | * -> word = equals | % -> phrase = contains (lowercase) | =
				 * -> line = contains
				 */

				if (ln.startsWith(PREFIX_WORD)) {
					b.findSimpleText(ln.substring(PREFIX_WORD.length()));
				} else if (ln.startsWith(PREFIX_PHRASE)) {
					b.findText(ln.substring(PREFIX_PHRASE.length()));
				} else if (ln.startsWith(PREFIX_LINE)) {
					b.containsText(ln.substring(PREFIX_LINE.length()));
				} else {
					b.findSimpleText(ln);
				}

				IndexffData[] files = Processor.execute(b);
				try {
					final File temp = File.createTempFile("output_", ".txt", Core.ROOT_DIR.toFile());
					final FileWriter fw = new FileWriter(temp);

					fw.write("Search by: " + ln + System.lineSeparator());

					for (IndexffData d : files) {
						fw.write(d.getFullPath());
						fw.write(System.lineSeparator());
					}

					fw.close();

					System.out.println("Result in " + temp.getName());

				} catch (IOException e) {
					// e.printStackTrace();
					for (IndexffData d : files) {
						System.out.println(d.getFilePath() + "\\" + d.getFileName());
					}
				}

				files = null;

				System.out.println("IndexFileFind ended " + new Date());
				System.out.println();
			}

		} finally {
			sc.close();
		}

	}
}

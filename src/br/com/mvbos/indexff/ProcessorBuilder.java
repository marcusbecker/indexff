package br.com.mvbos.indexff;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public class ProcessorBuilder {

	enum TextMode {
		WORD, LINE, PHRASE
	}

	private File file;
	private String text;
	private Set<IndexffData> data = Collections.emptySet();
	private Path folder = Core.FOLDER_DIR;
	private TextMode textMode = TextMode.WORD;

	public ProcessorBuilder data(Set<IndexffData> data) {
		this.data = data;
		return this;
	}

	public ProcessorBuilder file(File f) {
		this.file = f;
		return this;
	}

	public ProcessorBuilder findText(String text) {
		this.text = text.toLowerCase();
		this.textMode = TextMode.PHRASE;
		return this;
	}

	public ProcessorBuilder findSimpleText(String text) {
		this.text = Util.normalizeChar(text);
		this.textMode = TextMode.WORD;
		return this;
	}

	public ProcessorBuilder containsText(String text) {
		this.text = text.replaceAll(" ", "");
		this.textMode = TextMode.LINE;
		return this;
	}

	public ProcessorBuilder folder(File dir) {
		this.folder = dir.toPath();
		return this;
	}

	public Set<IndexffData> getData() {
		return data;
	}

	public File getFile() {
		return file;
	}

	public String getText() {
		return text;
	}

	public File getFolder() {
		return folder.toFile();
	}

	public Path getPath() {
		return folder;
	}

	public TextMode getTextMode() {
		return textMode;
	}

}

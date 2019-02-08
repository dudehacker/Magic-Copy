package main;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.JOptionPane;

import osu.beatmap.Beatmap;

public class MagicCopyMania implements Runnable {

	// Variables
	private File targetFile;
	private Beatmap sourceBeatmap;
	private Beatmap targetBeatmap;
	private boolean isKeysound;
	private boolean clearHS;

	// Constructor
	public MagicCopyMania(File input, File output, boolean keysound, boolean clear) throws ParseException, IOException {
		sourceBeatmap = new Beatmap(input);
		targetFile = output;
		targetBeatmap = new Beatmap(output);
		isKeysound = keysound;
		this.clearHS = clear;
	}

	@Override
	public void run() {
		if (clearHS) {
			sourceBeatmap.clearHitsounds();
		}
		targetBeatmap.copyHS(sourceBeatmap, isKeysound);
		try {
			targetBeatmap.exportBeatmap(targetFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(null, "Beatmap exported at " + targetFile.getAbsolutePath());
	}

}

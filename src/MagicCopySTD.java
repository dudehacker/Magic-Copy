import java.io.File;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

public class MagicCopySTD implements Runnable{
	
	//Variables
	private File hitsoundFile;
	private File targetFile;
	private ArrayList<HitObject> List_SourceHS = new ArrayList<>();
	private ArrayList<HitObject> List_TargetHS = new ArrayList<>();
	private String nl = OsuUtils.nl;
	
	// Constructor
	public MagicCopySTD(File input, File output){
		hitsoundFile = input;
		targetFile = output;
	}

	@Override
	public void run() {
		try {
			parseSource();
			System.out.println("Source parsed");
			parseTarget();
			System.out.println("Target parsed");
			exportBeatmap();
			JOptionPane.showMessageDialog(null, "Beatmap exported at " + targetFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private String copyHS(){
		String output="";
		
		return output;
	}
	
	private void parseTarget() throws Exception{
		List_TargetHS = OsuUtils.getListOfHSHitObjects(targetFile);
	}
	
	private void exportBeatmap() throws Exception{
		String[] beatmap = OsuUtils.getAllInfo(targetFile);
		String generalInfo = beatmap[0];
		String hitObjectsInfo = copyHS();
		String[] beatmapSource = OsuUtils.getAllInfo(hitsoundFile);
		String timingInfo = beatmapSource[2];
		
		String outputText = 
				generalInfo + nl + 
		        timingInfo + nl +
		        hitObjectsInfo;
		OsuUtils.exportBeatmap(targetFile, outputText);
	}
	
	private void parseSource() throws Exception{
		List_SourceHS = OsuUtils.getListOfHSHitObjects(hitsoundFile);
		ArrayList<Timing> timingPoints = OsuUtils.getTimingPoints(hitsoundFile);
		List_SourceHS = OsuUtils.setTimingForHitObjects(timingPoints, List_SourceHS);


	}

}

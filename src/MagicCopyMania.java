import java.io.File;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

public class MagicCopyMania implements Runnable{
	
	//Variables
	private File hitsoundFile;
	private File targetFile;
	private boolean isKeysound;
	private boolean clear;
	private ArrayList<HitObject> List_SourceHS = new ArrayList<>();
	private ArrayList<HitObject> List_TargetHS = new ArrayList<>();
	private ArrayList<Sample> List_Samples = new ArrayList<>();
	private ArrayList<Timing> sourceTimingTotal;
	private ArrayList<Timing> targetTiming;
	private String nl = OsuUtils.nl;
	
	// Constructor
	public MagicCopyMania(File input, File output, boolean keysound, boolean clear){
		hitsoundFile = input;
		targetFile = output;
		isKeysound = keysound;
		this.clear = clear;
	}

	@Override
	public void run() {
		try {
			if (clear){
				// Clear all hitsounds except SB
				OsuUtils.clearHitsounds(targetFile);
			}
			//System.exit(0);
			parseSource();
			parseTarget();
			exportBeatmap();
			JOptionPane.showMessageDialog(null, "Beatmap exported at " + targetFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private String copyHS(){
		Collections.sort(List_Samples,Sample.StartTimeComparator);
		String output = "";
		ArrayList<HitObject> outputHOs = new ArrayList<HitObject>();
		ArrayList<Long> list_time = OsuUtils.getDistinctStartTime(List_SourceHS,List_TargetHS);
		for (Long t : list_time){
			
			ArrayList<HitObject> sourceChord = OsuUtils.getChordByTime(List_SourceHS, t);
			ArrayList<HitObject> targetChord = OsuUtils.getChordByTime(List_TargetHS, t);
			int sourceSize = sourceChord.size();
			int targetSize = targetChord.size();
			if (sourceSize == targetSize){
				// CASE 1
				for (int i = 0; i<targetSize;i++){
					HitObject source_ho = sourceChord.get(i);
					HitObject target_ho = targetChord.get(i);
					target_ho.copyHS(source_ho);
					outputHOs.add(target_ho);
				}
			} else if (sourceSize> targetSize){
				// CASE 2
				if (targetSize ==0 ){
					if (isKeysound){
						// keysound = true then copy to SB, else do nothing
						for (int j = 0; j<sourceSize;j++){
							HitObject source_ho = sourceChord.get(j);
							if (source_ho.toSample().toString().contains(".wav")){
								List_Samples.add(source_ho.toSample());
							}
						}
						
					}

				}  else{
					int defaultHitSoundSize = OsuUtils.getDefaultHSChordSizeForTime(sourceChord, t);
					switch (defaultHitSoundSize){
					case 0:
					case 1:
						for (int i = 0; i<targetSize;i++){
							HitObject source_ho = sourceChord.get(i);
							HitObject target_ho = targetChord.get(i);
							target_ho.copyHS(source_ho);
							outputHOs.add(target_ho);
						}
						for (int j = targetSize; j < sourceSize; j++){
							HitObject source_ho = sourceChord.get(j);
							if (source_ho.toSample().toString().contains(".wav")){
								List_Samples.add(source_ho.toSample());
							}
						}
						break;
					
						
					case 2: // Combine both default hitsounds into 1 HitObject
						outputHOs.addAll(combineDefaultHS(sourceChord,targetChord,2));
						break;
						
					case 3:
						if (targetSize > 2){
							outputHOs.addAll(combineDefaultHS(sourceChord,targetChord,2));
						} else {
							outputHOs.addAll(combineDefaultHS(sourceChord,targetChord,3));
						}
						break;
					}
				}
			
			}else{
				// CASE 3 sourceSize < targetSize
				for (int i = 0; i<sourceSize;i++){
					HitObject source_ho = sourceChord.get(i);
					HitObject target_ho = targetChord.get(i);
					target_ho.copyHS(source_ho);
					outputHOs.add(target_ho);
				}
				for (int j = sourceSize; j<targetSize;j++){
					HitObject target_ho = targetChord.get(j);
					outputHOs.add(target_ho);
				}
			}
		}
		Collections.sort(outputHOs, HitObject.StartTimeComparator);
		for (HitObject ho : outputHOs) {
			output += ho.toString() + nl;
		}
		return output;
	}
	
	private void parseTarget() throws Exception{
		List_TargetHS = OsuUtils.getListOfHitObjects(targetFile);
		targetTiming = OsuUtils.getTimingPoints(targetFile);
	}
	
	
	private ArrayList<HitObject> combineDefaultHS(ArrayList<HitObject> sourceChord, ArrayList<HitObject> targetChord, int n){
		
		ArrayList<HitObject> output = new ArrayList<>();
		int sourceSize = sourceChord.size();
		int targetSize = targetChord.size();
		HitObject source_ho1 = sourceChord.get(0);
		HitObject source_ho2 = sourceChord.get(1);
		HitObject newHO = source_ho1.clone();
		if (n==2){
			newHO.addWhistleFinishClap(source_ho2.getWhistleFinishClap());
		} else if (n==3){
			HitObject source_ho3 = sourceChord.get(2);
			newHO.addWhistleFinishClap(source_ho2.getWhistleFinishClap(),source_ho3.getWhistleFinishClap());
		} else {
			throw new IllegalArgumentException();
		}
		HitObject target_ho1 = targetChord.get(0);
		target_ho1 = OsuUtils.copyHS(newHO, target_ho1);
		output.add( target_ho1);
		for (int x = 0; x < n ; x++){
			sourceChord.remove(0);
		}
		targetChord.remove(0);
		// copy rest of hitsounds
		try {
		if (sourceChord.size()>0 && targetChord.size()>=0){
			for (int i = 0; i<targetChord.size();i++){
				HitObject source_ho = sourceChord.get(i);
				HitObject target_ho = targetChord.get(i);
				target_ho.copyHS(source_ho);
				output.add(target_ho);
			}
			for (int j = targetChord.size(); j < sourceChord.size(); j++){
				HitObject source_ho = sourceChord.get(j);
				if (source_ho.toSample().toString().contains(".wav")){
					List_Samples.add(source_ho.toSample());
				}
			}
			
		}
		
		} catch (Exception e){
			System.out.println(n + " targetsize " + targetSize + " source size " + sourceSize);
			e.printStackTrace();
		}
		return output;
	}

	
	@SuppressWarnings("unchecked")
	private void exportBeatmap() throws Exception{
		String[] beatmap = OsuUtils.getAllInfo(targetFile);
		Collections.sort(List_Samples,Sample.StartTimeComparator);
		String generalInfo = beatmap[0];
		String hitObjectsInfo = "[HitObjects]" + nl;
		hitObjectsInfo += copyHS();

		String sampleInfo = OsuUtils.convertALtoString(List_Samples);
		//String[] beatmapSource = OsuUtils.getAllInfo(hitsoundFile);
		
		// only copy useful timing for default hitsounds to target difficulty

		Timing t1, t2;
		for (Timing t : targetTiming){
			for (int i = 0; i < sourceTimingTotal.size(); i++){
				if (i<sourceTimingTotal.size()-1){
					t1 = sourceTimingTotal.get(i);
					t2 = sourceTimingTotal.get(i+1);
					if (t1.getOffset() <= t.getOffset() && t.getOffset() < t2.getOffset()){
						copyTimingHS(t1,t);
						break;
					}
				} else {
					// last timing
					copyTimingHS(sourceTimingTotal.get(i),t);
				}
			}
		}
		// add timings that exist in HS but not in target
		ArrayList<Timing> targetTimingCopy = (ArrayList<Timing>) targetTiming.clone();
		ArrayList<Long> offsets = new ArrayList<>();
		
		for (Timing t_target: targetTimingCopy){
			if (!offsets.contains(t_target.getOffset())){
				offsets.add(t_target.getOffset());
			}
		}
		for (Timing t_source : sourceTimingTotal){
			if (!offsets.contains(t_source.getOffset())){
				Timing t = getTimingFromOffset(t_source.getOffset());
				targetTiming.add(t);
			}
		}
		/*System.out.println("Size of target Timings = " +targetTiming.size());
		for (Timing t : targetTiming) {
			System.out.println(t.toString());
		}*/
		targetTiming.sort(Timing.StartTimeComparator);
		String timingInfo = "[TimingPoints]" + nl + OsuUtils.convertALtoString(targetTiming);
		String outputText = 
				generalInfo + nl + 
				sampleInfo+ nl+ 
		        timingInfo + nl +
		        hitObjectsInfo;
		OsuUtils.exportBeatmap(targetFile, outputText);
	}
	
	private Timing getTimingFromOffset(long offset){
		
		for (Timing t:sourceTimingTotal){
			//System.out.println(t.toString());
			if (t.getOffset() == offset){
				return t;
			}
		}
		System.out.println("offset = "+offset);
		System.out.println("Error in getTimingFromOffset()");
		return null;
	}
	
	private void copyTimingHS(Timing source, Timing target){
		target.setSetID(source.getSetID());
		target.setVolume(source.getVolume());
		target.setSampleSet(source.getSampleSet());
	}
	
	private void parseSource() throws Exception{
		List_SourceHS = OsuUtils.getListOfHSHitObjects(hitsoundFile);
		System.out.println("Hitsound total size = "+List_SourceHS.size());
		sourceTimingTotal = OsuUtils.getTimingPoints(hitsoundFile);
		//List_SourceHS = OsuUtils.setTimingForHitObjects(sourceTimingTotal, List_SourceHS);
		List_Samples = OsuUtils.getSamples(hitsoundFile);

	}

}

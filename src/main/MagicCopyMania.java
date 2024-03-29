package main;

import lombok.extern.slf4j.Slf4j;
import osu.HitObject;
import osu.Sample;
import osu.Timing;
import util.OsuUtils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class MagicCopyMania implements Runnable {

    // Variables
    private final File hitsoundFile;
    private final File targetFile;
    private final boolean isKeysound;
    private final boolean clear;
    private ArrayList<HitObject> List_SourceHS = new ArrayList<>();
    private ArrayList<HitObject> List_TargetHS = new ArrayList<>();
    private ArrayList<Sample> List_Samples = new ArrayList<>();
    private ArrayList<Timing> sourceTimingTotal;
    private ArrayList<Timing> targetTiming;
    private String nl = OsuUtils.nl;

    // Constructor
    public MagicCopyMania(File input, File output, boolean keysound, boolean clear) {
        hitsoundFile = input;
        targetFile = output;
        isKeysound = keysound;
        this.clear = clear;
    }

    private boolean checkOffsetOutdated() throws Exception {
        ArrayList<Timing> sourceT = OsuUtils.getRedTimingPoints(hitsoundFile);
        ArrayList<Timing> targetT = OsuUtils.getRedTimingPoints(targetFile);
        if (sourceT.size() == targetT.size()) {
            for (int i = 0; i < sourceT.size(); i++) {
                Timing t1 = sourceT.get(i);
                Timing t2 = targetT.get(i);
                if (t1.getOffset() != t2.getOffset()) {
                    System.out.println("wrong offset");
                    System.out.println(t1);
                    System.out.println(t2);
                    return false;
                } else {
                    if (t1.getMspb() != t2.getMspb()) {
                        System.out.println("wrong bpm");
                        System.out.println(t1);
                        System.out.println(t2);
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        try {
            if (!checkOffsetOutdated()) {
                JOptionPane.showMessageDialog(null,
                        "Aborted: Please check the offset of red timing on your source difficulty");
            } else {
                if (clear) {
                    // Clear all hitsounds except SB
                    OsuUtils.clearHitsounds(targetFile);
                }
                // System.exit(0);
                parseSource();
                parseTarget();
                exportBeatmap();
                JOptionPane.showMessageDialog(null, "Beatmap exported at " + targetFile.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String copyHS() {
        List_Samples.sort(Sample.StartTimeComparator);
        StringBuilder output = new StringBuilder();
        ArrayList<HitObject> outputHOs = new ArrayList<>();
        Set<Long> list_time = OsuUtils.getDistinctStartTime(List_SourceHS, List_TargetHS);
        log.debug("full target HitObjects {}", List_TargetHS);
        for (Long t : list_time) {

            ArrayList<HitObject> sourceChord = OsuUtils.getChordByTime(List_SourceHS, t);
            log.debug("source chord: {}", sourceChord);
            ArrayList<HitObject> targetChord = OsuUtils.getChordByTime(List_TargetHS, t);
            log.debug("target chord: {}", targetChord);
            int sourceSize = sourceChord.size();
            int targetSize = targetChord.size();
            if (sourceSize == targetSize) {
                // CASE 1
                // System.out.println("same size at " +t);
                for (int i = 0; i < targetSize; i++) {
                    HitObject source_ho = sourceChord.get(i);
                    HitObject target_ho = targetChord.get(i);
                    target_ho.copyHS(source_ho);
                    outputHOs.add(target_ho);
                }
            } else if (sourceSize > targetSize) {
                // CASE 2
                log.debug("sourceSize > targetSize at {}", t);
                if (targetSize == 0) {
                    if (isKeysound) {
                        // keysound = true then copy to SB, else do nothing
                        for (int j = 0; j < sourceSize; j++) {
                            HitObject source_ho = sourceChord.get(j);
                            List_Samples.add(source_ho.toSample());
                        }

                    }

                } else {
                    int defaultHitSoundSize = OsuUtils.getDefaultHSChordSizeForTime(sourceChord, t);
                    log.debug("default HS size at {} is {}", t, defaultHitSoundSize);
                    switch (defaultHitSoundSize) {
                        case 0:
                        case 1:
                            log.debug("source WFC size 0|1 at {}", t);
                            for (int i = 0; i < targetSize; i++) {
                                HitObject source_ho = sourceChord.get(i);
                                HitObject target_ho = targetChord.get(i);
                                target_ho.copyHS(source_ho);
                                outputHOs.add(target_ho);
                            }
                            for (int j = targetSize; j < sourceSize; j++) {
                                HitObject source_ho = sourceChord.get(j);
                                List_Samples.add(source_ho.toSample());
                            }
                            break;

                        case 2: // Combine both default hitsounds into 1 HitObject
                            log.debug("source WFC size 2 at {}", t);
                            outputHOs.addAll(combineDefaultHS(sourceChord, targetChord, 2));
                            break;

                        case 3:
                            log.debug("source WFC size 3 at {}", t);
                            if (targetSize >= 2) {
                                if (sourceSize > defaultHitSoundSize) {
                                    outputHOs.addAll(combineDefaultHS(sourceChord, targetChord, 3));
                                } else {
                                    outputHOs.addAll(combineDefaultHS(sourceChord, targetChord, 2));
                                }
                            } else {
                                outputHOs.addAll(combineDefaultHS(sourceChord, targetChord, 3));
                            }
                            break;
                    }
                }

            } else {
                // CASE 3 sourceSize < targetSize
                // System.out.println("sourceSize < targetSize at " +t);
                for (int i = 0; i < sourceSize; i++) {
                    HitObject source_ho = sourceChord.get(i);
                    HitObject target_ho = targetChord.get(i);
                    target_ho.copyHS(source_ho);
                    outputHOs.add(target_ho);
                }
                for (int j = sourceSize; j < targetSize; j++) {
                    HitObject target_ho = targetChord.get(j);
                    outputHOs.add(target_ho);
                }
            }
        }
        outputHOs.sort(HitObject.StartTimeComparator);
        for (HitObject ho : outputHOs) {
            output.append(ho.toString()).append(nl);
        }
        return output.toString();
    }

    private void parseTarget() throws Exception {
        List_TargetHS = OsuUtils.getListOfHitObjects(targetFile);
        targetTiming = OsuUtils.getTimingPoints(targetFile);
    }

    private ArrayList<HitObject> combineDefaultHS(ArrayList<HitObject> sourceChord, ArrayList<HitObject> targetChord,
                                                  int n) {

        ArrayList<HitObject> output = new ArrayList<>();
        sourceChord.sort(HitObject.AdditionComparator);
        int sourceSize = sourceChord.size();
        int targetSize = targetChord.size();
        HitObject source_ho1 = sourceChord.get(0);
        HitObject source_ho2 = sourceChord.get(1);
        HitObject newHO = source_ho1.clone();
        if (n == 2) {
            if (newHO.getAddition() == source_ho2.getAddition()) {
                newHO.addWhistleFinishClap(source_ho2.getWhistle_finish_clap());

                HitObject target_ho1 = targetChord.get(0);
                target_ho1.copyHS(newHO);
                output.add(target_ho1);
                for (int x = 0; x < n; x++) {
                    sourceChord.remove(0);
                }
                targetChord.remove(0);

            }

        } else if (n == 3) {
            HitObject source_ho3 = sourceChord.get(2);
            if (source_ho3.getAddition() == source_ho2.getAddition()
                    && source_ho2.getAddition() == source_ho1.getAddition()) {
                newHO.addWhistleFinishClap(source_ho2.getWhistle_finish_clap(), source_ho3.getWhistle_finish_clap());

                HitObject target_ho1 = targetChord.get(0);
                target_ho1.copyHS(newHO);
                output.add(target_ho1);
                for (int x = 0; x < n; x++) {
                    sourceChord.remove(0);
                }
                targetChord.remove(0);

            } else if (source_ho1.getAddition() == source_ho2.getAddition()) {
                newHO.addWhistleFinishClap(source_ho2.getWhistle_finish_clap());

                HitObject target_ho1 = targetChord.get(0);
                target_ho1.copyHS(newHO);
                output.add(target_ho1);
                for (int x = 0; x < 2; x++) {
                    sourceChord.remove(0);
                }
                targetChord.remove(0);

            } else if (source_ho2.getAddition() == source_ho3.getAddition()) {
                newHO = source_ho2.clone();
                newHO.addWhistleFinishClap(source_ho3.getWhistle_finish_clap());

                HitObject target_ho1 = targetChord.get(0);
                target_ho1.copyHS(newHO);
                output.add(target_ho1);
                sourceChord.remove(1);
                sourceChord.remove(1);
                targetChord.remove(0);
            }

        } else {
            throw new IllegalArgumentException();
        }

        // copy rest of hitsounds
        try {
            if (!sourceChord.isEmpty() && targetChord.size() >= 0) {
                for (int i = 0; i < targetChord.size(); i++) {
                    HitObject source_ho = sourceChord.get(i);
                    HitObject target_ho = targetChord.get(i);
                    target_ho.copyHS(source_ho);
                    output.add(target_ho);
                }
                for (int j = targetChord.size(); j < sourceChord.size(); j++) {
                    HitObject source_ho = sourceChord.get(j);
                    List_Samples.add(source_ho.toSample());
                }

            }

        } catch (Exception e) {
            System.out.println(n + " targetsize " + targetSize + " source size " + sourceSize);
            e.printStackTrace();
        }
        return output;
    }

    @SuppressWarnings("unchecked")
    private void exportBeatmap() throws Exception {
        String[] beatmap = OsuUtils.getAllInfo(targetFile);
        List_Samples.sort(Sample.StartTimeComparator);
        String generalInfo = beatmap[0];
        String hitObjectsInfo = "[HitObjects]" + nl;
        hitObjectsInfo += copyHS();

        String sampleInfo = OsuUtils.convertALtoString(List_Samples);
        // String[] beatmapSource = OsuUtils.getAllInfo(hitsoundFile);

        // only copy useful timing for default hitsounds to target difficulty

        Timing t1, t2;
        for (Timing t : targetTiming) {
            for (int i = 0; i < sourceTimingTotal.size(); i++) {
                if (i < sourceTimingTotal.size() - 1) {
                    t1 = sourceTimingTotal.get(i);
                    t2 = sourceTimingTotal.get(i + 1);
                    if (t1.getOffset() <= t.getOffset() && t.getOffset() < t2.getOffset()) {
                        copyTimingHS(t1, t);
                        break;
                    }
                } else {
                    // last timing
                    copyTimingHS(sourceTimingTotal.get(i), t);
                }
            }
        }
        System.out.println("Size of target Timings = " + targetTiming.size());
        for
        (Timing t : targetTiming) {
            System.out.println(t.toString());
        }

        // add timings that exist in HS but not in target
        ArrayList<Timing> targetTimingCopy = (ArrayList<Timing>) targetTiming.clone();
        ArrayList<Long> offsets = new ArrayList<>();

        for (Timing t_target : targetTimingCopy) {
            if (!offsets.contains(t_target.getOffset())) {
                offsets.add(t_target.getOffset());
            }
        }

        for (Timing t_source : sourceTimingTotal) {
            if (!offsets.contains(t_source.getOffset())) {
                Timing t = getTimingFromOffset(t_source.getOffset());
                Timing previous = getPreviousTimingFromOffset(targetTiming, t.getOffset());
                if (!t.isSameHS(previous)) {
                    if (previous.isInherited()) {
                        t.setInherited(previous.getInherited());
                        t.setKiai(previous.getKiai());
                        t.setMspb(previous.getMspb());
                    } else {
                        t.setInherited(true);
                        t.setKiai(false);
                        t.setMspb(-100);
                    }
                    targetTiming.add(t);
                }
            }
        }


        targetTiming.sort(Timing.StartTimeComparator);
        String timingInfo = "[TimingPoints]" + nl + OsuUtils.convertALtoString(targetTiming);
        String outputText = generalInfo + nl + sampleInfo + nl + timingInfo + nl + hitObjectsInfo;
        OsuUtils.exportBeatmap(targetFile, outputText);
    }

    private Timing getPreviousTimingFromOffset(List<Timing> list, long offset) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Timing t = list.get(i);
            if (t.getOffset() <= offset) {
                return t;
            }
        }
        System.err.println(offset);
        System.err.println(list);
        return null;
    }

    private Timing getTimingFromOffset(long offset) {

        for (Timing t : sourceTimingTotal) {
            // System.out.println(t.toString());
            if (t.getOffset() == offset) {
                return t;
            }
        }
        System.out.println("offset = " + offset);
        System.out.println("Error in getTimingFromOffset()");
        return null;
    }

    private void copyTimingHS(Timing source, Timing target) {
        target.setSetID(source.getSetID());
        target.setVolume(source.getVolume());
        target.setSampleSet(source.getSampleSet());
    }

    private void parseSource() throws Exception {
        List_SourceHS = OsuUtils.getListOfHSHitObjects(hitsoundFile);
        System.out.println("Hitsound total size = " + List_SourceHS.size());
        sourceTimingTotal = OsuUtils.getTimingPoints(hitsoundFile);
        // List_SourceHS = OsuUtils.setTimingForHitObjects(sourceTimingTotal,
        // List_SourceHS);
        List_Samples = OsuUtils.getSamples(hitsoundFile);

    }

}

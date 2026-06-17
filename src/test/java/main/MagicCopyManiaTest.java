package main;

import org.junit.jupiter.api.Test;
import osu.HitObject;
import util.OsuUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MagicCopyManiaTest {

    private static final File SOURCE = Paths.get("src", "test", "resources",
                    "Sound piercer feat. DAZBEE - Hanatachi ni Kibou o (2020 Remaster) (-[ Peachy ]-) [Karin's Producer Ah Keysounds].osu")
            .toFile();
    private static final File TARGET = Paths.get("src", "test", "resources",
                    "Sound piercer feat. DAZBEE - Hanatachi ni Kibou o (2020 Remaster) (-[ Peachy ]-) [Lightbearer].osu")
            .toFile();

    private static List<HitObject> parseHitObjects(String text) {
        List<HitObject> result = new ArrayList<>();
        for (String line : text.split("\r?\n")) {
            if (line.isBlank()) {
                continue;
            }
            result.add(parseHitObjectLine(line));
        }
        return result;
    }

    private static HitObject parseHitObjectLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Unexpected hit object line: " + line);
        }
        int x = Integer.parseInt(parts[0]);
        long startTime = Long.parseLong(parts[2]);
        int type = Integer.parseInt(parts[3]);
        int whistleFinishClap = Integer.parseInt(parts[4]);
        String tail = parts[5];
        for (int i = 6; i < parts.length; i++) {
            tail += "," + parts[i];
        }

        String[] tailParts = tail.split(":", 5);
        if (type == 128) {
            String[] lnParts = tail.split(":", 6);
            long endLN = Long.parseLong(lnParts[0]);
            int sampleSet = Integer.parseInt(lnParts[1]);
            int addition = Integer.parseInt(lnParts[2]);
            int volume = Integer.parseInt(lnParts[4]);
            String hitSound = lnParts[5];
            HitObject ho = new HitObject(x, startTime, whistleFinishClap, volume, endLN, hitSound);
            ho.setSampleSet(sampleSet);
            ho.setAddition(addition);
            return ho;
        }

        int sampleSet = Integer.parseInt(tailParts[0]);
        int addition = Integer.parseInt(tailParts[1]);
        int volume = Integer.parseInt(tailParts[3]);
        String hitSound = tailParts[4];
        HitObject ho = new HitObject(x, startTime, whistleFinishClap, volume, hitSound);
        ho.setSampleSet(sampleSet);
        ho.setAddition(addition);
        return ho;
    }

    private static void assertHitObjectEqualsExceptHitSound(HitObject actual, HitObject expected) {
        assertEquals(expected.getXposition(), actual.getXposition(), "x position must match");
        assertTrue(actual.isStartTimeAboutEquals(expected.getStartTime()),
                "start time must match within 1 ms");
        assertEquals(expected.getType(), actual.getType(), "hit object type must match");
        assertEquals(expected.getEndLN(), actual.getEndLN(), "end time must match");
    }

    @Test
    void validateNoAddOrRemoveHitObjects() throws Exception {
        assertTrue(SOURCE.exists(), "Source file must exist");
        assertTrue(TARGET.exists(), "Target file must exist");

        MagicCopyMania subject = new MagicCopyMania(SOURCE, TARGET, false, false);

        Method parseSource = MagicCopyMania.class.getDeclaredMethod("parseSource");
        parseSource.setAccessible(true);
        parseSource.invoke(subject);

        Method parseTarget = MagicCopyMania.class.getDeclaredMethod("parseTarget");
        parseTarget.setAccessible(true);
        parseTarget.invoke(subject);

        Method copyHS = MagicCopyMania.class.getDeclaredMethod("copyHS");
        copyHS.setAccessible(true);
        String hitObjectsOutput = (String) copyHS.invoke(subject);

        assertNotNull(hitObjectsOutput, "copyHS must produce hit object output");

        Field targetField = MagicCopyMania.class.getDeclaredField("List_TargetHS");
        targetField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<HitObject> targetHitObjects = (List<HitObject>) targetField.get(subject);

        List<HitObject> outputHitObjects = parseHitObjects(hitObjectsOutput);
        assertEquals(targetHitObjects.size(), outputHitObjects.size(),
                "copyHS output should preserve the target hit object count");

        outputHitObjects.sort(HitObject.StartTimeComparator.thenComparing(HitObject.ColumnComparator));
        targetHitObjects.sort(HitObject.StartTimeComparator.thenComparing(HitObject.ColumnComparator));

        for (int i = 0; i < outputHitObjects.size(); i++) {
            assertHitObjectEqualsExceptHitSound(outputHitObjects.get(i), targetHitObjects.get(i));
        }
    }

    @Test
    void validate1MsGapInSourceChord() throws Exception {
        MagicCopyMania subject = new MagicCopyMania(SOURCE, TARGET, false, false);
        Method parseSource = MagicCopyMania.class.getDeclaredMethod("parseSource");
        parseSource.setAccessible(true);
        parseSource.invoke(subject);
        Method parseTarget = MagicCopyMania.class.getDeclaredMethod("parseTarget");
        parseTarget.setAccessible(true);
        parseTarget.invoke(subject);

        Field sourceField = MagicCopyMania.class.getDeclaredField("List_SourceHS");
        sourceField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<HitObject> sourceHitObjects = (ArrayList<HitObject>) sourceField.get(subject);

        Field targetField = MagicCopyMania.class.getDeclaredField("List_TargetHS");
        targetField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<HitObject> targetHitObjects = (ArrayList<HitObject>) targetField.get(subject);

        long time = 154310L;
        List<HitObject> sourceChord = OsuUtils.getChordByTime(sourceHitObjects, time);
        List<HitObject> targetChord = OsuUtils.getChordByTime(targetHitObjects, time);

        System.out.println("sourceChord size=" + sourceChord.size());
        for (HitObject ho : sourceChord) {
            System.out.println("SRC: " + ho);
        }
        System.out.println("targetChord size=" + targetChord.size());
        for (HitObject ho : targetChord) {
            System.out.println("TGT: " + ho);
        }

        assertEquals(8, sourceChord.size(), "Source chord at 154310 should contain 8 hits from the sample-based source file");
        assertEquals(3, targetChord.size(), "Target chord at 154310 should contain 3 hits in the target difficulty");
    }
}

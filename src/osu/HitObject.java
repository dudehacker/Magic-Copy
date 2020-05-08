package osu;
import java.util.Comparator;

public class HitObject {
	// Instance Variables
	private int type; // 1=circle 128=LN
	private long endLN;
	private int xposition;
	private final int ypos = 192;
	private long startTime;
	private String hitSound;
	private int volume;
	private int column;
	private boolean isDefaultHS;
	private int additionFlags=0;
	private int setID=0;	// X in hit-hitnormalX.wav
	private int additionSampleSet=0;
	private int sampleSet=0;
	private int timingPointSampleSet = 2;
	private int timingPointVolume = 70;
	
	// addition flags
	public final static int HITNORMAL = 0;
	public final static int HITWHISTLE = 2;
	public final static int HITFINISH = 4;
	public final static int HITCLAP = 8;

	// sampleset IDs
	public final static int SAMPLESET_AUTO = 0;
	public final static int SAMPLESET_NORMAL = 1;
	public final static int SAMPLESET_SOFT = 2;
	public final static int SAMPLESET_DRUM = 3;
	
	public final static int AdditionDefault = 0;
	
	//64,192,708,  1,    2,      3        :2       :0    :0: 
	//x, y  ,t,  type,whistle,   sampleset:addition:setID:volume: 
	//                drum-softwhistle.wav          auto :auto:
	
	public HitObject(long t, int col){
		setStartTime(t);
		setColumn(col);
	}
	
	/**
	 * Constructor for a single note
	 * 
	 * @param xpos
	 *            x-position of hit object, max 512
	 * @param t
	 *            start time of hit object
	 * @param vol
	 *            volume from 0 to 100
	 * @param hitSound
	 *            filename of hitsound, in .wav format
	 */
	public HitObject(int xpos, long t, int vol, String hitSound) {
		xposition = xpos;
		setStartTime(t);
		type = 1;
		endLN = 0;
		setVolume(vol);
		this.setHitSound(hitSound);
	}
	
	public HitObject(int xpos, long t,int whistle_finish_clap ,int vol, String hitSound) {
		xposition = xpos;
		setStartTime(t);
		type = 1;
		endLN = 0;
		this.additionFlags=whistle_finish_clap;
		setVolume(vol);
		this.setHitSound(hitSound);
	}

	/**
	 * Constructor for a LN
	 * 
	 * @param xpos
	 *            x-position of hit object, max 512
	 * @param t
	 *            start time of hit object
	 * @param volume
	 *            volume from 0 to 100
	 * @param end
	 *            end time of hit object
	 * @param hitSound
	 *            filename of hitsound, in .wav format
	 */
	public HitObject(int xpos, long t, int volume, long end, String hitSound) {
		type = 128;
		xposition = xpos;
		setStartTime(t);
		endLN = end;
		this.setVolume(volume);
		this.setHitSound(hitSound);
	}

	public HitObject(int xpos, long t,int WFC, int volume, long end, String hitSound) {
		type = 128;
		xposition = xpos;
		setStartTime(t);
		additionFlags = WFC;
		endLN = end;
		this.setVolume(volume);
		this.setHitSound(hitSound);
	}
	
	public void setIsDefaultHS(boolean input){
		isDefaultHS = input;
	}
	
	public boolean getIsDefaultHS(){
		return isDefaultHS;
	}
	
	public int getType() {
		return type;
	}
	
	public int getWhistleFinishClap (){
		return additionFlags;
	}
	

	public void convertColumnIDtoXpos(int KeyCount){
		double columnWidth = 512.0/KeyCount;
		xposition = (int) Math.round(getColumn() * columnWidth) + 10;
	}
	
	public HitObject clearHS(){
		setWhislteFinishClap(0);
		setSampleSet(0);
		setHitSound("");
		setVolume(0);
		return this;
	}

	/**
	 * Check if a custom sample file is in use.
	 */
	public boolean usesCustomSampleFile(){
		/*
		We cannot expect what names the user would use; while it might
		look simple to add a ".ogg" check, in the future someone would come
		with hitsounds in ".oga" or even ".flac" (when lazer replaces stable).
		If the field is empty there is definitely no custom samples, however.
		*/
		return !hitSound.isEmpty();
	}
	
	public void setWhislteFinishClap(int type){
		additionFlags = type;
	}
	
	public int getSetID(){
		return setID;
	}
	
	public void setSetID(int set){
		setID = set;
	}
	
	public HitObject clone(){
		HitObject ho = null;
		if (endLN!=0  && endLN>startTime){
			// LN
			ho = new HitObject(xposition,startTime,additionFlags,volume,endLN,hitSound);
		} else {
			// Short note
			ho =  new HitObject(xposition,startTime,additionFlags,volume,hitSound);
		}
		ho.setSetID(setID);
		ho.setAddition(additionSampleSet);
		ho.setSampleSet(sampleSet);
		ho.setTPSampleSet(timingPointSampleSet);
		ho.setTimingPointVolume(timingPointVolume);
		return ho;
	}
	
	public int getAddition(){
		return additionSampleSet;
	}
	
	public void addWhistleFinishClap(int value){
			setWhislteFinishClap(additionFlags+value);
	}
	
	public void addWhistleFinishClap(int value1, int value2){
		setWhislteFinishClap(additionFlags+value1+value2);
	}
	
	public void setAddition(int v){
		additionSampleSet=v;
	}
	
	public Sample toSample(){
		Sample s;
		if (!usesCustomSampleFile()) {
			String sampleSound = hitSound;
			int effectiveSampleSet;

			if (additionFlags == SAMPLESET_AUTO)
				effectiveSampleSet = timingPointSampleSet;
			else
				effectiveSampleSet = additionSampleSet;

			switch (effectiveSampleSet){
				case SAMPLESET_NORMAL:
					sampleSound = "normal-";
					break;
				
				case SAMPLESET_SOFT:
					sampleSound = "soft-";
					break;
				
				case SAMPLESET_DRUM:
					sampleSound = "drum-";
					break;
			}

			switch (additionFlags){
				case HITNORMAL:
					sampleSound+="hitnormal";
					break;
				case HITWHISTLE:
					sampleSound+="hitwhistle";
					break;
				case HITFINISH:
					sampleSound+="hitfinish";
					break;
				case HITCLAP:
					sampleSound+="hitclap";
					break;
			}

			String id = "";

			if (setID > 1) {
				id += setID;
			}

			// Assume the normal hitsounds are wav files. This is
			// the format in which DH & Co. provide their hitsounds.
			sampleSound += id + ".wav";
			s = new Sample(startTime,sampleSound,timingPointVolume);
			System.out.println(timingPointVolume);
		} else {
			s = new Sample(startTime,hitSound,volume);
		}

		s.addQuotesToHS();
		if (s.gethitSound().isEmpty()){
			System.err.println("Failed to convert HitObject to Sample");
			System.err.println(toString());
			System.err.println(s.toString());
			System.exit(-1);
		}
		return s;
	}

	@Override
	public String toString() {
		if (type==1) {
			// for a single note
			return "" + xposition + "," + ypos + "," + getStartTime() + "," + 1
					+ "," + additionFlags + "," +sampleSet +":" +additionSampleSet+ ":0:" + getVolume() + ":" + getHitSound();
		}
		// for a LN
		return "" + xposition + "," + ypos + "," + getStartTime() + "," + 128 + ","
				+ additionFlags + "," + endLN + ":" +sampleSet +":" +additionSampleSet+ ":0:" + getVolume() + ":" + getHitSound();
	}
	
	public static Comparator<HitObject> StartTimeComparator = new Comparator<HitObject>() {
		@Override
		public int compare(HitObject ho1, HitObject ho2) {
			long t1 = ho1.startTime;
			long t2 = ho2.startTime;
			/* For ascending order */
			return (int) (t1 - t2);
		}
	};

	public static Comparator<HitObject> AdditionComparator = new Comparator<HitObject>() {
		@Override
		public int compare(HitObject ho1, HitObject ho2) {
			long a1 = ho1.additionSampleSet;
			long a2 = ho2.additionSampleSet;
			/* For ascending order */
			return (int) (a1 - a2);
		}
	};

	
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public String getHitSound() {
		return hitSound;
	}

	public void setHitSound(String hitSound) {
		this.hitSound = hitSound;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public int getSampleSet() {
		return sampleSet;
	}

	public void setSampleSet(int sampleSet) {
		this.sampleSet = sampleSet;
	}

	public int getTPSampleSet() {
		return timingPointSampleSet;
	}

	public void setTPSampleSet(int toSampleSet) {
		this.timingPointSampleSet = toSampleSet;
	}

	public int getTimingPointVolume() {
		return timingPointVolume;
	}

	public void setTimingPointVolume(int timingPointVolume) {
		this.timingPointVolume = timingPointVolume;
	}
	
	public void copyHS(HitObject input) {
		setHitSound(input.getHitSound());
		setVolume(input.getVolume());
		setWhislteFinishClap(input.getWhistleFinishClap());
		setSetID(input.getSetID());
		setSampleSet(input.getSampleSet());
		setAddition(input.getAddition());
		setTPSampleSet(input.getTPSampleSet());
	}
	
	

	public static Comparator<HitObject> ColumnComparator = new Comparator<HitObject>() {
		@Override
		public int compare(HitObject n1, HitObject n2) {
			long c1 = n1.xposition;
			long c2 = n2.xposition;
			/* For ascending order */
			return (int) (c1 - c2);
		}
	};
}

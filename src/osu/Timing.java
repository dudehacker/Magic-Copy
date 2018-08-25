package osu;
import java.util.Comparator;


public class Timing {
	// Osu Syntax
	// Offset, Milliseconds per Beat, Meter, Sample Set, Sample Set ID, Volume, Inherited, Kiai Mode

	// offset is time in ms 
	private long offset;
	// mspb is ms per beat, negative if it's non inherited timing point
	private double mspb;
	// meter is beat per measure
	private int meter;
	// st is sample set [1-4] 1 = normal , 2 = soft , 3 = drum
	private int sampleSet;
	// ss is setID [1-infinity], X in "normal-hitnormalX.wav"
	private int setID;
	// volume is volume of hitsounds [0-100]
	private int volume;
	// inherited 1 for true and 0 for false
	private int inherited;
	// kiai 1 for active 0 for inactive
	private int kiai;
	
	// Constructor
	public Timing(long time, float tempo){
		meter = 4;
		setSampleSet(1);
		setSetID(1);	
		setVolume(100);
		inherited = 1;
		kiai = 0;
		setOffset(time);
		setMspb(tempo);
	}
	
	public Timing(long offset, double mspb, int meter, int sampleSet, int setID, int volume, int isInherited, int isKiai){
		this.offset = offset;
		this.mspb=mspb;
		this.meter=meter;
		this.setSampleSet(sampleSet);
		this.setSetID(setID);
		this.setVolume(volume);
		this.inherited = isInherited;
		this.kiai = isKiai;
	}
	
	public Timing(Timing t){
		this.offset = t.offset;
		this.mspb=t.mspb;
		this.meter=t.meter;
		this.setSampleSet(t.getSampleSet());
		this.setSetID(t.getSetID());
		this.setVolume(t.getVolume());
		this.inherited = t.inherited;
		this.kiai = t.kiai;
	}
	// default instructor
	public Timing(){
		meter = 4;
		setSampleSet(1);
		setSetID(1);	
		setVolume(100);
		inherited = 1;
		kiai = 0;
	}
	
	public static Comparator<Timing> StartTimeComparator = new Comparator<Timing>() {
		@Override
		public int compare(Timing tp1, Timing tp2) {
			long t1 = tp1.offset;
			long t2 = tp2.offset;
			/* For ascending order */
			return (int) (t1 - t2);
		}
	};

	
	public Timing clone(){
		Timing t = new Timing(offset, mspb, meter, getSampleSet(), getSetID(), getVolume(), inherited, kiai);
		return t;
	}
	
	public String toString(){
		return "" + offset + "," + mspb + "," + meter + "," + getSampleSet() +"," + getSetID() + "," + getVolume() + "," + inherited + "," + kiai;
	}

	public long getOffset() {
		return offset;
	}

	public int getSampleSet(){
		return sampleSet;
	}
	
	public int getSetID(){
		return setID;
	}
	
	public int getVolume(){
		return volume;
	}
	
	public void setOffset(long offset) {
		this.offset = offset;
	}

	public double getMspb() {
		return mspb;
	}

	public void setMspb(float mspb) {
		this.mspb = mspb;
	}

	public void setSetID(int setID) {
		this.setID = setID;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public void setSampleSet(int sampleSet) {
		this.sampleSet = sampleSet;
	}
	
}

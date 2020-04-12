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
		setInherited(1);
		setKiai(0);
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
		this.setInherited(isInherited);
		this.setKiai(isKiai);
	}
	
	public Timing(Timing t){
		this.offset = t.offset;
		this.mspb=t.mspb;
		this.meter=t.meter;
		this.setSampleSet(t.getSampleSet());
		this.setSetID(t.getSetID());
		this.setVolume(t.getVolume());
		this.setInherited(t.getInherited());
		this.setKiai(t.getKiai());
	}
	// default instructor
	public Timing(){
		meter = 4;
		setSampleSet(1);
		setSetID(1);	
		setVolume(100);
		setInherited(false);
		setKiai(false);
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
		return "" + offset + "," + mspb + "," + meter + "," + getSampleSet() +"," + getSetID() + "," + getVolume() + "," + getInherited() + "," + getKiai();
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

	public void setMspb(double mspb) {
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

	public int getKiai() {
		return kiai;
	}

	public void setKiai(boolean kiai) {
		this.kiai = kiai ? 1 : 0;
	}
	
	public void setKiai(int kiai) {
		this.kiai = kiai;
	}

	public int getInherited() {
		return inherited;
	}
	
	public boolean isInherited() {
		return inherited==0;
	}


	public void setInherited(int inherited) {
		this.inherited = inherited;
	}
	
	public void setInherited(boolean inherited) {
		this.inherited = inherited ? 0 : 1;
	}
	
	public boolean isSameHS(Timing t) {
		if (this.sampleSet!=t.sampleSet) return false;
		if (this.setID!=t.setID) return false;
		if (this.volume!=t.volume) return false;
		return true;
	}
	
}

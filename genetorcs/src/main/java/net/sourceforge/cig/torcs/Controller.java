/*
* This class is a part Computational Intelligence in Games project 
* https://sourceforge.net/projects/cig/    
* and is licensed under GPLv2
*/
package net.sourceforge.cig.torcs;

import pl.edu.amu.wmi.min.torcs.fcl.GearPreference;

public abstract class Controller {

    public void setGearsPreferences(GearPreference gPref) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

	public enum Stage {
		
		WARMUP,QUALIFYING,RACE,UNKNOWN;
		
		static public Stage fromInt(int value)
		{
			switch (value) {
			case 0:
				return WARMUP;
			case 1:
				return QUALIFYING;
			case 2:
				return RACE;
			default:
				return UNKNOWN;
			}			
		}
	};
	
	private Stage stage;
	private String trackName;
	
	public float[] initAngles()	{
		float[] angles = new float[19];
		for (int i = 0; i < 19; ++i)
			angles[i]=-90+i*10;
		return angles;
	}
	
	public Stage getStage() {
		return stage;
	}
		
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public String getTrackName() {
		return trackName;
	}

	public void setTrackName(String trackName) {
		this.trackName = trackName;
	}

    public abstract Action control(SensorModel sensors);

    public abstract void reset(); // called at the beginning of each new trial
    
    public abstract void shutdown();

}
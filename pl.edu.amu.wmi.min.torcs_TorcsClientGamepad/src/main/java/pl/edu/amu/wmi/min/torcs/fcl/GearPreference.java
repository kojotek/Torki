package pl.edu.amu.wmi.min.torcs.fcl;
        
import au.com.bytecode.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sun.security.util.ObjectIdentifier;

public class GearPreference {
    public List<Integer> gearUps = new ArrayList<Integer>();
    public List<Integer> gearDowns = new ArrayList<Integer>();
    public List<Integer> speedUps = new ArrayList<Integer>();
    public List<Integer> speedDowns = new ArrayList<Integer>();
    
    
    public void Load(String file) throws FileNotFoundException, IOException
    {
        try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
            
            List<String[]> all = reader.readAll();
            for (String str: all.get(0)){
                gearUps.add(new Integer(str));
            }
            for (String str: all.get(1)){
                gearDowns.add(new Integer(str));
            }
            
            for (String str: all.get(2)){
                speedUps.add(new Integer(str));
            }
            for (String str: all.get(3)){
                speedDowns.add(new Integer(str));
            }
        }
    }
}

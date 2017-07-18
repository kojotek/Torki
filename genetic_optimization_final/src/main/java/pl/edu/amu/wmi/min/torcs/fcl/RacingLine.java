package pl.edu.amu.wmi.min.torcs.fcl;

import au.com.bytecode.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author admin
 */
public class RacingLine {
    
    public Double[] distanceRaced;;
    public Double[] lineRelativePosition;
    public Double[] speedLimit;
    //int indicesPerMeter;
    private int iterator;
    
    public void Load(String file) throws FileNotFoundException, IOException
    {
        List<Double> distanceRacedList = new ArrayList<Double>();
        List<Double> lineRelativePositionList = new ArrayList<Double>();
        List<Double> speedLimitList = new ArrayList<Double>();
        
        try (CSVReader reader = new CSVReader(new FileReader(file), ',', '\'', 1)) {
            
            List<String[]> data = reader.readAll();
            for (String[] row: data){
                distanceRacedList.add(new Double(row[0]));
                lineRelativePositionList.add(new Double(row[1]));
                speedLimitList.add(new Double(row[2]));
            }
        }
        
        distanceRaced = distanceRacedList.toArray(new Double[0]);
        lineRelativePosition = lineRelativePositionList.toArray(new Double[0]);
        speedLimit = speedLimitList.toArray(new Double[0]);
        //indicesPerMeter = distanceRaced.length / distanceRaced[distanceRaced.length-1].intValue();
        
        iterator = 0;
    }
    
    public Double GetFirstPointAfter(double position){
        
        if(distanceRaced[iterator] < position){
            while(iterator < distanceRaced.length-1 && distanceRaced[iterator] < position){
                iterator++;
            }
            return lineRelativePosition[iterator];
        }
        else{
            while(iterator > 0 && distanceRaced[iterator-1] > position){
                //System.err.println(iterator + ": " + distanceRaced[iterator-1] + " > " + position);
                iterator--;
            }
            return lineRelativePosition[iterator];
        }
    }
    
    public Double GetSpeedLimitAfter(double position){
        
        if(distanceRaced[iterator] < position){
            while(iterator < distanceRaced.length-1 && distanceRaced[iterator] < position){
                iterator++;
            }
            return speedLimit[iterator];
        }
        else{
            while(iterator > 0 && distanceRaced[iterator-1] > position){
                iterator--;
            }
            return speedLimit[iterator];
        }
    }
    
}

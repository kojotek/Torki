package pl.edu.amu.wmi.min.torcs.fcl;

import net.sourceforge.jFuzzyLogic.optimization.ErrorFunction;
import net.sourceforge.jFuzzyLogic.rule.RuleBlock;
import au.com.bytecode.opencsv.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class TorcsErrorFunction extends ErrorFunction{

    List<String[]> entries;
    
    //zmienne
    double inTrackPos[], inAngle[], inTrack6[], inTrack12[], inTrack7[], inTrack11[], inTrack8[], inTrack10[], inTrack9[], speed[];
    double outSteering[], outAccelerate[], outBrake[];
    
    public TorcsErrorFunction(String filePath) throws FileNotFoundException, IOException {
        CSVReader reader = new CSVReader(new FileReader(filePath), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
        entries = reader.readAll();
        reader.close();
        
        int entriesCount = entries.size();
        
        inTrackPos = new double[entriesCount];
        inAngle = new double[entriesCount];
        inTrack6 = new double[entriesCount];
        inTrack12 = new double[entriesCount];
        inTrack7 = new double[entriesCount];
        inTrack11 = new double[entriesCount];
        inTrack8 = new double[entriesCount];
        inTrack10 = new double[entriesCount];
        inTrack9 = new double[entriesCount];
        speed = new double[entriesCount];
        outSteering = new double[entriesCount];
        outAccelerate = new double[entriesCount];
        outBrake = new double[entriesCount];


        for (int i = 0; i < entries.size(); i++){
            String[] entry = entries.get(i);
            inTrackPos[i] = Double.parseDouble(entry[19]);
            inAngle[i] = Double.parseDouble(entry[33]);
            inTrack6[i] = Double.parseDouble(entry[6]);
            inTrack12[i] = Double.parseDouble(entry[12]);
            inTrack7[i] = Double.parseDouble(entry[7]);
            inTrack11[i] = Double.parseDouble(entry[11]);
            inTrack8[i] = Double.parseDouble(entry[8]);
            inTrack10[i] = Double.parseDouble(entry[10]);
            inTrack9[i] = Double.parseDouble(entry[9]);
            speed[i] = Double.parseDouble(entry[21]);
            outSteering[i] = Double.parseDouble(entry[34]);
            outAccelerate[i] = Double.parseDouble(entry[35]);
            outBrake[i] = Double.parseDouble(entry[36]);
        }
    }

    @Override
    public double evaluate(RuleBlock fuzzyRuleSet) {
        double error = 0.0f;

        FunctionBlock fb = fuzzyRuleSet.getFunctionBlock();
        Variable varTrackPos = fb.getVariable("trackPos");
        Variable varAngle = fb.getVariable("angle");
        Variable varTrack6 = fb.getVariable("track6");
        Variable varTrack12 = fb.getVariable("track12");
        Variable varTrack7 = fb.getVariable("track7");
        Variable varTrack11 = fb.getVariable("track11");
        Variable varTrack8 = fb.getVariable("track8");
        Variable varTrack10 = fb.getVariable("track10");
        Variable varTrack9 = fb.getVariable("track9");
        Variable varSpeed = fb.getVariable("speed");

        Variable varSteering = fb.getVariable("steering");
        Variable varAccelerate = fb.getVariable("accelerate");
        Variable varBrake = fb.getVariable("brake");

        for (int sample = 0; sample < entries.size(); sample++) {
            // Set variables 
            varTrackPos.setValue(inTrackPos[sample]);
            varAngle.setValue(inAngle[sample]);
            varTrack6.setValue(inTrack6[sample]); 
            varTrack12.setValue(inTrack12[sample]);
            varTrack7.setValue(inTrack7[sample]); 
            varTrack11.setValue(inTrack11[sample]);
            varTrack8.setValue(inTrack8[sample]); 
            varTrack10.setValue(inTrack10[sample]);
            varTrack9.setValue(inTrack9[sample]); 
            varSpeed.setValue(speed[sample]); 

            // Evaluate FIS 
            fb.evaluate(); 

            // Get output 
            double errorSteering = outSteering[sample] - varSteering.getValue();
            double errorAccelerate = outAccelerate[sample] - varAccelerate.getValue();
            double errorBrake = outBrake[sample] - varBrake.getValue();
            // Accumulate error 
            error += (errorSteering * errorSteering) + (errorAccelerate * errorAccelerate) + (errorBrake * errorBrake);
        } 

        error = Math.cbrt(error); 

        return error; 
    } 
  
    
}
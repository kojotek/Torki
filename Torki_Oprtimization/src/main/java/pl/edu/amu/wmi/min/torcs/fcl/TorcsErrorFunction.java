package pl.edu.amu.wmi.min.torcs.fcl;

import net.sourceforge.jFuzzyLogic.optimization.ErrorFunction;
import net.sourceforge.jFuzzyLogic.rule.RuleBlock;
import au.com.bytecode.opencsv.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class TorcsErrorFunction extends ErrorFunction{

    //List<String[]> entries;
    List<String> params;
    Map<String, double[]> samples;
    //zmienne
    //double inTrackPos[], inAngle[], inTrack6[], inTrack12[], inTrack7[], inTrack11[], inTrack8[], inTrack10[], inTrack9[], speed[];
    //double outSteering[], outAccelerate[], outBrake[];
    
    public TorcsErrorFunction(String filePath, List<String> paramList) throws FileNotFoundException, IOException {
        CSVReader reader = new CSVReader(new FileReader(filePath), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
        List<String[]> entries = reader.readAll();
        reader.close();
        
        params = paramList;
        samples = new HashMap<>();
        
        int entriesCount = entries.size();
        
        samples.put("trackPos", new double[entriesCount]);
        samples.put("angle", new double[entriesCount]);
        samples.put("track6", new double[entriesCount]);
        samples.put("track12", new double[entriesCount]);
        samples.put("track7", new double[entriesCount]);
        samples.put("track11", new double[entriesCount]);
        samples.put("track8", new double[entriesCount]);
        samples.put("track10", new double[entriesCount]);
        samples.put("track9", new double[entriesCount]);
        samples.put("speed", new double[entriesCount]);
        samples.put("steering", new double[entriesCount]);
        samples.put("accelerate", new double[entriesCount]);
        samples.put("brake", new double[entriesCount]);
        samples.put("curvePred0", new double[entriesCount]);
        samples.put("curvePred1", new double[entriesCount]);
        samples.put("curvePred2", new double[entriesCount]);
        samples.put("curvePred3", new double[entriesCount]);
        samples.put("curvePred4", new double[entriesCount]);
        

        for (int i = 0; i < entriesCount; i++){
            String[] entry = entries.get(i);
            samples.get("trackPos")[i]  = Double.parseDouble(entry[28]);
            samples.get("angle")[i]     = Double.parseDouble(entry[67]);
            samples.get("track6")[i]    = Double.parseDouble(entry[6]);
            samples.get("track12")[i]   = Double.parseDouble(entry[12]);
            samples.get("track7")[i]    = Double.parseDouble(entry[7]);
            samples.get("track11")[i]   = Double.parseDouble(entry[11]);
            samples.get("track8")[i]    = Double.parseDouble(entry[8]);
            samples.get("track10")[i]   = Double.parseDouble(entry[10]);
            samples.get("track9")[i]    = Double.parseDouble(entry[9]);
            samples.get("speed")[i]     = Double.parseDouble(entry[29]);
            samples.get("steering")[i]  = Double.parseDouble(entry[68]);
            samples.get("accelerate")[i] = Double.parseDouble(entry[69]);
            samples.get("brake")[i]     = Double.parseDouble(entry[70]);
            samples.get("curvePred0")[i]     = Double.parseDouble(entry[19]);
            samples.get("curvePred1")[i]     = Double.parseDouble(entry[20]);
            samples.get("curvePred2")[i]     = Double.parseDouble(entry[21]);
            samples.get("curvePred3")[i]     = Double.parseDouble(entry[22]);
            samples.get("curvePred4")[i]     = Double.parseDouble(entry[23]);
        }
    }

    @Override
    public double evaluate(RuleBlock fuzzyRuleSet) {
        double error = 0.0f;

        List<Variable> outVariables = new ArrayList<Variable>();
        
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
        
        Variable varCurve0 = fb.getVariable("curvePred0");
        Variable varCurve1 = fb.getVariable("curvePred1");
        Variable varCurve2 = fb.getVariable("curvePred2");
        Variable varCurve3 = fb.getVariable("curvePred3");
        Variable varCurve4 = fb.getVariable("curvePred4");

        for (int i=0; i<params.size(); i++){
            outVariables.add(fb.getVariable(params.get(i)));
        }
        
        //Variable varSteering = fb.getVariable("steering");
        //Variable varAccelerate = fb.getVariable("accelerate");
        //Variable varBrake = fb.getVariable("brake");

        for (int sample = 0; sample < samples.get("speed").length; sample++) {
            // Set variables 
            varTrackPos.setValue(samples.get("trackPos")[sample]);
            varAngle.setValue(samples.get("angle")[sample]);
            varTrack6.setValue(samples.get("track6")[sample]); 
            varTrack12.setValue(samples.get("track12")[sample]);
            varTrack7.setValue(samples.get("track7")[sample]); 
            varTrack11.setValue(samples.get("track11")[sample]);
            varTrack8.setValue(samples.get("track8")[sample]); 
            varTrack10.setValue(samples.get("track10")[sample]);
            varTrack9.setValue(samples.get("track9")[sample]); 
            varSpeed.setValue(samples.get("speed")[sample]); 
            
            varCurve0.setValue(samples.get("curvePred0")[sample]); 
            varCurve1.setValue(samples.get("curvePred1")[sample]); 
            varCurve2.setValue(samples.get("curvePred2")[sample]); 
            varCurve3.setValue(samples.get("curvePred3")[sample]); 
            varCurve4.setValue(samples.get("curvePred4")[sample]); 
            
            // Evaluate FIS 
            fb.evaluate(); 

            double value;
            
            // Get output 
            for (int i=0; i<outVariables.size(); i++){
                value = outVariables.get(i).getValue();
                error += Math.pow((value - samples.get(outVariables.get(i).getName())[sample]), 2.0f);
            }
        }
        
        error = error / samples.get("speed").length;

        return error; 
    } 
  
    
}
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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class TorcsErrorFunction extends ErrorFunction{

    //List<String[]> entries;
    List<String> params;
    String[] args;
    //zmienne
    //double inTrackPos[], inAngle[], inTrack6[], inTrack12[], inTrack7[], inTrack11[], inTrack8[], inTrack10[], inTrack9[], speed[];
    //double outSteering[], outAccelerate[], outBrake[];
    
    public TorcsErrorFunction(String[] args){ 
        this.args = args;
    }

    @Override
    public double evaluate(RuleBlock fuzzyRuleSet) {
        double error = 0.0f;
/*
        List<Variable> outVariables = new ArrayList<Variable>();
        
        FunctionBlock fb = fuzzyRuleSet.getFunctionBlock();
        Variable varTrack9 = fb.getVariable("track9");
        Variable varSpeed = fb.getVariable("speed");
        Variable varCurvePred0 = fb.getVariable("curvePred0");
        Variable varCurvePred1 = fb.getVariable("curvePred1");
        Variable varCurvePred2 = fb.getVariable("curvePred2");
        
        for (int i=0; i<params.size(); i++){
            outVariables.add(fb.getVariable(params.get(i)));
        }
*/
        Client client = new Client();
        
        try {
            error = client.runRace(args, fuzzyRuleSet);
        } catch (IOException ex) {
            Logger.getLogger(TorcsErrorFunction.class.getName()).log(Level.SEVERE, null, ex);
        }
        

        return error; 
    } 
  
    
}
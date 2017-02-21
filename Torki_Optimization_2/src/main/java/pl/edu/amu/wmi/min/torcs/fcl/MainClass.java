/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.amu.wmi.min.torcs.fcl;

import java.util.ArrayList;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.Gpr;
import net.sourceforge.jFuzzyLogic.optimization.OptimizationDeltaJump;
import net.sourceforge.jFuzzyLogic.optimization.OptimizationGradient;
import net.sourceforge.jFuzzyLogic.optimization.Parameter;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.RuleBlock;

public class MainClass {
    
    public static void main(String[] args){
        
        FIS fis = FIS.load(args[0], true);
        if (fis == null) {
            System.err.println("Cannot load FCL driver file: '" + args[0] + "'");
            return;
        }
        
        FunctionBlock functionBlock = fis.getFunctionBlock(null);
        RuleBlock ruleBlock = functionBlock.getFuzzyRuleBlock(null); 
        
        ArrayList<Parameter> parameterList = new ArrayList<Parameter>(); 
        ArrayList<String> errorList = new ArrayList<>();
        
        //parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("track9")));
        //parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("speed")));
        //parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("curvePred0")));
        //parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("curvePred1")));
        parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("curvePred2")));
        
        //for (Rule rule : ruleBlock.getRules()){
        //    parameterList.addAll(Parameter.parametersRuleWeight(rule));
        //}
        
        TorcsErrorFunction errorFunction = new TorcsErrorFunction(args); 
        
        OptimizationGradient optimizationDeltaJump = new OptimizationGradient(ruleBlock, errorFunction, parameterList);
        optimizationDeltaJump.setMaxIterations(10);
        optimizationDeltaJump.setVerbose(true); 
        optimizationDeltaJump.optimize(); 


        Gpr.toFile("fcl_optimized/fcl_optimized.fcl", functionBlock.toString()); 

        functionBlock.reset();
    }
    
}

package pl.edu.amu.wmi.min.torcs.fcl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList; 
import java.util.List;
 
import net.sourceforge.jFuzzyLogic.FIS; 
import net.sourceforge.jFuzzyLogic.FunctionBlock; 
import net.sourceforge.jFuzzyLogic.Gpr; 
import net.sourceforge.jFuzzyLogic.optimization.*; 
import net.sourceforge.jFuzzyLogic.optimization.Parameter; 
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart; 
import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.RuleBlock; 

public class Optimization {
    
    private static String fisFilePath;
    private static String trainingDataFilePath;
    private static int NUM_ITERATIONS = 20;
    
    public static void main(String[] args) throws IOException{   
        
        if (args.length < 3){
            System.out.println("jako argument 1 podaj sterownik fcl");
            System.out.println("jako argument 2 podaj plik z danymi");
            System.out.println("jako argument 3 podaj liczba iteracji");
            System.out.println("jako argument 4 podaj params i liste parametrow do optymalizacji");
            System.out.println("jako argument 5 podaj rules i liste zasad do optymalizacji");
            return;
        }
        
        fisFilePath = args[0];
        trainingDataFilePath = args[1];
        NUM_ITERATIONS = Integer.parseInt(args[2]);

        FIS fis = FIS.load(fisFilePath); 
        FunctionBlock functionBlock = fis.getFunctionBlock(null);
        RuleBlock ruleBlock = functionBlock.getFuzzyRuleBlock(null); 



        ArrayList<Parameter> parameterList = new ArrayList<Parameter>(); 
        
        ArrayList<String> errorList = new ArrayList<>();
        
        boolean params = false;
        boolean rules = false;
        boolean error = false;
        
        for (int i=3; i<args.length; i++){
            System.out.println(args[i]);
            if(args[i].equals("-params")){
                params = true;
                rules = false;
                error = false;
            }
            else if(args[i].equals("-rules")){
                params = false;
                rules = true;
                error = false;
            }
            else if(args[i].equals("-error")){
                params = false;
                rules = false;
                error = true;
            }
            else{
                if (params){
                    parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable(args[i])));
                }
                else if (rules){
                    boolean found = false;
                    for (Rule rule : ruleBlock.getRules()){
                        if (rule.getName().equals(args[i])){
                            found = true;
                            parameterList.addAll(Parameter.parametersRuleWeight(rule)); 
                            break;
                        }
                    }
                    if (!found){
                        System.err.println("Rule " + args[i] + " not found!");
                        return;
                    }
                }
                else if (error){
                    errorList.add(args[i]);
                }
                else{
                    System.err.println("Unknown syntax error.");
                    return;
                }
            }
        }
/*
        parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("track7")));
        parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("track8")));
        parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("track9")));
        parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("track10")));
        parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("track11")));
        //parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("trackPos")));
        //parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("angle")));
        parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("speed")));
        
        parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("steering")));
        //parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("accelerate")));
        //parameterList.addAll(Parameter.parametersMembershipFunction(ruleBlock.getVariable("brake")));


        List<String> ruleNames = new ArrayList<>();

        
        for (int i=0; i < ruleArray.length; i++){
            ruleNames.add(ruleArray[i]);
        }
        
        
        // Add every rule's weight 
        for (Rule rule : ruleBlock.getRules()){
            if (ruleNames.contains(rule.getName())){
                parameterList.addAll(Parameter.parametersRuleWeight(rule)); 
            }
            
        }
        */
        TorcsErrorFunction errorFunction = new TorcsErrorFunction(trainingDataFilePath, errorList); 
        
        OptimizationDeltaJump optimizationDeltaJump = new OptimizationDeltaJump(ruleBlock, errorFunction, parameterList);
        optimizationDeltaJump.setMaxIterations(NUM_ITERATIONS);
        optimizationDeltaJump.setVerbose(true); 
        optimizationDeltaJump.optimize(); 


        Gpr.toFile("fcl_optimized/fcl_optimized.fcl", functionBlock.toString()); 

        functionBlock.reset();
        System.out.println("Optimization finished"); 
    }

}
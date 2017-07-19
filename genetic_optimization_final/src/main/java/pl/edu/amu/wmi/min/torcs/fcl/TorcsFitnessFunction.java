package pl.edu.amu.wmi.min.torcs.fcl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionGaussian;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionPieceWiseLinear;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTriangular;
import net.sourceforge.jFuzzyLogic.membership.Value;
import net.sourceforge.jFuzzyLogic.rule.LinguisticTerm;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.jgap.FitnessFunction;
import org.jgap.IChromosome;


public class TorcsFitnessFunction extends FitnessFunction{

    class TermValue{

        public TermValue(double left, double lMem, double ofst, double rMem, int genNum) {
            this.leftValue = left;
            this.offset = ofst;
            this.leftMembership = lMem;
            this.rightMembership = rMem;
            this.geneNumber = genNum;
        }
        
        double leftValue;
        double offset;
        double leftMembership;
        double rightMembership;
        int geneNumber;
    }
    
    class RuleWeight{

        public RuleWeight(double wght, int genNum) {
            this.weight = wght;
            this.geneNumber = genNum;
        }
        
        double weight;
        int geneNumber;
    }
    
    public volatile double score = 0.0f;
    
    public int steeringAdjustSpeedGeneNum = 0;
    public int accelerationAdjustSpeedGeneNum = 0;
    public int brakeAdjustSpeedGeneNum = 0;
    
    public volatile boolean ready = false;
    public static volatile int counter = 0;
    public FIS fis;
    public FIS referenceFis;
    
    public HashMap<String, HashMap<String,TermValue>> originalVariables;
    public HashMap<String, RuleWeight> ruleWeights;
    public int rulesCount = 0;
    public int genesForTerms = 0;

    
    public TorcsFitnessFunction(FIS workingFis, String fclFile) {
        fis = workingFis;
        referenceFis = FIS.load(fclFile);
        
        originalVariables = new HashMap<>();
        ruleWeights = new HashMap<>();
        
        int counter = 0;
        
        for (Map.Entry<String, Variable> entry : referenceFis.getFunctionBlock("fb").getVariables().entrySet()) {
            
            String key = entry.getKey();
            Variable value = entry.getValue();
            
            if (value.isInput()){
                originalVariables.put(key, new HashMap<>());
                for (Map.Entry<String, LinguisticTerm> entry1 : value.getLinguisticTerms().entrySet()) {
                    String key1 = entry1.getKey();
                    LinguisticTerm value1 = entry1.getValue();
                    
                    double a = value1.getMembershipFunction().getParameter(0);
                    double b = value1.getMembershipFunction().getParameter(1);
                    double c = value1.getMembershipFunction().getParameter(2) - a;
                    double d = value1.getMembershipFunction().getParameter(3);
                    originalVariables.get(key).put(key1, new TermValue(a,b,c,d, counter));
                    counter += 2;
                    genesForTerms = counter;
                }
            }
        }

        for (Rule entry : referenceFis.getFunctionBlock("fb").getFuzzyRuleBlock("No1").getRules()) {
            String key = entry.getName();
            Double value = entry.getWeight();
            ruleWeights.put(key, new RuleWeight(value, genesForTerms +  rulesCount));
            rulesCount++;
            counter++;
        }
        
        steeringAdjustSpeedGeneNum = counter; counter++;
        accelerationAdjustSpeedGeneNum = counter; counter++;
        brakeAdjustSpeedGeneNum = counter;
        
    }
    
    
    
    @Override
    public double evaluate(IChromosome ic) {
        
        System.err.println("Testing chromosome nr " + ++counter);
        padDriver.unitNumber++;
        
        
        synchronized (fis){
            
            for (Map.Entry<String, HashMap<String, TermValue>> variable : originalVariables.entrySet()) {
            
                String variableName = variable.getKey();
                HashMap<String, TermValue> variableValue = variable.getValue();

                for (Map.Entry<String, TermValue> term : variableValue.entrySet()) {
                    String termName = term.getKey();
                    TermValue termValue = term.getValue();

                    double x0 = termValue.leftValue + (Double) ic.getGene(termValue.geneNumber).getAllele();
                    double y0 = termValue.leftMembership;
                    double x1 = x0 + termValue.offset * (Double) ic.getGene(termValue.geneNumber + 1).getAllele();
                    double y1 = termValue.rightMembership;
                    
                    fis.getFunctionBlock("fb")
                            .getVariable(variableName)
                            .getLinguisticTerm(termName)
                            .setMembershipFunction(new MembershipFunctionPieceWiseLinear(new Value[]{new Value(x0), new Value(x1)}, new Value[]{new Value(y0),new Value(y1)}));
                }
            }
            
            
            for (Map.Entry<String, RuleWeight> entry : ruleWeights.entrySet()) {
                String ruleName = entry.getKey();
                RuleWeight ruleWeight = entry.getValue();
                
                for (Iterator<Rule> iterator = fis.getFunctionBlock("fb").getFuzzyRuleBlock("No1").getRules().iterator(); iterator.hasNext();) {
                    
                    Rule next = iterator.next();
                    
                    if (next.getName().equals(ruleName)){
                        next.setWeight((Double) ic.getGene(ruleWeight.geneNumber).getAllele());
                    }
                    
                }
            }

            fis.getFunctionBlock("fb").getVariable("steeringAdjustSpeed").setDefaultValue((Double) ic.getGene(steeringAdjustSpeedGeneNum).getAllele());
            fis.getFunctionBlock("fb").getVariable("accelerationAdjustSpeed").setDefaultValue((Double) ic.getGene(accelerationAdjustSpeedGeneNum).getAllele());
            fis.getFunctionBlock("fb").getVariable("brakeAdjustSpeed").setDefaultValue((Double) ic.getGene(brakeAdjustSpeedGeneNum).getAllele());
        }
        
        
        while(true){
            if (ready){
                break;
            }
        }

        double newScore = score;
        score = 0.0f;
        ready = false;
        System.err.println("Evaluated. Result: " + Double.toString(newScore));
        return newScore;
    }
    
}

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

   
    @Override
    public double evaluate(IChromosome ic) {
        int a = (Integer)ic.getGene(0).getAllele();
        int b = (Integer)ic.getGene(1).getAllele();
        int c = (Integer)ic.getGene(2).getAllele();
        int result = a+b+c;
        System.err.println(a + " + " + b + " + " + c + " = " + result);
        return result;
    }
    
}

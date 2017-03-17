/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.amu.wmi.min.torcs.fcl;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionGaussian;
import net.sourceforge.jFuzzyLogic.membership.Value;
import org.apache.commons.lang.Validate;
import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

/**
 *
 * @author admin
 */
public class TorcsFitnessFUnction extends FitnessFunction{

    public volatile double distance = 1.0f;
    public volatile boolean ready = false;
    public FIS fis;
    public TorcsFitnessFUnction(FIS f) {
        fis = f;
    }
    
    
    
    @Override
    public double evaluate(IChromosome ic) {
        
        System.err.println("setting new parameters...");
        
        synchronized (fis){
            double wmMean0 = 0.0f;
        double wmStd0 = ((Double) ic.getGene(0).getAllele()).doubleValue();
        
        double wmMean1 = ((Double) ic.getGene(1).getAllele()).doubleValue();
        double wmStd1 = ((Double) ic.getGene(2).getAllele()).doubleValue();
        
        double wmMean2 = ((Double) ic.getGene(3).getAllele()).doubleValue();
        double wmStd2 = ((Double) ic.getGene(4).getAllele()).doubleValue();
        
        double wmMean3 = ((Double) ic.getGene(5).getAllele()).doubleValue();
        double wmStd3 = ((Double) ic.getGene(6).getAllele()).doubleValue();
        
        double wmMean4 = ((Double) ic.getGene(7).getAllele()).doubleValue();
        double wmStd4 = ((Double) ic.getGene(8).getAllele()).doubleValue();
        
        
        double stMean0 = 0.0f;
        double stStd0 = ((Double) ic.getGene(9).getAllele()).doubleValue();
        
        double stMean1 = ((Double) ic.getGene(10).getAllele()).doubleValue();
        double stStd1 = ((Double) ic.getGene(11).getAllele()).doubleValue();
        
        double stMean2 = ((Double) ic.getGene(12).getAllele()).doubleValue();
        double stStd2 = ((Double) ic.getGene(13).getAllele()).doubleValue();
        
        double stMean3 = ((Double) ic.getGene(14).getAllele()).doubleValue();
        double stStd3 = ((Double) ic.getGene(15).getAllele()).doubleValue();
        
        double stMean4 = ((Double) ic.getGene(16).getAllele()).doubleValue();
        double stStd4 = ((Double) ic.getGene(17).getAllele()).doubleValue();
        

        setWeightedMean(1, wmMean4, wmStd4);
        setWeightedMean(2, wmMean3, wmStd3);
        setWeightedMean(3, wmMean2, wmStd2);
        setWeightedMean(4, wmMean1, wmStd1);
        setWeightedMean(5, wmMean0, wmStd0);
        setWeightedMean(6, -wmMean1, wmStd1);
        setWeightedMean(7, -wmMean2, wmStd2);
        setWeightedMean(8, -wmMean3, wmStd3);
        setWeightedMean(9, -wmMean4, wmStd4);

        setSteering(1, -stMean4, stStd4);
        setSteering(2, -stMean3, stStd3);
        setSteering(3, -stMean2, stStd2);
        setSteering(4, -stMean1, stStd1);
        setSteering(5, stMean0, stStd0);
        setSteering(6, stMean1, stStd1);
        setSteering(7, stMean2, stStd2);
        setSteering(8, stMean3, stStd3);
        setSteering(9, stMean4, stStd4);
        }
        
        
        while(true){
            if (ready){
                break;
            }
        }

        double newDist = distance;
        distance = 1.0f;
        ready = false;
        System.err.println("Evaluated. Result: " + Double.toString(newDist));
        return newDist;
    }
    
    
    void setWeightedMean(int num, double mean, double std){
        fis.getFunctionBlock("driver").getVariable("weightedMean").getLinguisticTerm("wm" + Integer.toString(num)).setMembershipFunction(new MembershipFunctionGaussian(new Value(mean), new Value(std)));
    }
    
    void setSteering(int num, double mean, double std){
        fis.getFunctionBlock("driver").getVariable("steering").getLinguisticTerm("st" + Integer.toString(num)).setMembershipFunction(new MembershipFunctionGaussian(new Value(mean), new Value(std)));
    }
}

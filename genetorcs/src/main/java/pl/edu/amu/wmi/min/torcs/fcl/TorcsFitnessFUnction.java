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
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTriangular;
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
    public static volatile int counter = 0;
    public FIS fis;
    public TorcsFitnessFUnction(FIS f) {
        fis = f;
    }
    
    
    
    @Override
    public double evaluate(IChromosome ic) {
        
        System.err.println("Testing chromosome nr " + ++counter);
        
        synchronized (fis){
       double stMean0 = 0.0f;
            double stStd0 = ((Double) ic.getGene(0).getAllele()).doubleValue();
            double stMean1 = ((Double) ic.getGene(1).getAllele()).doubleValue();
            double stStd1 = ((Double) ic.getGene(2).getAllele()).doubleValue();
            double stMean2 = ((Double) ic.getGene(3).getAllele()).doubleValue();
            double stStd2 = ((Double) ic.getGene(4).getAllele()).doubleValue();
            double stMean3 = ((Double) ic.getGene(5).getAllele()).doubleValue();
            double stStd3 = ((Double) ic.getGene(6).getAllele()).doubleValue();
            double stMean4 = ((Double) ic.getGene(7).getAllele()).doubleValue();
            double stStd4 = ((Double) ic.getGene(8).getAllele()).doubleValue();
            double stMean5 = ((Double) ic.getGene(9).getAllele()).doubleValue();
            double stStd5 = ((Double) ic.getGene(10).getAllele()).doubleValue();
            double stMean6 = ((Double) ic.getGene(11).getAllele()).doubleValue();
            double stStd6 = ((Double) ic.getGene(12).getAllele()).doubleValue();

            double accStd =     ((Double) ic.getGene(13).getAllele()).doubleValue();
            double accMean1  =  ((Double) ic.getGene(14).getAllele()).doubleValue();
            double accMean2  =  ((Double) ic.getGene(15).getAllele()).doubleValue();
            double accMean3  =  ((Double) ic.getGene(16).getAllele()).doubleValue();
            double accMean4  =  ((Double) ic.getGene(17).getAllele()).doubleValue();
            double accMean5  =  ((Double) ic.getGene(18).getAllele()).doubleValue();
            double accMean6  =  ((Double) ic.getGene(19).getAllele()).doubleValue();
            double accMean7  =  ((Double) ic.getGene(20).getAllele()).doubleValue();
            double accMean8  =  ((Double) ic.getGene(21).getAllele()).doubleValue();
            double accMean9  =  ((Double) ic.getGene(22).getAllele()).doubleValue();
            double accMean10  = ((Double) ic.getGene(23).getAllele()).doubleValue();
            double accMean11  = ((Double) ic.getGene(24).getAllele()).doubleValue();
            double accMean12  = ((Double) ic.getGene(25).getAllele()).doubleValue();
            double accMean13  = ((Double) ic.getGene(26).getAllele()).doubleValue();
            double accMean14  = ((Double) ic.getGene(27).getAllele()).doubleValue();
            double accMean15  = ((Double) ic.getGene(28).getAllele()).doubleValue();
            double accMean16  = ((Double) ic.getGene(29).getAllele()).doubleValue();
            double accMean17  = ((Double) ic.getGene(30).getAllele()).doubleValue();
            double accMean18  = ((Double) ic.getGene(31).getAllele()).doubleValue();
            double accMean19  = ((Double) ic.getGene(32).getAllele()).doubleValue();
            double accMean20  = ((Double) ic.getGene(33).getAllele()).doubleValue();
            double accMean21  = ((Double) ic.getGene(34).getAllele()).doubleValue();
            double accMean22  = ((Double) ic.getGene(35).getAllele()).doubleValue();
            double accMean23  = ((Double) ic.getGene(36).getAllele()).doubleValue();
            double accMean24  = ((Double) ic.getGene(37).getAllele()).doubleValue();
            double accMean25  = ((Double) ic.getGene(38).getAllele()).doubleValue();

            double brkStd =     ((Double) ic.getGene(39).getAllele()).doubleValue();
            double brkMean1  =  ((Double) ic.getGene(40).getAllele()).doubleValue();
            double brkMean2  =  ((Double) ic.getGene(41).getAllele()).doubleValue();
            double brkMean3  =  ((Double) ic.getGene(42).getAllele()).doubleValue();
            double brkMean4  =  ((Double) ic.getGene(43).getAllele()).doubleValue();
            double brkMean5  =  ((Double) ic.getGene(44).getAllele()).doubleValue();
            double brkMean6  =  ((Double) ic.getGene(45).getAllele()).doubleValue();
            double brkMean7  =  ((Double) ic.getGene(46).getAllele()).doubleValue();
            double brkMean8  =  ((Double) ic.getGene(47).getAllele()).doubleValue();
            double brkMean9  =  ((Double) ic.getGene(48).getAllele()).doubleValue();
            double brkMean10  = ((Double) ic.getGene(49).getAllele()).doubleValue();
            double brkMean11  = ((Double) ic.getGene(50).getAllele()).doubleValue();
            double brkMean12  = ((Double) ic.getGene(51).getAllele()).doubleValue();
            double brkMean13  = ((Double) ic.getGene(52).getAllele()).doubleValue();
            double brkMean14  = ((Double) ic.getGene(53).getAllele()).doubleValue();
            double brkMean15  = ((Double) ic.getGene(54).getAllele()).doubleValue();
            double brkMean16  = ((Double) ic.getGene(55).getAllele()).doubleValue();
            double brkMean17  = ((Double) ic.getGene(56).getAllele()).doubleValue();
            double brkMean18  = ((Double) ic.getGene(57).getAllele()).doubleValue();
            double brkMean19  = ((Double) ic.getGene(58).getAllele()).doubleValue();
            double brkMean20  = ((Double) ic.getGene(59).getAllele()).doubleValue();
            double brkMean21  = ((Double) ic.getGene(60).getAllele()).doubleValue();
            double brkMean22  = ((Double) ic.getGene(61).getAllele()).doubleValue();
            double brkMean23  = ((Double) ic.getGene(62).getAllele()).doubleValue();
            double brkMean24  = ((Double) ic.getGene(63).getAllele()).doubleValue();
            double brkMean25  = ((Double) ic.getGene(64).getAllele()).doubleValue();

            setSteering(1, -stMean6, stStd6);
            setSteering(2, -stMean5, stStd5);
            setSteering(3, -stMean4, stStd4);
            setSteering(4, -stMean3, stStd3);
            setSteering(5, -stMean2, stStd2);
            setSteering(6, -stMean1, stStd1);
                setSteering(7, stMean0, stStd0);
            setSteering(8, stMean1, stStd1);
            setSteering(9, stMean2, stStd2);
            setSteering(10, stMean3, stStd3);
            setSteering(11, stMean4, stStd4);
            setSteering(12, stMean5, stStd5);
            setSteering(13, stMean6, stStd6);
            
            setAccelerate(1 , accMean1 , accStd);
            setAccelerate(2 , accMean2 , accStd);
            setAccelerate(3 , accMean3 , accStd);
            setAccelerate(4 , accMean4 , accStd);
            setAccelerate(5 , accMean5 , accStd);
            setAccelerate(6 , accMean6 , accStd);
            setAccelerate(7 , accMean7 , accStd);
            setAccelerate(8 , accMean8 , accStd);
            setAccelerate(9 , accMean9 , accStd);
            setAccelerate(10, accMean10, accStd);
            setAccelerate(11, accMean11, accStd);
            setAccelerate(12, accMean12, accStd);
            setAccelerate(13, accMean13, accStd);
            setAccelerate(14, accMean14, accStd);
            setAccelerate(15, accMean15, accStd);
            setAccelerate(16, accMean16, accStd);
            setAccelerate(17, accMean17, accStd);
            setAccelerate(18, accMean18, accStd);
            setAccelerate(19, accMean19, accStd);
            setAccelerate(20, accMean20, accStd);
            setAccelerate(21, accMean21, accStd);
            setAccelerate(22, accMean22, accStd);
            setAccelerate(23, accMean23, accStd);
            setAccelerate(24, accMean24, accStd);
            setAccelerate(25, accMean25, accStd);
            
            setBrake(1 , brkMean1 , brkStd);
            setBrake(2 , brkMean2 , brkStd);
            setBrake(3 , brkMean3 , brkStd);
            setBrake(4 , brkMean4 , brkStd);
            setBrake(5 , brkMean5 , brkStd);
            setBrake(6 , brkMean6 , brkStd);
            setBrake(7 , brkMean7 , brkStd);
            setBrake(8 , brkMean8 , brkStd);
            setBrake(9 , brkMean9 , brkStd);
            setBrake(10, brkMean10, brkStd);
            setBrake(11, brkMean11, brkStd);
            setBrake(12, brkMean12, brkStd);
            setBrake(13, brkMean13, brkStd);
            setBrake(14, brkMean14, brkStd);
            setBrake(15, brkMean15, brkStd);
            setBrake(16, brkMean16, brkStd);
            setBrake(17, brkMean17, brkStd);
            setBrake(18, brkMean18, brkStd);
            setBrake(19, brkMean19, brkStd);
            setBrake(20, brkMean20, brkStd);
            setBrake(21, brkMean21, brkStd);
            setBrake(22, brkMean22, brkStd);
            setBrake(23, brkMean23, brkStd);
            setBrake(24, brkMean24, brkStd);
            setBrake(25, brkMean25, brkStd);
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
    
    void setSpeed(int num, double mean, double std){
        fis.getFunctionBlock("driver").getVariable("speed").getLinguisticTerm("sp" + Integer.toString(num)).setMembershipFunction(new MembershipFunctionGaussian(new Value(mean), new Value(std)));
    }
    
    void setTrack9(int num, double mean, double std){
        fis.getFunctionBlock("driver").getVariable("track9").getLinguisticTerm("tr" + Integer.toString(num)).setMembershipFunction(new MembershipFunctionGaussian(new Value(mean), new Value(std)));
    }
    
    void setAccelerate(int num, double mean, double std){
        fis.getFunctionBlock("driver").getVariable("accelerate").getLinguisticTerm("acc" + Integer.toString(num)).setMembershipFunction(new MembershipFunctionTriangular(new Value(mean-(std/2.0f)), new Value(mean), new Value(mean+(std/2.0f))));
    }
    
    void setBrake(int num, double mean, double std){
        fis.getFunctionBlock("driver").getVariable("brake").getLinguisticTerm("brk" + Integer.toString(num)).setMembershipFunction(new MembershipFunctionTriangular(new Value(mean-(std/2.0f)), new Value(mean), new Value(mean+(std/2.0f))));
    }
}

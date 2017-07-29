package pl.edu.amu.wmi.min.torcs.fcl;

import java.io.IOException;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;

import org.antlr.works.visualization.graphics.GRenderer;


public class Client {

    public static void main(String[] args) {
        String fileName = "controller.fcl";
        FIS fis = FIS.load(fileName,true);
        
        if( fis == null ) { // Error while loading?
            System.err.println("Can't load file: '" + fileName + "'");
            return;
        }
        
        JFuzzyChart.get().chart(fis.getFunctionBlock(null));
    }
}

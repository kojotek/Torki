/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.amu.wmi.min.torcs.fcl;
import java.io.FileNotFoundException;
import java.io.FileReader;
import net.sourceforge.jFuzzyLogic.FIS;
import org.jgap.Chromosome;
import pl.edu.amu.wmi.min.torcs.fcl.TorcsFitnessFUnction;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.InvalidConfigurationException;
import org.jgap.UnsupportedRepresentationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;
import org.jgap.xml.GeneCreationException;
import org.jgap.xml.ImproperXMLException;
import org.jgap.xml.XMLManager;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jgap.Configuration;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.event.GeneticEvent;
import org.jgap.event.GeneticEventListener;
import org.jgap.event.IEventManager;
import org.jgap.gp.GPFitnessFunction;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.impl.BranchTypingCross;
import org.jgap.gp.impl.DefaultGPFitnessEvaluator;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.impl.TournamentSelector;
import org.jgap.util.SystemKit;
import org.jgap.xml.XMLDocumentBuilder;
import org.jgap.xml.XMLManager;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author admin
 */
public class GeneticAl {
    
    public Configuration conf;
    public TorcsFitnessFUnction fitFunc;
    public Gene[] sampleGenes;
    public Genotype population;
    public int starGen = 0;
    
    public GeneticAl(FIS fis) throws InvalidConfigurationException, FileNotFoundException, ParserConfigurationException, ImproperXMLException, UnsupportedRepresentationException, GeneCreationException, SAXException, IOException {
        conf = new DefaultConfiguration();
        fitFunc = new TorcsFitnessFUnction(fis);
        conf.setFitnessFunction(fitFunc);
        
        sampleGenes = new Gene[65];
        
        
        //STEERING WHEEL
        sampleGenes[0] = new DoubleGene(conf, 0.01f, 1.0f);//szer
        sampleGenes[1] = new DoubleGene(conf, -1.0f, 1.0f);//dyst
        sampleGenes[2] = new DoubleGene(conf, 0.01f, 1.0f);//szer
        sampleGenes[3] = new DoubleGene(conf, -1.0f, 1.0f);//dyst
        sampleGenes[4] = new DoubleGene(conf, 0.01f, 1.0f);//szer
        sampleGenes[5] = new DoubleGene(conf, -1.0f, 1.0f);//dyst
        sampleGenes[6] = new DoubleGene(conf, 0.01f, 1.0f);//szer
        sampleGenes[7] = new DoubleGene(conf, -1.0f, 1.0f);//dyst
        sampleGenes[8] = new DoubleGene(conf, 0.01f, 1.0f);//szer
        sampleGenes[9] = new DoubleGene(conf, -1.0f, 1.0f);//dyst
        sampleGenes[10] = new DoubleGene(conf, 0.01f, 1.0f);//szer
        sampleGenes[11] = new DoubleGene(conf, -1.0f, 1.0f);//dyst
        sampleGenes[12] = new DoubleGene(conf, 0.01f, 1.0f);//szer koniec steering
        
        sampleGenes[13] = new DoubleGene(conf, 0.01f, 1.0f);//szerokosc poczatek acc
        sampleGenes[14] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[15] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[16] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[17] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[18] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[19] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[20] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[21] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[22] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[23] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[24] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[25] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[26] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[27] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[28] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[29] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[30] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[31] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[32] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[33] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[34] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[35] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[36] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[37] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[38] = new DoubleGene(conf, -1.0f, 1.5f);//dyst koniec acc
        
        sampleGenes[39] = new DoubleGene(conf, 0.01f, 1.0f);//szerokosc poczatek brake
        sampleGenes[40] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[41] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[42] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[43] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[44] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[45] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[46] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[47] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[48] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[49] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[50] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[51] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[52] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[53] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[54] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[55] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[56] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[57] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[58] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[59] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[60] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[61] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[62] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[63] = new DoubleGene(conf, -1.0f, 1.5f);//dyst
        sampleGenes[64] = new DoubleGene(conf, -1.0f, 1.5f);//dyst

        Chromosome sampleChromosome = new Chromosome(conf, sampleGenes );
        
        conf.setSampleChromosome( sampleChromosome );
        
        Scanner sc = new Scanner(System.in);
        String file = sc.nextLine();
        if (file.isEmpty()){
            conf.setPopulationSize( Consts.populationSize );
            population = Genotype.randomInitialGenotype(conf);
        }
        else{
            starGen = Integer.parseInt(file.replace(".xml", ""));
            starGen = starGen + 1;
            Reader documentReader = new FileReader( "fcl/generations/" + file );
            InputSource documentSource = new InputSource( documentReader );
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document genotypeDocument = builder.parse( documentSource );
            conf.setPopulationSize( Consts.populationSize );
            population = XMLManager.getGenotypeFromDocument(conf, genotypeDocument );
            System.err.println(population.getPopulation().size());
        }
        
    }
}

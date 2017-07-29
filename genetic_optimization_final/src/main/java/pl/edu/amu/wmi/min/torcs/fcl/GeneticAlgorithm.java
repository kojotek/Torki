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
import pl.edu.amu.wmi.min.torcs.fcl.TorcsFitnessFunction;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jgap.Configuration;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
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
import org.jgap.impl.BestChromosomesSelector;
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
public class GeneticAlgorithm {
    
    public Configuration conf;
    public TorcsFitnessFunction fitFunc;
    public Gene[] sampleGenes;
    public Genotype genotype;
    public int starGen = 0;
    
    public GeneticAlgorithm(FIS workingFis, String fileName) throws InvalidConfigurationException, FileNotFoundException, ParserConfigurationException, ImproperXMLException, UnsupportedRepresentationException, GeneCreationException, SAXException, IOException {
        conf = new DefaultConfiguration();
        conf.removeNaturalSelectors(false);
        BestChromosomesSelector bcs = new BestChromosomesSelector(conf, 0.9);
        bcs.setDoubletteChromosomesAllowed(true);
        conf.addNaturalSelector(bcs, false);
        fitFunc = new TorcsFitnessFunction(workingFis, fileName);
        conf.setFitnessFunction(fitFunc);
        conf.setPreservFittestIndividual(true);
        
        sampleGenes = new Gene[fitFunc.genesForTerms + fitFunc.rulesCount + 3];
        
        for (Map.Entry<String, HashMap<String, TorcsFitnessFunction.TermValue>> en : fitFunc.originalVariables.entrySet()) {
            String key = en.getKey();
             HashMap<String, TorcsFitnessFunction.TermValue> value = en.getValue();
            
             for (Map.Entry<String, TorcsFitnessFunction.TermValue> entry : value.entrySet()) {
                String key1 = entry.getKey();
                TorcsFitnessFunction.TermValue value1 = entry.getValue();
                
                sampleGenes[value1.geneNumber] = new DoubleGene(conf, -15.0f, 15.0f);
                sampleGenes[value1.geneNumber+1] = new DoubleGene(conf, 0.1f, 10.0f);
            }
        }
        
        for (Map.Entry<String, TorcsFitnessFunction.RuleWeight> entry : fitFunc.ruleWeights.entrySet()) {
            String key = entry.getKey();
            TorcsFitnessFunction.RuleWeight value = entry.getValue();
            
            sampleGenes[value.geneNumber] = new DoubleGene(conf, 0.1f, 1.0f);
        }
        
        sampleGenes[sampleGenes.length-3] = new DoubleGene(conf, 0.0f, 0.2f);
        sampleGenes[sampleGenes.length-2] = new DoubleGene(conf, 0.0f, 0.2f);
        sampleGenes[sampleGenes.length-1] = new DoubleGene(conf, 0.0f, 0.2f);
        
        Chromosome sampleChromosome = new Chromosome(conf, sampleGenes);
        conf.setSampleChromosome( sampleChromosome );
        
        Scanner sc = new Scanner(System.in);
        String file = sc.nextLine();
        if (file.isEmpty()){
            conf.setPopulationSize(Consts.populationSize);
            genotype = Genotype.randomInitialGenotype(conf);
        }
        else{
            starGen = Integer.parseInt(file.replace(".xml", ""));
            starGen = starGen + 1;
            Reader documentReader = new FileReader( "furia_genetic/generations/" + file );
            InputSource documentSource = new InputSource( documentReader );
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document genotypeDocument = builder.parse( documentSource );
            conf.setPopulationSize( Consts.populationSize );
            genotype = XMLManager.getGenotypeFromDocument(conf, genotypeDocument );
            System.err.println(genotype.getPopulation().size());
        }
        
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.amu.wmi.min.torcs.fcl;
import net.sourceforge.jFuzzyLogic.FIS;
import org.jgap.Chromosome;
import pl.edu.amu.wmi.min.torcs.fcl.TorcsFitnessFUnction;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;

/**
 *
 * @author admin
 */
public class GeneticAl {
    
    public Configuration conf;
    public TorcsFitnessFUnction fitFunc;
    public Gene[] sampleGenes;
    public Genotype population;
    
    public GeneticAl(FIS fis) throws InvalidConfigurationException {
        conf = new DefaultConfiguration();
        fitFunc = new TorcsFitnessFUnction(fis);
        conf.setFitnessFunction(fitFunc);
        
        sampleGenes = new Gene[18];
        
        sampleGenes[0] = new DoubleGene(conf, 0.5f, 10.0f);
        
        sampleGenes[1] = new DoubleGene(conf, 0.0f, 25.0f);
        sampleGenes[2] = new DoubleGene(conf, 0.5f, 10.0f);
       
        sampleGenes[3] = new DoubleGene(conf, 0.0f, 25.0f);
        sampleGenes[4] = new DoubleGene(conf, 0.5f, 10.0f);
        
        sampleGenes[5] = new DoubleGene(conf, 0.0f, 25.0f);
        sampleGenes[6] = new DoubleGene(conf, 0.5f, 10.0f);
        
        sampleGenes[7] = new DoubleGene(conf, 0.0f, 25.0f);
        sampleGenes[8] = new DoubleGene(conf, 0.5f, 10.0f);
        
        
        
        
        sampleGenes[9] = new DoubleGene(conf, 0.01f, 1.0f);
        
        sampleGenes[10] = new DoubleGene(conf, 0.01f, 1.0f);
        sampleGenes[11] = new DoubleGene(conf, 0.01f, 1.0f);
        
        sampleGenes[12] = new DoubleGene(conf, 0.01f, 1.0f);
        sampleGenes[13] = new DoubleGene(conf, 0.01f, 1.0f);
        
        sampleGenes[14] = new DoubleGene(conf, 0.01f, 1.0f);
        sampleGenes[15] = new DoubleGene(conf, 0.01f, 1.0f);
        
        sampleGenes[16] = new DoubleGene(conf, 0.01f, 1.0f);
        sampleGenes[17] = new DoubleGene(conf, 0.01f, 1.0f);

        
        Chromosome sampleChromosome = new Chromosome(conf, sampleGenes );
        
        conf.setSampleChromosome( sampleChromosome );
        
        conf.setPopulationSize( Consts.populationSize );
        
        population = Genotype.randomInitialGenotype(conf);
    }
    
    
}

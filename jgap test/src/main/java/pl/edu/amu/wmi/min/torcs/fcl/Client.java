/*
    Copyright (C) 2016 Patryk Żywica

    This file is part of Torcs FCL Client for Java.

    Torcs FCL Client for Java is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Torcs FCL Client for Java is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package pl.edu.amu.wmi.min.torcs.fcl;

import java.util.HashMap;
import java.util.Map;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.BestChromosomesSelector;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.IntegerGene;

/**
 * This class is based on original Computational Intelligence in Games TORCS
 * client.
 *
 * @author Daniele Loiacono
 * @author Patryk Żywica
 *
 */
public class Client {

    
    public static void main(String[] args) throws InvalidConfigurationException {
        
        Configuration conf = new DefaultConfiguration();
        conf.removeNaturalSelectors(false);
        BestChromosomesSelector bcs = new BestChromosomesSelector(conf, 1.0);
        bcs.setDoubletteChromosomesAllowed(true);
        conf.addNaturalSelector(bcs, false);
        TorcsFitnessFunction fitFunc = new TorcsFitnessFunction();
        conf.setFitnessFunction(fitFunc);

        Gene[] sampleGenes = new Gene[3];
        
        sampleGenes[0] = new IntegerGene(conf, 0, 50);
        sampleGenes[1] = new IntegerGene(conf, 0, 50);
        sampleGenes[2] = new IntegerGene(conf, 0, 50);

        Chromosome c = new Chromosome(conf,sampleGenes);
        conf.setSampleChromosome(c);
        conf.setPopulationSize(10);
        Genotype genotype = Genotype.randomInitialGenotype(conf);
        
        for (int i = 0; i < 100; i++) {
            System.err.println("");
            System.err.println("generacja: " + i);
            genotype.evolve();
            for (int j = 0; j < genotype.getPopulation().getChromosomes().size(); j++) {
                System.err.println("osobnik " + j + ": " 
                        + (Integer)genotype.getPopulation().getChromosome(j).getGene(0).getAllele() + " + " 
                        + (Integer)genotype.getPopulation().getChromosome(j).getGene(1).getAllele() + " + " 
                        + (Integer)genotype.getPopulation().getChromosome(j).getGene(2).getAllele() + " = " + genotype.getPopulation().getChromosome(j).getFitnessValue());
            }
        }
    }
}

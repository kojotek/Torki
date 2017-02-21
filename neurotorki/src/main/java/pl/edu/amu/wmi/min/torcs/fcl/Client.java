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

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import net.sourceforge.cig.torcs.MessageBasedSensorModel;
import net.sourceforge.cig.torcs.Controller;
import net.sourceforge.cig.torcs.Action;
import net.sourceforge.cig.torcs.SocketHandler;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.jFuzzyLogic.FIS;

import net.sourceforge.cig.torcs.Controller.Stage;
import org.antlr.works.visualization.graphics.GRenderer;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.Hopfield;
import org.neuroph.nnet.NeuroFuzzyPerceptron;
import org.neuroph.nnet.Perceptron;

/**
 * This class is based on original Computational Intelligence in Games TORCS
 * client.
 *
 * @author Daniele Loiacono
 * @author Patryk Żywica
 *
 */
public class Client {

    private static final int UDP_TIMEOUT = 10000;
    private static int port;
    private static String host;
    private static String clientId;
    private static boolean verbose;
    private static int maxEpisodes;
    private static int maxSteps;
    private static Stage stage;
    private static String trackName;
    private static String gearsFile;

    public static void main(String[] args) throws IOException {
        //System.err.println("zaczynam");
        //learn("output.csv");
        //System.err.println("koniec");
        parseParameters(args);
        SocketHandler mySocket = new SocketHandler(host, port, verbose);
        String inMsg;

        if (args.length == 0) {
            System.err.println("Arguments:\n"
                    + "<fileName>\t is used to specify the path to FCL driver file (required as first argument)\n"
                    + "<port:N>\t is used to specify the port for the connection (default is 3001)\n"
                    + "<host:ADDRESS>\t is used to specify the address of the host where the server is running (default is localhost)  \n"
                    + "<id:ClientID>\t is used to specify the ID of the client sent to the server (default is championship2009) \n"
                    + "<verbose:on>\t is used to set verbose mode on (default is off)\n"
                    + "<maxEpisodes:N>\t is used to set the number of episodes (default is 1)\n"
                    + "<maxSteps:N>\t is used to set the max number of steps for each episode (0 is default value, that means unlimited number of steps)\n"
                    + "<stage:N>\t is used to set the current stage: 0 is WARMUP, 1 is QUALIFYING, 2 is RACE, others value means UNKNOWN (default is UNKNOWN)\n"
                    + "<trackName:name>\t is used to set the name of current track (optional)"
                    + "<gearPreferences:file>\t is used to specify file with gears preferences (optional)");
            return;
        }

        // load the saved network 
        NeuralNetwork neuralNetwork = NeuralNetwork.load("or_perceptron.nnet");

        Controller driver = new NeuroDriver(neuralNetwork);
        driver.setStage(stage);
        driver.setTrackName(trackName);
        
        GearPreference gPref = new GearPreference();
        try {
            gPref.Load(gearsFile);
            driver.setGearsPreferences(gPref);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Build init string */
        float[] angles = driver.initAngles();
        String initStr = clientId + "(init";
        for (int i = 0; i < angles.length; i++) {
            initStr = initStr + " " + angles[i];
        }
        initStr = initStr + ")";

        long curEpisode = 0;
        boolean shutdownOccurred = false;
        do {
            /*
             * Client identification
             */
            do {
                mySocket.send(initStr);
                inMsg = mySocket.receive(UDP_TIMEOUT);
            } while (inMsg == null || inMsg.indexOf("***identified***") < 0);

            /*
	     * Start to drive
             */
            long currStep = 0;
            while (true) {
                /*
		 * Receives from TORCS the game state
                 */
                inMsg = mySocket.receive(UDP_TIMEOUT);

                if (inMsg != null) {

                    /*
		     * Check if race is ended (shutdown)
                     */
                    if (inMsg.indexOf("***shutdown***") >= 0) {
                        shutdownOccurred = true;
                        System.out.println("Server shutdown!");
                        break;
                    }

                    /*
		     * Check if race is restarted
                     */
                    if (inMsg.indexOf("***restart***") >= 0) {
                        driver.reset();
                        if (verbose) {
                            System.out.println("Server restarting!");
                        }
                        break;
                    }

                    Action action = new Action();
                    if (currStep < maxSteps || maxSteps == 0) {
                        action = driver.control(new MessageBasedSensorModel(
                                inMsg));
                    } else {
                        action.restartRace = true;
                    }

                    currStep++;
                    mySocket.send(action.toString());
                } else {
                    System.out.println("Server did not respond within the timeout");
                }
            }

        } while (++curEpisode < maxEpisodes && !shutdownOccurred);

        /*
	 * Shutdown the controller
         */
        driver.shutdown();
        mySocket.close();
        System.out.println("Client shutdown.");
        System.out.println("Bye, bye!");
    }

    private static void parseParameters(String[] args) {
        /*
	 * Set default values for the options
         */
        port = 3001;
        host = "localhost";
        clientId = "SCR";
        verbose = false;
        maxEpisodes = 1;
        maxSteps = 0;
        stage = Stage.UNKNOWN;
        trackName = "unknown";
        gearsFile = "";
        
        for (int i = 1; i < args.length; i++) {
            StringTokenizer st = new StringTokenizer(args[i], ":");
            String entity = st.nextToken();
            String value = st.nextToken();
            if (entity.equals("port")) {
                port = Integer.parseInt(value);
            }
            if (entity.equals("host")) {
                host = value;
            }
            if (entity.equals("id")) {
                clientId = value;
            }
            if (entity.equals("verbose")) {
                if (value.equals("on")) {
                    verbose = true;
                } else if (value.equals(false)) {
                    verbose = false;
                } else {
                    throw new IllegalArgumentException(entity + ":" + value
                            + " is not a valid option");
                }
            }
            if (entity.equals("id")) {
                clientId = value;
            }
            if (entity.equals("gearPreferences")) {
                gearsFile = value;
            }
            if (entity.equals("stage")) {
                stage = Stage.fromInt(Integer.parseInt(value));
            }
            if (entity.equals("trackName")) {
                trackName = value;
            }
            if (entity.equals("maxEpisodes")) {
                maxEpisodes = Integer.parseInt(value);
                if (maxEpisodes <= 0) {
                    throw new IllegalArgumentException(entity + ":" + value
                            + " is not a valid option");
                }
            }
            if (entity.equals("maxSteps")) {
                maxSteps = Integer.parseInt(value);
                if (maxSteps < 0) {
                    throw new IllegalArgumentException(entity + ":" + value
                            + " is not a valid option");
                }
            }
        }
    }
    
    public static void learn(String filePath) throws FileNotFoundException, IOException{
        CSVReader reader = new CSVReader(new FileReader(filePath), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
        List<String[]> entries = reader.readAll();
        reader.close();
        int entriesCount = entries.size();
        
                // create new perceptron network 
        NeuralNetwork neuralNetwork = new Perceptron(4, 3);
        
        // create training set 
        DataSet trainingSet =  new  DataSet(4, 3); 
        // add training data to training set (logical OR function) 
        
        for (int i = 0; i < entriesCount; i++){
            String[] entry = entries.get(i);
            double inputs[] = new double[4];
            double outputs[] = new double[3];
             inputs[0] = Double.parseDouble(entry[9]);
            inputs[1] = Double.parseDouble(entry[19]);
            inputs[2] = Double.parseDouble(entry[21]);
            inputs[3] = Double.parseDouble(entry[23]);
            outputs[0] = Double.parseDouble(entry[68]);
            outputs[0] = Double.parseDouble(entry[69]);
            outputs[0] = Double.parseDouble(entry[70]);
            
            trainingSet.addRow (new DataSetRow (inputs, outputs)); 
        }
        // learn the training set 
        neuralNetwork.learn(trainingSet); 
        // save the trained network into file 
        neuralNetwork.save("or_perceptron.nnet"); 
    }
}

package pl.edu.amu.wmi.min.torcs.fcl;
import java.util.List;
import net.sourceforge.cig.torcs.Controller;
import net.sourceforge.cig.torcs.Action;
import net.sourceforge.cig.torcs.SensorModel;


public class padDriver extends Controller {

    boolean pulse;
    net.java.games.input.Controller pad;
    private final float[] angles;
    /* Gear Changing Constants*/
    private  int[] gearUp = {9000, 8000, 8000, 8000, 8000, 0};
    private  int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};
    private float x,z,b = 0.0f;
    private float smoothedSteering = 0.0f;
    //private float currentFrameTimeStamp = (float)System.nanoTime()/1000.0f;
    //private float lastFrameTimeStamp =  (float)System.nanoTime()/1000.0f;
    //private float frameTime = 1.0f;
    
    Action toReturn;

    public padDriver() {
        this.pulse = false;
        
        toReturn = new Action();
        
        net.java.games.input.Controller[] controllers = net.java.games.input.ControllerEnvironment.getDefaultEnvironment().getControllers();
        
        if(controllers.length==0) {
            System.out.println("Found no controllers.");
            System.exit(0);
        }

        for (net.java.games.input.Controller controller : controllers) {
            if (controller.getType() == net.java.games.input.Controller.Type.KEYBOARD) {
                pad = controller;
            }
        }
        
        this.angles = new float[19];
        /* set angles as {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90} */
        for (int i = 0; i < 5; i++) {
            angles[i] = -90 + i * 15;
            angles[18 - i] = 90 - i * 15;
        }
        
        for (int i = 5; i < 9; i++) {
            angles[i] = -20 + (i - 5) * 5;
            angles[18 - i] = 20 - (i - 5) * 5;
        }
        angles[9] = 0;
    }
    
    
    @Override
    public Action control(SensorModel sensors) {
        
        //currentFrameTimeStamp = (float)System.nanoTime()/1000.0f;
        //frameTime = currentFrameTimeStamp - lastFrameTimeStamp;
        //lastFrameTimeStamp = currentFrameTimeStamp;
        
        while(true) {
         net.java.games.input.Controller[] controllers = net.java.games.input.ControllerEnvironment.getDefaultEnvironment().getControllers();
         if(controllers.length==0) {
            System.out.println("Found no controllers.");
            System.exit(0);
         }
         
         for(int i=0;i<controllers.length;i++) {
            controllers[i].poll();
            net.java.games.input.EventQueue queue = controllers[i].getEventQueue();
            net.java.games.input.Event event = new net.java.games.input.Event();
            
            while(queue.getNextEvent(event)) {
               StringBuffer buffer = new StringBuffer(controllers[i].getName());
               buffer.append(" at ");
               buffer.append(event.getNanos()).append(", ");
               net.java.games.input.Component comp = event.getComponent();
               buffer.append(comp.getIdentifier().getName()).append(" changed to ");
               float value = event.getValue(); 
                  buffer.append(value);

               System.err.println(buffer.toString());
            }
         }
        
         try {
            Thread.sleep(20);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
        }
        
        pad.poll();
        net.java.games.input.EventQueue queue = pad.getEventQueue();
        net.java.games.input.Event event = new net.java.games.input.Event();

        System.err.println("dupa");
        while(queue.getNextEvent(event)) {
            String id = event.getComponent().getIdentifier().toString();
            System.err.println(id);
            float value = event.getValue();
            
            switch(id){
                case "x":
                    x = value;
                break;

                case "z":
                    z = value;
                break;

                case "0":
                    b = value;
                break;
            }
        }

        
        
        if (x > 0.3f ){
            toReturn.steering = -1.0f * ((0.6f * x * x) + 0.7f * x - 0.20f) * (x/2.0f + 0.5f);
        }
        if (x < -0.3f){
            float invValue = -x;
            toReturn.steering = 1.0f * ((0.6f * invValue * invValue) + 0.7f * invValue - 0.20f) * (invValue/2.0f + 0.5f);
        }
        if (Math.abs(x) < 0.3f){
            toReturn.steering = 0.0f;
        }
            
        
        if(toReturn.steering == 0.0f){
            smoothedSteering += (toReturn.steering - smoothedSteering) * 20.0f/60.0f; 
        }
        else{
            smoothedSteering += (toReturn.steering - smoothedSteering) * 3.0f/60.0f; 
        }
        toReturn.steering = smoothedSteering / 3.0f; 
                
        float brakeRaw = 0.0f;
        
        if (z < -0.25f){
            toReturn.accelerate = -z;
        }
        if (z > 0.25f){
            brakeRaw = z;
            if (pulse){
                toReturn.brake = z;
                pulse = false;
            }
            else{
                toReturn.brake = 0.0f;
                pulse = true;
            }

        }


        if (Math.abs(z) < 0.25f){
            brakeRaw = 0.0f;
            toReturn.accelerate = 0.0f;
            toReturn.brake = 0.0f;
        }
  
        
        if (b > 0.1f){
            toReturn.accelerate = -1.0f;
        }

        
        toReturn.gear = getGear(sensors);

        // Set the input
        // Track [0,200] (m)
        // Vector of 19 range finder sensors: each sensors returns the
        // distance between the track edge and the car within a range
        // of 200 meters. When noisy option is enabled (see Section
        // 7), sensors are affected by i.i.d. normal noises with a
        // standard deviation equal to the 10% of sensors range. By
        // default, the sensors sample the space in front of the car every
        // 10 degrees, spanning clockwise from -90 degrees up to
        // +90 degrees with respect to the car axis. However, the con-
        // figuration of the range finder sensors (i.e., the angle w.r.t.
        // to the car axis) can be set by the client once during initialization,
        // i.e., before the beginning of each race. When the
        // car is outside of the track (i.e., pos is less than -1 or greater
        // than 1), the returned values are not reliable (typically -1 is
        // returned).
        double[] trackEdge = sensors.getTrackEdgeSensors();



        //for (int i = 0; i < angles.length; i++) {
        //    fis.setVariable("track" + i, trackEdge[i]);
        //}

        double[] curvePredictions = new double[trackEdge.length/2];
        for (int i = 10; i < trackEdge.length; i++) {
            double curvePrediction = Math.log10(trackEdge[i]/trackEdge[18-i]);
            curvePredictions[i-10] = curvePrediction;
        }

        // Track pos (−∞,+∞)
        // Distance between the car and the track axis. The value is
        // normalized w.r.t to the track width: it is 0 when car is on
        // the axis, -1 when the car is on the right edge of the track
        // and +1 when it is on the left edge of the car. Values greater
        // than 1 or smaller than -1 mean that the car is outside of
        // the track.

        double trackPosition = sensors.getTrackPosition();
        //fis.setVariable("trackPos", trackPosition);

        // Speed (x) (−∞,+∞) (km/h) 
        // Speed of the car along the longitudinal axis of the car.
        double speed = sensors.getSpeed();
        //fis.setVariable("speed", speed);

        double distanceRaced = sensors.getDistanceRaced();
        //fis.setVariable("distanceRaced", distanceRaced);

        // Opponents [0,200] (m)
        // Vector of 36 opponent sensors: each sensor covers a span
        // of 10 degrees within a range of 200 meters and returns the
        // distance of the closest opponent in the covered area. When
        // noisy option is enabled (see Section 7), sensors are affected
        // by i.i.d. normal noises with a standard deviation equal to
        // the 2% of sensors range. The 36 sensors cover all the space
        // around the car, spanning clockwise from -180 degrees up to
        // +180 degrees with respect to the car axis.
        double[] opponents = sensors.getOpponentSensors();
        //for (int i = 0; i < opponents.length; i++) {
        //    fis.setVariable("opponent" + i, opponents[i]);
        //}

        // angle [-π,+π] (rad) 
        // Angle between the car direction and the direction of the
        // track axis.
        double angle = sensors.getAngleToTrackAxis();

        //fis.setVariable("angle", angle);

        //fis.evaluate();

        // set the output
        // Steering value: -1 and +1 means respectively full right and
        // left, that corresponds to an angle of 0.366519 rad. Range [-1,1]
        //Variable steering = fis.getVariable("steering");
        //Virtual gas pedal (0 means no gas, 1 full gas). Range [0,1] 
        //Variable accelerate = fis.getVariable("accelerate");
        // Virtual brake pedal (0 means no brake, 1 full brake). Range [0,1]
        //Variable brake = fis.getVariable("brake");
        
        

        //toReturn.steering = steering.getValue();
        //toReturn.accelerate = accelerate.getValue();
        //toReturn.brake = brake.getValue();


        toReturn.gear = getGear(sensors);



        StringBuilder strBuilder = new StringBuilder();
        
        for (int i = 0; i < angles.length; i++) {
            strBuilder.append(trackEdge[i]);
            strBuilder.append(",");
        }
        
        for (int i = 0; i < curvePredictions.length; i++) {
            strBuilder.append(curvePredictions[i]);
            strBuilder.append(",");
        }
        
        strBuilder.append(trackPosition);
        strBuilder.append(",");
        strBuilder.append(speed);
        strBuilder.append(",");
        strBuilder.append(distanceRaced);
        strBuilder.append(",");
       
        for (int i = 0; i < opponents.length; i++) {
            strBuilder.append(opponents[i]);
            strBuilder.append(",");
        }
       
        strBuilder.append(angle);
        strBuilder.append(",");
        strBuilder.append(toReturn.steering);
        strBuilder.append(",");
        strBuilder.append(toReturn.accelerate);
        strBuilder.append(",");
        //strBuilder.append(toReturn.brake);
        strBuilder.append(brakeRaw);
        strBuilder.append(",");
        strBuilder.append(toReturn.gear);
        strBuilder.append(",");

        System.out.println(strBuilder.toString());
        return toReturn;
    }
    
    @Override
    public void reset() {
        //System.out.println("Restarting the race!");
        
    }
    
    @Override
    public void shutdown() {
        //System.out.println("Bye bye!");
    }
    
    @Override
    public float[] initAngles() {
        return angles;
    }
    
    private int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        // if gear is 0 (N) or -1 (R) just return 1 
        if (gear < 1) {
            return 1;
        }
        // check if the RPM value of car is greater than the one suggested 
        // to shift up the gear from the current one     
        if (gear < 6 && rpm >= gearUp[gear - 1]) {
            return gear + 1;
        } else // check if the RPM value of car is lower than the one suggested 
        // to shift down the gear from the current one
        if (gear > 1 && rpm <= gearDown[gear - 1]) {
            return gear - 1;
        } else // otherwhise keep current gear
        {
            return gear;
        }
    }
    
    @Override
    public void setGearsPreferences(GearPreference gp){
        gearUp = convertIntegers(gp.gearUps);
        gearDown = convertIntegers(gp.gearDowns);
    }
    
    private int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i);
        }
        return ret;
    }
}
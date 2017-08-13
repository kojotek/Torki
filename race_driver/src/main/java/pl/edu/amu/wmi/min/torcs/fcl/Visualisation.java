
package pl.edu.amu.wmi.min.torcs.fcl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Circle;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class Visualisation {
   
    
    class DrawingComponent extends JComponent{
        
        public Point2D.Double center;
        public float scaleFactor;
        public float carWidth, carHeight;
        public List<Point2D.Double> leftRoadPoints;
        public List<Point2D.Double> rightRoadPoints;
        public List<Point2D.Double> estimatedRoadPoints;
        private List<Point2D.Double> racingLine;
        int circleSize = 8;
        
        DrawingComponent() {
            this.center = new Point2D.Double(640.0f/2.0f, 400.0f);
            leftRoadPoints = new ArrayList<Point2D.Double>();
            rightRoadPoints = new ArrayList<Point2D.Double>();
            estimatedRoadPoints = new ArrayList<Point2D.Double>();
            racingLine = new ArrayList<Point2D.Double>();
            carWidth = 2.0f;
            carHeight = 4.6f;
            scaleFactor = 5.0f;
            
        }
        
        @Override
        public void paintComponent(Graphics g){
            paintComponentBusy = true;
            
            Graphics2D g2 = (Graphics2D) g;

            Rectangle car = new Rectangle(
                    (int)center.x - (int)(carWidth*scaleFactor/2.0f),
                    (int)center.y - (int)(carHeight*scaleFactor/2.0f),
                    (int)(carWidth*scaleFactor),(int)(carHeight*scaleFactor));
            g2.setColor(Color.black);
            g2.draw(car);
            
            
            for (int i = 1; i < leftRoadPoints.size(); i++) {
                Line2D.Double line = new Line2D.Double(
                        leftRoadPoints.get(i).x *scaleFactor + center.x,
                        -leftRoadPoints.get(i).y *scaleFactor + center.y,
                        leftRoadPoints.get(i-1).x *scaleFactor + center.x,
                        -leftRoadPoints.get(i-1).y *scaleFactor + center.y);
                g2.setColor(Color.blue);
                g2.draw(line);
                
                g2.fillOval(new Double(leftRoadPoints.get(i).x * scaleFactor + center.x).intValue() - (circleSize/2),
                        new Double(-leftRoadPoints.get(i).y * scaleFactor + center.y).intValue() - (circleSize/2),
                        circleSize, circleSize);
            }
            
            for (int i = 1; i < rightRoadPoints.size(); i++) {
                Line2D.Double line = new Line2D.Double(
                        rightRoadPoints.get(i).x *scaleFactor + center.x,
                        -rightRoadPoints.get(i).y *scaleFactor + center.y,
                        rightRoadPoints.get(i-1).x *scaleFactor + center.x,
                        -rightRoadPoints.get(i-1).y *scaleFactor + center.y);
                g2.setColor(Color.red);
                g2.draw(line);
                
                g2.fillOval(new Double(rightRoadPoints.get(i).x * scaleFactor + center.x).intValue() - (circleSize/2),
                        new Double(-rightRoadPoints.get(i).y * scaleFactor + center.y).intValue() - (circleSize/2),
                        circleSize, circleSize);
            }
            
            for (int i = 1; i < racingLine.size(); i++) {
                Line2D.Double line = new Line2D.Double(
                        racingLine.get(i).x *scaleFactor + center.x,
                        -racingLine.get(i).y *scaleFactor + center.y,
                        racingLine.get(i-1).x *scaleFactor + center.x,
                        -racingLine.get(i-1).y *scaleFactor + center.y);
                g2.setColor(Color.magenta);
                g2.draw(line);
                
                
                g2.fillOval(new Double(racingLine.get(i).x * scaleFactor + center.x).intValue() - (circleSize/2),
                        new Double(-racingLine.get(i).y * scaleFactor + center.y).intValue() - (circleSize/2),
                        circleSize, circleSize);
            }
            
            for (int i = 0; i < 31; i+=5) {
                Line2D.Double line = new Line2D.Double(
                        -100.0f *scaleFactor + center.x,
                        -i *scaleFactor + center.y,
                        100.0f *scaleFactor + center.x,
                        -i *scaleFactor + center.y);
                g2.setColor(Color.black);
                g2.draw(line);
            }
            
            paintComponentBusy = false;
        }
    }
    
    DrawingComponent dc;
    JFrame window;
    volatile boolean paintComponentBusy = false; 
    
    public Visualisation(){
        dc = new DrawingComponent();
        window = new JFrame();
        window.setSize(640, 480);
        window.setTitle("Visualisation");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        window.add(dc);
        window.repaint();
    }
    
    public void Redraw(List<Point2D.Double> leftPoints, List<Point2D.Double> rightPoints, List<Point2D.Double> racingLine){
        if (!paintComponentBusy){
            dc.leftRoadPoints = new ArrayList<>(leftPoints);
            dc.rightRoadPoints = new ArrayList<>(rightPoints);
            dc.racingLine = new ArrayList<>(racingLine);
            window.repaint();
        }

    }
    
}

package com.qualcomm.ftcrobotcontroller.Autonomous;

import com.qualcomm.ftcrobotcontroller.systems.ButtonPusher;
import com.qualcomm.ftcrobotcontroller.systems.ClimberDepositor;
import com.qualcomm.ftcrobotcontroller.systems.MyDirection;
import com.qualcomm.ftcrobotcontroller.systems.Necessities;
import com.qualcomm.ftcrobotcontroller.systems.Wheels;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

/**
 * Created by benorgera on 12/22/15.
 */

public class AutonomousImplementation {

    private Necessities n;
    private Wheels wheels;
    private OpticalDistanceSensor ods;
    private ColorSensor leftBottom;
    private ColorSensor rightBottom;
    private ButtonPusher pusher;
    private MyDirection color;
    private ClimberDepositor depositor;
    private Stage stage = AutonomousImplementation.Stage.PRIMARY_SENSOR_CONTACT;
    
    private String data = "";
    
    private int whiteSignalThreshold = 12; //this signal must be read by color sensors for them to be considered as on the white line
    
    private double distanceThreshold = 0.2; //this signal must be read by the ods for it to be considered as touching the beacon
    

    public AutonomousImplementation(Necessities n, Wheels wheels, OpticalDistanceSensor ods, ColorSensor leftBottom, ColorSensor rightBottom, ButtonPusher pusher, ClimberDepositor depositor, MyDirection color) {
        this.n = n;
        this.ods = ods;
        this.pusher = pusher;
        this.wheels = wheels;
        this.color = color;
        this.depositor = depositor;
        this.leftBottom = leftBottom;
        this.rightBottom = rightBottom;
    }

    public void run() {

        while (ods.getLightDetected() < distanceThreshold) { //once we have reached the beacon, this condition is no longer true

            //green is used as a measure of the white line because blue and red are found on the field, while green is not
            int left = leftBottom.green();
            int right = rightBottom.green();

            //primary: first sensor to cross the white line
            //secondary: second sensor to cross the white line
            int primary = color == MyDirection.BLUE ? left : right;
            int secondary = color == MyDirection.BLUE ? right : left;

            switch (stage) {
                case PRIMARY_SENSOR_CONTACT:
                    processPrimarySensorContact(primary);
                    break;
                case SECONDARY_SENSOR_CONTACT:
                    processSecondSensorContact(secondary);
                    break;
                case WAITING_FOR_PRIMARY_TO_SURPASS_SECONDARY:
                    processWaitingForPrimaryToSurpassSecondary(primary, secondary);
                    break;
                case DUAL_SENSOR_CONTACT:
                    processDualSensorContact(left, right);
                    break;
            }

            n.syso(data, "DATA");
            n.syso("" + ods.getLightDetected(), "ODS");
            n.syso("" + left, "LEFT");
            n.syso("" + right, "RIGHT");

            n.waitCycle();
        }

        wheels.stop();

        n.syso("Beacon Reached", "DATA");

        n.sleep(1000); //wait for robot to stabilize

        depositor.drop(); //drop the climbers

        n.syso("Climbers Dropped", "DATA");

    }

    private void processPrimarySensorContact(int primary) {
        
        if (primary > whiteSignalThreshold) { //primary sensor is on the white line
            wheels.stop();
            data = "Made Primary Sensor Contact";
            stage = Stage.SECONDARY_SENSOR_CONTACT;
            n.sleep(500); //wait for robot to stop
        } else { //primary sensor is not yet on the white line
            strongStraight();
            data = "Waiting For Primary Sensor Contact";
        }

    }

    private void processSecondSensorContact(int secondary) {
        
        if (secondary > whiteSignalThreshold) { //secondary sensor is on the white line
            wheels.stop();
            data = "Made Secondary Sensor Contact";
            stage = Stage.WAITING_FOR_PRIMARY_TO_SURPASS_SECONDARY;
            n.sleep(500); //wait for robot to stop
        } else { //secondary sensor is not yet on the white line
            weakStraight();
            data = "Waiting For Secondary Sensor Contact";
        }

    }


    private void processWaitingForPrimaryToSurpassSecondary(int primary, int secondary) {
        
        if (primary >= secondary) { //robot parallel to the line or has surpassed parallel
            wheels.stop();
            data = "Primary Surpassed Secondary";
            stage = Stage.DUAL_SENSOR_CONTACT;
            depositor.swing(); //bring out the preload arm
            n.sleep(500); //wait for robot to stop
        } else { //robot not yet parallel to the line
            if (color == MyDirection.BLUE) {
                strongRight();
            } else {
                strongLeft();
            }
            data = "Waiting For Primary To Surpass Secondary";
        }

    }

    private void processDualSensorContact(int left, int right) {
        
        if (Math.abs(left - right) <= 1) { //both sensors equally on the white line
            strongStraight();
            data = "Dual Sensor Contact Close To Equal";
        } else if (left > right) { //left more on the white line
            slightLeft();
            data = "Dual Sensor Contact Left Stronger";
        } else { //right more on the white line
            slightRight();
            data = "Dual Sensor Contact Right Stronger";
        }
        
    }


    private void strongRight() {
        wheels.drive(0.3, -0.3);
    }

    private void slightRight() {
        wheels.drive(0.4, 0.0);
    }

    private void strongLeft() {
        wheels.drive(-0.3, 0.3);
    }

    private void slightLeft() {
        wheels.drive(0.0, 0.4);
    }

    private void strongStraight() {
        wheels.drive(0.2, 0.2);
    }

    private void weakStraight() {
        wheels.drive(0.15, 0.15);
    }

    private enum Stage {
        PRIMARY_SENSOR_CONTACT, SECONDARY_SENSOR_CONTACT, WAITING_FOR_PRIMARY_TO_SURPASS_SECONDARY, DUAL_SENSOR_CONTACT
    }
}

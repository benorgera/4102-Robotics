package com.qualcomm.ftcrobotcontroller.Autonomous;

import android.graphics.Color;
import com.qualcomm.ftcrobotcontroller.systems.ButtonPusher;
import com.qualcomm.ftcrobotcontroller.systems.ClimberDepositor;
import com.qualcomm.ftcrobotcontroller.systems.MyDirection;
import com.qualcomm.ftcrobotcontroller.systems.Necessities;
import com.qualcomm.ftcrobotcontroller.systems.Wheels;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

/**
 * Created by benorgera on 12/22/15.
 */

public class AutonomousImplementation {

    private boolean firstDrive = true; //true if this is the initial drive straight

    private Necessities n;
    private Wheels wheels;
    private OpticalDistanceSensor ods;
    private ColorSensor leftBottom;
    private ColorSensor rightBottom;
    private ButtonPusher pusher;
    private MyDirection color;
    private ClimberDepositor depositor;

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

        while (ods.getLightDetected() < 0.3) {

            int left = leftBottom.green();
            int right = rightBottom.green();
            String mode = null;


            if (left < 11 && right < 11) { //not at all on the line
                straight();
                mode = "Straight b/c not on line";

            } else {

                if (firstDrive) {
                    if (color == MyDirection.BLUE) {
                        strongRight();
                    } else {
                        strongLeft();
                    }
                    n.sleep(100);
                }

                firstDrive = false; //no longer the first drive

                int difference = Math.abs(left - right); //how far off are they

                int lesser = right < left ? right : left;
                int greater = right > left ? right : left;

                if (difference < 1.5 && left > 14 && right > 14) { //they are both definitely on the white (greater than 14) and pretty close to eachother
                    straight();
                    mode = "Weak straight b/c both on line";
                } else if (lesser < 8) { //one of them is completely off the line
                    if (lesser == left) {
                        strongRight();
                        mode = "Strong right b/c left off line";
                    } else {
                        strongLeft();
                        mode = "Strong left b/c right off line";
                    }
                } else {
                    if (left > right) {
                        slightRight();
                        mode = "Slight right b/c left weaker";
                    } else {
                        slightLeft();
                        mode = "Slight left b/c right weaker";
                    }

                }

                n.syso(mode, "MODE");

                n.syso("" + ods.getLightDetected(), "ODS");

                n.waitCycle();

            }

        }


    }



    private void straight() {
        double power = 0.2;

        wheels.drive(power, power);
    }

    private void strongRight() {
        wheels.drive(0.3, -0.3);
    }

    private void slightRight() {
        wheels.drive(0.3, 0.1);
    }

    private void strongLeft() {
        wheels.drive(-0.3, 0.3);
    }

    private void slightLeft() {
        wheels.drive(0.1, 0.3);
    }
}

package com.qualcomm.ftcrobotcontroller.systems;

import com.qualcomm.ftcrobotcontroller.opmodes.OpMode4102;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by benorgera on 10/31/15.
 */
public class AllClearSignHitters {

    private Servo servoLeft;
    private Servo servoRight;
    private OpMode4102 opMode;

    private Necessities n;

    private boolean isHammeringLeft = false;
    private boolean isHammeringRight = false;

    private boolean isDownR = false;
    private boolean isDownL = false;

    private boolean rHasntDoubledYet = true;
    private boolean lHasntDoubledYet = true;

    private int leftPreviousSwitch = 0;
    private int rightPreviousSwitch = 0;

    private int switchThreshold = 20; //number of hardware cycles it takes servo to move 90 degrees

    private int holdThreshold = 20; //number of cycles you let servo push


    public AllClearSignHitters(Servo servoLeft, Servo servoRight, Necessities n) {
        this.servoRight = servoRight;
        this.n = n;
        this.servoLeft = servoLeft;
        servoLeft.setPosition(0);
        servoRight.setPosition(1);
    }

    public void toggleLeft() { //moves left or right arm up or down
        isHammeringLeft = !isHammeringLeft;
    }

    public void toggleRight() {
        isHammeringRight = !isHammeringRight;
    }

    public boolean isHammeringLeft() {
        return isHammeringLeft;
    }

    public boolean isHammeringRight() {
        return isHammeringRight;
    }

    public void hammerLeft(int count) {
        if (count - leftPreviousSwitch >= switchThreshold) {
            if (isDownL && lHasntDoubledYet) {
                lHasntDoubledYet = false;
                return;
            }
            lHasntDoubledYet = true;
            leftPreviousSwitch = count;
            servoLeft.setPosition(isDownL ? .5 : 0);
            isDownL = !isDownL;
        }
    }

    public void hammerRight(int count) {
        if (count - rightPreviousSwitch >= switchThreshold) {
            if (isDownR && rHasntDoubledYet) {
                rHasntDoubledYet = false;
                return;
            }
            rHasntDoubledYet = true;
            rightPreviousSwitch = count;
            servoRight.setPosition(isDownR ? .5 : 1);
            isDownR = !isDownR;
        }
    }

    public void raise() {
        stopHammering();
        servoLeft.setPosition(1);
        servoRight.setPosition(0);
    }

    public void lower() {
        stopHammering();
        servoLeft.setPosition(0);
        servoRight.setPosition(1);
    }

    private void stopHammering() {
        isHammeringRight = false;
        isHammeringLeft = false;
    }

}
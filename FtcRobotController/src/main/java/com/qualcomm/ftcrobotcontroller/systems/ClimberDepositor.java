package com.qualcomm.ftcrobotcontroller.systems;


import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by benorgera on 10/31/15.
 */
public class ClimberDepositor {

    private Servo swingServo;
    private Servo dropServo;

    MyDirection dropPosition = MyDirection.CLOSED;
    MyDirection swingPosition = MyDirection.CLOSED;

    private final double[] swingPositions = {Servo.MAX_POSITION, Servo.MIN_POSITION}; //swing arm positions (in and out respectively)
    private final double[] dropPositions = {Servo.MIN_POSITION + .2, Servo.MAX_POSITION}; //drop door positions (hold and drop respectively)

    public ClimberDepositor(Servo swingServo, Servo dropServo) {
        this.swingServo = swingServo;
        this.dropServo = dropServo;

        neutralizeServos();
    }

    private void neutralizeServos() { //sets servos to neutral position (arm in, door holding)
        swingServo.setPosition(swingPositions[0]);
        dropServo.setPosition(dropPositions[0]);
    }


    public void swing() {
        if (swingPosition == MyDirection.OPEN) {
            swingServo.setPosition(1);
            swingPosition = MyDirection.CLOSED;
        } else {
            swingServo.setPosition(0);
            swingPosition = MyDirection.OPEN;
        }
    }


    public void drop() {
        if (dropPosition == MyDirection.CLOSED) { //its closed, open it
            dropServo.setPosition(dropPositions[1]);
            dropPosition = MyDirection.OPEN;
        } else { //its open, close it
            dropServo.setPosition(dropPositions[0]);
            dropPosition = MyDirection.CLOSED;
        }
    }



}
package com.qualcomm.ftcrobotcontroller.systems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by benorgera on 10/29/15.
 */
public class Arm {

    private DcMotor motor;
    private Servo latchServo;
    private Servo armHolder;


    private boolean latched = false;

    public Arm(Servo latchServo, Servo armHolder, DcMotor motor) {
        this.motor = motor;
        this.latchServo = latchServo;
        this.armHolder = armHolder;

        motor.setMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);

        motor.setDirection(DcMotor.Direction.REVERSE);

        latchServo.setPosition(0.12);

        armHolder.setPosition(0.7);
    }

    public void spool(MyDirection direction) {

        armHolder.setPosition(0);
        motor.setPower(direction == MyDirection.UP ? 1 : -1);

    }

    public void stop() {
        motor.setPower(0);
    }


    public void latch() {
        if (latched) {
            latched = false;
            latchServo.setPosition(0.12);
        } else {
            latched = true;
            latchServo.setPosition(1);
        }

    }







}
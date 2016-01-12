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
    private Necessities n;


    private boolean latched = false;

    public Arm(Servo latchServo, DcMotor motor, Necessities n) {
        this.motor = motor;
        this.n = n;
        this.latchServo = latchServo;

        motor.setMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);

        motor.setDirection(DcMotor.Direction.REVERSE);

        latchServo.setPosition(0.12);
    }

    public void spool(MyDirection direction) {
        motor.setPower(direction == MyDirection.UP ? 1 : -1);
        n.syso("Spooling Arm " + (direction == MyDirection.UP ? "Out" : "In"), "Arm Data");
    }

    public void stop() {
        motor.setPower(0);
    }


    public void latch() {
        if (latched) {
            latched = false;
            latchServo.setPosition(0.12);
            n.syso("Latches Unlatched", "Arm Data");
        } else {
            latched = true;
            latchServo.setPosition(1);
            n.syso("Latches Latched", "Arm Data");
        }

    }







}
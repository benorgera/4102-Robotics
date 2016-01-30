package com.qualcomm.ftcrobotcontroller.systems;

import com.qualcomm.ftcrobotcontroller.opmodes.OpMode4102;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by benorgera on 10/31/15.
 */
public class AllClearSignHitters {

    private DcMotor motor;
    private OpMode4102 opMode;

    private Necessities n;

    private double movePower = 0.1;

    public AllClearSignHitters(DcMotor motor, Necessities n) {
        this.motor = motor;
        this.motor.setDirection(DcMotor.Direction.REVERSE);
    }

    public void raise() {
        motor.setPower(movePower);
    }

    public void lower() {
        motor.setPower(movePower * -1);
    }

    public void swing() {
        motor.setPower(-1);
    }

    public void neutralize() {
        motor.setPower(0);
    }


}
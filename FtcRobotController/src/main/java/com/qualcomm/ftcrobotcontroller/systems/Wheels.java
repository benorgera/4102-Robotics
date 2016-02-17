package com.qualcomm.ftcrobotcontroller.systems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;

/**
 * Created by benorgera on 11/1/15.
 */
public class Wheels {

    private boolean isBackward = false;

    private boolean isHolding = false;

    private double holdingPower = -0.16;

    private DcMotor left;

    private DcMotor right;

    private Necessities n;

    private final double minHypotenuseLength = 0.2; //lowest value the hypotenuse can be in order to turn

    public Wheels(DcMotor left, DcMotor right, Necessities n) {

        left.setDirection(DcMotor.Direction.REVERSE);

        this.n = n;

        this.right = right;

        this.left = left;

    }

    public void stop() { //stops the motors because the robot isn't being driven

        left.setPower(isHolding ? holdingPower : 0);
        right.setPower(isHolding ? holdingPower : 0);

        n.syso("Robot is stopped", "Wheels Data:");

    }

    public void drive(double leftWheelsPower, double rightWheelsPower) {

        if (leftWheelsPower > 1) leftWheelsPower = 1;

        if (leftWheelsPower < -1) leftWheelsPower = -1;

        if (rightWheelsPower > 1) rightWheelsPower = 1;

        if (rightWheelsPower < -1) rightWheelsPower = -1;


        n.syso("Left wheels set to power " + leftWheelsPower, "Wheels Data:");


        left.setPower(leftWheelsPower);

        n.syso("Right wheels set to power " + rightWheelsPower, "Wheels Data:");

        right.setPower(rightWheelsPower);

    }

    public void processMainstream(double x, double y) { //processes joystick coordinates into directions and powers of wheels, not accounting for angle

        Double degreesOfStick = getDegreesOfStick(x, y);

        n.syso("Stick angle is " + degreesOfStick, "Wheels Data:");

        Double hypotenuseLength = Math.sqrt(y * y + x * x);
        n.syso("Stick magnitude is " + hypotenuseLength, "Wheels Data:");

        Double magnitudeAsPower = hypotenuseLength;

        n.syso("Stick magnitude as power is " + hypotenuseLength, "Wheels Data:");


        if (x == 0 && y == 0) {
            n.syso("Stick neutral, not driving", "Wheels Data:");
            stop();

        } else if (hypotenuseLength <= minHypotenuseLength && (Math.abs(degreesOfStick) <= 45 || Math.abs(degreesOfStick - 180) <= 45)) { //hypotenuse too short and you're at a turn angle, do nothing
            stop();
            n.syso("Not driving because magnitude " + magnitudeAsPower + " too low and stick angle " + degreesOfStick + " is in a turn zone", "Wheels Data:");

        } else if ((degreesOfStick <= 45 && degreesOfStick >= 0) || (degreesOfStick >= 315 && degreesOfStick <= 360)) { //right turn

            drive(magnitudeAsPower, magnitudeAsPower * -1);

            n.syso("Driving right at power " + magnitudeAsPower, "Wheels Data:");

        } else if (degreesOfStick >= 135 && degreesOfStick <= 225) { //left turn

            drive(magnitudeAsPower * -1, magnitudeAsPower);

            n.syso("Driving left at power " + magnitudeAsPower, "Wheels Data:");

        } else if (degreesOfStick >= 45 && degreesOfStick <= 135) { //drive forward

            drive(magnitudeAsPower * (isBackward ? -1 : 1), magnitudeAsPower * (isBackward ? -1 : 1));

            n.syso("Driving straight at power " + magnitudeAsPower, "Wheels Data:");

        } else if (degreesOfStick >= 225 && degreesOfStick <= 315) { //drive backward

            drive(magnitudeAsPower * (isBackward ? 1 : -1), magnitudeAsPower * (isBackward ? 1 : -1));

            n.syso("Driving backwards at power " + magnitudeAsPower, "Wheels Data:");
        } else {

            n.syso("Error, no driving circumstances satisfied with joystick angle " + degreesOfStick + " and magnitude " + hypotenuseLength, "Wheels Data:");
        }

    }


    private double getDegreesOfStick(double x, double y) {

        Double degreesOfStick;
        if (x >= 0 && y >= 0) {

            degreesOfStick = Math.atan(y / x) * 180 / Math.PI;

        } else if ((x <= 0 && y >= 0) || (x < 0 && y <= 0)) {

            degreesOfStick = Math.atan(y / x) * 180 / Math.PI + 180;

        } else {

            degreesOfStick = Math.atan(y / x) * 180 / Math.PI + 360;
        }

        return (degreesOfStick == -0.0 ? -1 * degreesOfStick : degreesOfStick); //if -0, make 0

    }


    public void toggleBackward(boolean isBackward) {
        this.isBackward = isBackward;
    }

    public void toggleHolding(boolean isHolding) { this.isHolding = isHolding; }

}
package com.qualcomm.ftcrobotcontroller.systems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;

/**
 * Created by benorgera on 11/1/15.
 */
public class Wheels {

    private boolean isBackward = false;

    private DcMotor left;

    private DcMotor right;

    private Necessities n;

    private final double minHypotenuseLength = 0.2; //lowest value the hypotenuse can be in order to turn

    private DcMotorController controller;


    public Wheels(DcMotor left, DcMotor right, Necessities n) {

        controller = left.getController();

        left.setDirection(DcMotor.Direction.REVERSE);

        this.n = n;

        this.right = right;

        this.left = left;

    }

    public void stop() { //stops the motors because the robot isn't being driven

        left.setPower(0);
        right.setPower(0);

        n.syso("Robot is stopped", "Wheels Data:");

    }

    public void drive(double leftWheelsPower, double rightWheelsPower) {

        controller.setMotorControllerDeviceMode(DcMotorController.DeviceMode.SWITCHING_TO_WRITE_MODE);

        if (leftWheelsPower > 1) leftWheelsPower = 1;

        if (leftWheelsPower < -1) leftWheelsPower = -1;

        if (rightWheelsPower > 1) rightWheelsPower = 1;

        if (rightWheelsPower < -1) rightWheelsPower = -1;


        n.syso("Left wheels set to power " + leftWheelsPower, "Wheels Data:");


        left.setPower(leftWheelsPower);

        n.syso("Right wheels set to power " + rightWheelsPower, "Wheels Data:");

        right.setPower(rightWheelsPower);

    }


    private void setPowers(double power) { //sets power of all motors to the given power

        n.syso("Power set to power " + power, "Wheels Data");

        left.setPower(power);
        right.setPower(power);

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

    private void forceResetEncoders() {

        if (left.getCurrentPosition() != 0 || right.getCurrentPosition()!= 0) { //they're not 0
            left.setMode(DcMotorController.RunMode.RESET_ENCODERS); //reset encoder positions to 0
            right.setMode(DcMotorController.RunMode.RESET_ENCODERS);

            while (left.getCurrentPosition() != 0 || right.getCurrentPosition()!= 0) { //wait until they're at 0
                n.waitCycle();
            }
        }
    }

    public void turn(double degrees, double skew,  GyroSensor gyro) { //right is positive, left is negative

        forceHeadingReset(gyro);

        double power;

        double minPower = 0.16;

        boolean degreesIsGreaterThanZero = (degrees > 0.0);

        double startSlowingAt = 4.0 / minPower;

        double holdingPower = -0.3;

        double threshold = (degreesIsGreaterThanZero ? (degrees - skew) : (degrees + skew));

        while (Math.abs(gyro.getHeading()) < Math.abs(threshold)) {

            double degreesRemaining = Math.abs(threshold) - Math.abs(gyro.getHeading()); //calculate remaining degrees

            power = (degreesRemaining / startSlowingAt); //power is a scale of remaining distance, at startSlowingAt degrees remaining we start slowing down

            if (power > 1.0) power = 1.0; //too high a power calculated

            if (power < minPower) power = minPower; //too low a power calculated

            left.setPower(degreesIsGreaterThanZero ? power : holdingPower);
            right.setPower(degreesIsGreaterThanZero ? holdingPower : power);
            n.syso("Position: " + gyro.getHeading() + "\nTarget: " + threshold + "\nPower: " + power, "Autonomous Turn");
            wait(1);
            waitAndStop(2);

        }
        stop();

    }

    private void waitAndStop(int w) {
        for (int i = 0; i < w; i ++) {
            n.waitCycle();
            stop();
        }
    }

    private void wait(int w) {
        for (int i = 0; i < w; i ++) {
            n.waitCycle();
        }
    }


    private void forceHeadingReset(GyroSensor gyro) {

        gyro.resetZAxisIntegrator(); //reset heading

        int count = 0;
        while (gyro.getHeading() != 0) {
            gyro.resetZAxisIntegrator();
            count++;
            n.syso("Waited " + count + " cycles to reset z axis integrator", "TURN DATA");
            n.waitCycle();
        }

    }

    public void driveStraight(int rotations, double power) { //set target position, runtoposition, set power

        forceResetEncoders();

        n.setMotorMode(left, DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
        n.setMotorMode(right, DcMotorController.RunMode.RUN_USING_ENCODERS);

        n.syso("" + right.getCurrentPosition(), "Right Current Position");

        while (Math.abs(right.getCurrentPosition()) < rotations) {
            drive(power, power);
            n.syso("Current Position: " + right.getCurrentPosition() + "\nTarget Position: " + rotations + "\nPower: " + power, "Drive Straight");
        }

        stop();

        n.syso("" + right.getCurrentPosition(), "Right Current Position After Loop");

    }

    public void toggleBackward(boolean isBackward) {
        this.isBackward = isBackward;
    }


}
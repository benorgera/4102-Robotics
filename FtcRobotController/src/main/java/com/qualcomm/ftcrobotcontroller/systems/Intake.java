package com.qualcomm.ftcrobotcontroller.systems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by benorgera on 10/31/15.
 */
public class Intake {

    private DcMotor intakeMotor;
    private Servo frontDoorServo;
    private DcMotor beltMotor;
    private Servo leftDoorServo;
    private Servo rightDoorServo;

    private final double troughPower = 0.25;
    private final double[] frontDoorPositions = {Servo.MAX_POSITION, Servo.MIN_POSITION}; //front door positions (closed, open respectively)
    private final double[] leftDoorPositions = {Servo.MIN_POSITION + .45, Servo.MAX_POSITION}; //trough left door positions (closed, open respectively)
    private final double[] rightDoorPositions = {Servo.MAX_POSITION - .45, Servo.MIN_POSITION}; //trough right door positions (closed, open respectively)

    private Necessities n;

    public Intake(DcMotor intakeMotor, DcMotor beltMotor, Servo frontDoorServo, Servo leftDoorServo, Servo rightDoorServo, Necessities n) {

        this.intakeMotor = intakeMotor;
        this.beltMotor = beltMotor;
        this.frontDoorServo = frontDoorServo;
        this.leftDoorServo = leftDoorServo;
        this.rightDoorServo = rightDoorServo;
        this.n = n;

        frontDoorServo.setPosition(frontDoorPositions[0]);
        leftDoorServo.setPosition(leftDoorPositions[0]);
        rightDoorServo.setPosition(rightDoorPositions[0]);

        intakeMotor.setChannelMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
        beltMotor.setChannelMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);

        intakeMotor.setDirection(DcMotor.Direction.FORWARD);

    }

    public void startIntake() { //starts intake, opening front door and turning on brushes

        frontDoorServo.setPosition(frontDoorPositions[1]);
        n.syso("Door opened", "Intake Data:");
        intakeMotor.setPower(1.0);
        n.syso("Debris pickup began", "Intake Data:");
    }

    public void stopIntake() { //stops intake, closing front door and turning off brushes

        frontDoorServo.setPosition(frontDoorPositions[0]);
        n.syso("Door closed", "Intake Data");
        intakeMotor.setPower(1.0);
        n.syso("Debris pickup ended", "Intake Data:");

    }

    public void drop(MyDirection whichSide) { //tilts trough to one side and opens corresponding door, or closes door and neutralizes tilt

        leftDoorServo.setPosition(leftDoorPositions[whichSide == MyDirection.LEFT ? 1 : 0]);
        rightDoorServo.setPosition(rightDoorPositions[whichSide == MyDirection.RIGHT ? 1 : 0]);

        n.syso((whichSide == MyDirection.LEFT ? "Left" : "Right") + " trough door opened\n\t" + (whichSide == MyDirection.LEFT ? "Right" : "Left") + " trough door closed", "Intake Data:");

        beltMotor.setDirection(whichSide == MyDirection.RIGHT ? DcMotor.Direction.FORWARD : DcMotor.Direction.REVERSE); //sets belt motor to proper direction to drop debris
        beltMotor.setPower(troughPower); //powers belt motor

        n.syso("Belt Motor dropping to the " + (whichSide == MyDirection.LEFT ? "left" : "right"), "Intake Data:");
    }

    public void stopDrop() { //ends the dropping of debrise

        beltMotor.setPower(0); //motor stopped

        //doors closed
        leftDoorServo.setPosition(leftDoorPositions[0]);
        rightDoorServo.setPosition(rightDoorPositions[0]);

        n.syso("Belt Motor stopped and doors shut", "Intake Data:");

    }

    public void shiftTrough(MyDirection direction) { //shifts trough without moving doors up

        beltMotor.setDirection(direction == MyDirection.RIGHT ? DcMotor.Direction.FORWARD : DcMotor.Direction.REVERSE);

        beltMotor.setPower(troughPower);
    }

    public void unjam() {
        intakeMotor.setPower(-1.0);
    }



}
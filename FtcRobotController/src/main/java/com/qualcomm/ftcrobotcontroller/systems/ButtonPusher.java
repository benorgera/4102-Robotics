package com.qualcomm.ftcrobotcontroller.systems;

import com.qualcomm.ftcrobotcontroller.systems.MyDirection;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.LED;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.Objects;

/**
 * Created by benorgera on 10/29/15.
 */
public class ButtonPusher {

    private Servo servo;
    private ColorSensor colorSensorRight;
    private ColorSensor colorSensorLeft;

    private LED leftLED;
    private LED rightLED;

    private final double strengthThreshold = 200.0; //color signal needed to justify a button push
    private Necessities n;

    private final double leftPushPosition = Servo.MAX_POSITION;
    private final double rightPushPosition = Servo.MIN_POSITION;
    private final double neutralPosition = (Servo.MIN_POSITION + Servo.MAX_POSITION) / 2;

    public ButtonPusher(Servo servo, ColorSensor colorSensorLeft, ColorSensor colorSensorRight, LED leftLED, LED rightLED, Necessities n) {

        this.servo = servo;
        this.colorSensorLeft = colorSensorLeft;
        this.colorSensorRight = colorSensorRight;
        this.leftLED = leftLED;
        this.rightLED = rightLED;
        this.n = n;
        servo.setPosition(neutralPosition);

        setLEDs(true); //turn on leds

        n.syso("Enabled LEDs", "Button Pusher Data");

        n.syso("Sleep ended", "Button Pusher Data");


    }

    private void setLEDs(boolean enabled) {
        leftLED.enable(enabled);
        rightLED.enable(enabled);
    }


    public boolean senseAndPush(MyDirection color) { //returns true if it pushed

        Object[] leftArray = getColorAndStrength(MyDirection.LEFT);

        Object[] rightArray = getColorAndStrength(MyDirection.RIGHT);

        if (leftArray[0] == rightArray[0]) return false; //if the same, return

        push(leftArray[0] == color ? MyDirection.LEFT : MyDirection.RIGHT); //we know they're different so push left if left is correct, otherwise push right

        //array[1], which is the strength of the color, is never checked in this implementation

        return true;
    }

    public Object[] getColorAndStrength(MyDirection side) { //return object array, 0 index is color and 1 index is strength
        //pass in left or right

        double redStrength = (side == MyDirection.LEFT ? colorSensorLeft.red() : colorSensorRight.red());
        double blueStrength = (side == MyDirection.LEFT ? colorSensorLeft.blue() : colorSensorRight.blue());

        return new Object[]{redStrength > blueStrength ? MyDirection.RED : MyDirection.BLUE, redStrength > blueStrength ? redStrength : blueStrength};
    }


    private void reset() { //sets button pusher to neutral position

        servo.setPosition(neutralPosition);

    }

    public void push(MyDirection direction) { //pushes button (max for left, min for right)

        if (direction == MyDirection.LEFT) {
            servo.setPosition(leftPushPosition);
        } else {
            servo.setPosition(rightPushPosition);
        }

        n.sleep(1000);

        reset();
    }


}
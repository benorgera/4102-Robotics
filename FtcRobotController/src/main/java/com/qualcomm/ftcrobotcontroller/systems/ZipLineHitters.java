package com.qualcomm.ftcrobotcontroller.systems;

import com.qualcomm.ftcrobotcontroller.chsopmodes.OpMode4102;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by benorgera on 10/31/15.
 */
public class ZipLineHitters {

    private Servo servo;
    private Servo servo2;
    private OpMode4102 opMode;

    private Necessities n;

    private boolean isOne = true;
    private boolean isOne2 = true;



    public ZipLineHitters(Servo servo, Servo servo2, Necessities n) {
        this.servo = servo;
        this.n = n;
        this.servo2 = servo2;
        servo.setPosition(0.3);
        servo2.setPosition(0.7);
    }

    public void toggleLeft() { //moves left or right arm up or down
        if (isOne) {
            servo2.setPosition(0);
            isOne = false;
        } else {
            servo2.setPosition(0.7);
            isOne = true;
        }
    }

    public void toggleRight() {
        if (isOne2) {
            servo.setPosition(1);
            isOne2 = false;
        } else {
            servo.setPosition(0.3);
            isOne2 = true;
        }
    }


}
/*

 ************************************************************************************
 * MIT License
 *
 * Copyright (c) 2023 Crunchlabs LLC (IRTurret Control Code) <- BtTurret is adapted from IRTurret
 * Copyright (c) 2020-2022 Armin Joachimsmeyer (IRremote Library) <- BtTurret is adapted from IRremote Library

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 ************************************************************************************
 */

//////////////////////////////////////////////////
               //  LIBRARIES  //
//////////////////////////////////////////////////
#include <Arduino.h>
#include <Servo.h>
#include "PinDefinitionsAndMore.h" // Define macros for input and output pin etc.
#include <IRremote.hpp>

#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>

#include <SoftwareSerial.h>// import the serial library

#define BUTTON_OFF_INDEX 100
#define SEEK_BAR_BASE_INDEX 0
#define SEEK_BAR_ARM_INDEX 1
#define SEEK_BAR_FIRE_INDEX 2
#define BUTTON_BASE_INDEX 10
#define BUTTON_ARM_INDEX 11
#define BUTTON_FIRE_INDEX 12

#define TRIGGER_MOVE_OFF 0
#define TRIGGER_MOVE_UP 1
#define TRIGGER_MOVE_DOWN 0
#define TRIGGER_MOVE_RIGHT 1
#define TRIGGER_MOVE_LEFT 0
#define TRIGGER_FIRE_ALL 1
#define TRIGGER_FIRE 0

#define SENT_DATA_SIZE 6
#define START_TRANSMIT_CHAR 0xFF88FF88

//////////////////////////////////////////////////
          //  PINS AND PARAMETERS  //
//////////////////////////////////////////////////
//this is where we store global variables!
Servo yawServo; //names the servo responsible for YAW rotation, 360 spin around the base
Servo pitchServo; //names the servo responsible for PITCH rotation, up and down tilt
Servo rollServo; //names the servo responsible for ROLL rotation, spins the barrel to fire darts

int pitchServoVal = 100;

int selectedIndex;
int selectedOption;
int sequence = 0;
SoftwareSerial bluetooth(2, 3); // TX, RX

// Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver(0x40, Wire);
// Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver(0x40);
Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();

//Servo0 175 - 375, 425 - 625
//Servo1 225 - 390 - 550
//Servo2 175 - 375, 425 - 625

#define FIRE_PWM 425
// #define FIRE_DELAY 10
#define FIRE_ALL_PWM 440
#define FIRE_ALL_DELAY 1300
#define FIRE_PWM_OFFSET_INITIAL 100

// int SERVO_BASE_PWMS[6] = {175, 375, 0, 425, 625};
#define BASE_PWMN_LEFT 375
#define BASE_PWMN_RIGHT 425
// #define BASE_DELAY 500
#define BASE_PWM_OFFSET_INITIAL 100


#define ARM_PWM_HOME 405
#define ARM_PWM_UP 550
#define ARM_PWM_DOWN 225
// #define ARM_PWM_MOVE 5
// #define ARM_DELAY 500
#define ARM_PWM_MOVE_INITIAL 1

int delayPWMMax = 5000; //maximum time (ms) pwm will stay on even if button pressed indefinitely
int firePWMOffset = FIRE_PWM_OFFSET_INITIAL; //PWM offset 
int basePWMOffset = BASE_PWM_OFFSET_INITIAL; //PWM value offset
int armPWMMove = ARM_PWM_MOVE_INITIAL;
int armPWMVal = ARM_PWM_HOME;
// bool pwmActive = false;

#define BASE_PIN 2
#define ARM_PIN 1
#define FIRE_PIN 0
#define SERVO_FREQ 75 // Analog servos run at ~50 Hz updates

//////////////////////////////////////////////////
               //  S E T U P  //
//////////////////////////////////////////////////
void setup() {
    Serial.begin(9600); // initializes the Serial communication between the computer and the microcontroller

    bluetooth.begin(9600);

    Serial.println("Bluetooth Turret active");
    // delay(100);

    // In theory the internal oscillator is 25MHz but it really isn't
    // that precise. You can 'calibrate' by tweaking this number till
    // you get the frequency you're expecting!
    pwm.begin();
    pwm.setOscillatorFrequency(27000000);  // The int.osc. is closer to 27MHz
    pwm.setPWMFreq(SERVO_FREQ);  // Analog servos run at ~50 Hz updates

    homeServos(); //set servo motors to home position
}

////////////////////////////////////////////////
               //  L O O P  //
////////////////////////////////////////////////

void loop() {
  delay(100);
  // Serial.println("-------------------");
  // Serial.print(bluetooth.available());
  // check for expected length of data
  if (bluetooth.available() >= SENT_DATA_SIZE)
  {
    sequence = bluetooth.read();
    //Serial.print("Sequence: ");
    Serial.print(sequence);
    //Confirming sequence prior to reading index and data
    //need second mask to get rid of signed bit shifting
    if (sequence == (((START_TRANSMIT_CHAR & 0xFF000000) >> 24) & 0x000000FF))
    {
      //delay(10);
      sequence = bluetooth.read();
      Serial.print(sequence);
      if (sequence == ((START_TRANSMIT_CHAR & 0x00FF0000) >> 16))
      {
        //delay(10);
        sequence = bluetooth.read();
        Serial.print(sequence);
        if (sequence == ((START_TRANSMIT_CHAR & 0x0000FF00) >> 8))
        {
          //delay(10);
          sequence = bluetooth.read();
          Serial.println(sequence);
          //final sequence
          if (sequence == ((START_TRANSMIT_CHAR & 0x000000FF) >> 0))
          {
            //delay(10);
            selectedIndex = bluetooth.read();
            Serial.print("Selected Index: ");
            Serial.println(selectedIndex);
            //delay(10);

            selectedOption = bluetooth.read();
            Serial.print("Selected Option: ");
            Serial.println(selectedOption);

            //determine the motor to change or actuate on/off (selectedIndex)
            //change motor to given value or determine direction of actuation (selectedOption)
            switch(selectedIndex) {
              case SEEK_BAR_BASE_INDEX:
                basePWMOffset = selectedOption;
                Serial.println("Changed base");
                break;
              
              case SEEK_BAR_ARM_INDEX:
                armPWMMove = selectedOption;
                Serial.println("Changed arm");
                break;

              case SEEK_BAR_FIRE_INDEX:
                firePWMOffset = selectedOption;
                Serial.println("Changed fire");
                break;

              case BUTTON_BASE_INDEX:
                // pwmActive = true;
                if (selectedOption == TRIGGER_MOVE_RIGHT) {
                  rightMove(1);
                  Serial.println("Moving right");
                }
                else {
                  leftMove(1);
                  Serial.println("Moving left");
                }
                // startTime = millis();
                break;

              case BUTTON_ARM_INDEX:
                // pwmActive = true;
                // pwmArmActve = true;
                if (selectedOption == TRIGGER_MOVE_UP) {
                  upMove(1);
                  Serial.println("Moving up");
                }
                else {
                  downMove(1);
                  Serial.println("Moving down");
                }
                // startTime = millis();
                break;

              case BUTTON_FIRE_INDEX:
                if (selectedOption == TRIGGER_FIRE) {
                  // pwmActive = true;
                  fire();
                  Serial.println("Fire");
                }
                else {
                  fireAll();
                  Serial.println("Fire all");
                  delay(50);
                }
                // startTime = millis();
                break;

              case BUTTON_OFF_INDEX:
                // pwmActive = false;
                turnOffMotors();
                Serial.println("Button released");
                break;

            }
            delay(5);
          }
        }
      }
    }
  }
  else if (selectedIndex == BUTTON_ARM_INDEX) {
    if (selectedOption == TRIGGER_MOVE_UP) {
      upMove(1);
    }
    else {
      downMove(1);
    }
  }
}


void leftMove(int moves){
    for (int i = 0; i < moves; i++){
        Serial.println("LEFT");
        pwm.setPWM(BASE_PIN, 0, BASE_PWMN_LEFT - basePWMOffset);
  }

}

void rightMove(int moves){ // function to move right
  for (int i = 0; i < moves; i++){
      Serial.println("RIGHT");
      pwm.setPWM(BASE_PIN, 0, BASE_PWMN_RIGHT + basePWMOffset);
  }
}

void upMove(int moves){
  for (int i = 0; i < moves; i++){
      armPWMVal += armPWMMove;
      if (armPWMVal < ARM_PWM_UP) {
        Serial.println("UP");
        pwm.setPWM(ARM_PIN, 0, armPWMVal);
      }
      else {
        armPWMVal = ARM_PWM_UP;
        Serial.println("Can't go higher");
      }
      Serial.println(armPWMVal);
  }
}

void downMove (int moves){
  for (int i = 0; i < moves; i++){
        armPWMVal -= armPWMMove;
        if (armPWMVal > ARM_PWM_DOWN) {
        Serial.println("DOWN");
        pwm.setPWM(ARM_PIN, 0, armPWMVal);
      }
      else {
        armPWMVal = ARM_PWM_DOWN;
        Serial.println("Can't go lower");
      }
      Serial.println(armPWMVal);
  }
}

/**
* fire does xyz
*/
void fire() { //function for firing a darts
    Serial.println("FIRING");
    pwm.setPWM(FIRE_PIN, 0, FIRE_PWM + firePWMOffset);
}

void fireAll() { //function to fire all 6 darts at once
    Serial.println("FIRING ALL");
    pwm.setPWM(FIRE_PIN, 0, FIRE_ALL_PWM + firePWMOffset);
    if (firePWMOffset < FIRE_PWM_OFFSET_INITIAL/2) {
      delay(FIRE_ALL_DELAY + 50*firePWMOffset);
    }
    else if (firePWMOffset < FIRE_PWM_OFFSET_INITIAL) {
      delay(FIRE_ALL_DELAY + 10*firePWMOffset);
    }
    else {
      delay(FIRE_ALL_DELAY);
    }
    Serial.println("DONE FIRING ALL");
    pwm.setPWM(FIRE_PIN, 0, 0);
}

void homeServos(){
    pwm.setPWM(FIRE_PIN, 0, 0);
    pwm.setPWM(BASE_PIN, 0, 0);
    pwm.setPWM(ARM_PIN, 0, ARM_PWM_HOME);
    armPWMVal = ARM_PWM_HOME;
    Serial.println("HOMING");
}

void turnOffMotors(){
  pwm.setPWM(FIRE_PIN, 0, 0);
  pwm.setPWM(BASE_PIN, 0, 0);
  Serial.println("TURNED OFF");
}

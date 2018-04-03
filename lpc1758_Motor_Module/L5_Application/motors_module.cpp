#include "motors_module.h"
#include "generated_can.h"
#include "lpc_pwm.hpp"
#include "io.hpp"
#include "gpio.hpp"
#include "utilities.h"

PWM SERVO_MOTOR(PWM::pwm1, 9.915);	//servo motor on P2.0 and 100Hz.
PWM DC_MOTOR(PWM::pwm2, 100);		//servo motor on P2.0 and 100Hz


bool servo_motor_steer_angle (motor_steer direction, int angle) // angle data-type to be  fixed
{
	// bool ret_status = 1;
    switch (direction)
    {
        case full_left_dir	:	SERVO_MOTOR.set(angle);	break;
        case slt_left_dir	:	SERVO_MOTOR.set(angle);	break;
        case straight_dir	:	SERVO_MOTOR.set(angle);	break;
        case slt_right_dir	:	SERVO_MOTOR.set(angle);	break;
        case full_right_dir	:	SERVO_MOTOR.set(angle);	break;
        default             :	{
        							SERVO_MOTOR.set(angle);
                            		return false;
                            	}
    }

    return true;
}


bool dc_motor_drive (motor_drive drive, int speed)
{
//  bool ret_status = 1;
    switch (drive)
    {
        case forward_drive	:	DC_MOTOR.set(speed);	break;
        case neutral_drive  :	DC_MOTOR.set(speed);	break;
        case reverse_drive  :	DC_MOTOR.set(speed);	break;
        default             :	{
        							DC_MOTOR.set(speed);
                                	return false;
                            	}
    }

    return true;
}



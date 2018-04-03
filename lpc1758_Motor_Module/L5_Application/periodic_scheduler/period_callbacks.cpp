/*
 *     SocialLedge.com - Copyright (C) 2013
 *
 *     This file is part of free software framework for embedded processors.
 *     You can use it and/or distribute it as long as this copyright header
 *     remains unmodified.  The code is free for personal use and requires
 *     permission to use in a commercial product.
 *
 *      THIS SOFTWARE IS PROVIDED "AS IS".  NO WARRANTIES, WHETHER EXPRESS, IMPLIED
 *      OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED WARRANTIES OF
 *      MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE APPLY TO THIS SOFTWARE.
 *      I SHALL NOT, IN ANY CIRCUMSTANCES, BE LIABLE FOR SPECIAL, INCIDENTAL, OR
 *      CONSEQUENTIAL DAMAGES, FOR ANY REASON WHATSOEVER.
 *
 *     You can reach the author of this software at :
 *          p r e e t . w i k i @ g m a i l . c o m
 */

/**
 * @file
 * This contains the period callback functions for the periodic scheduler
 *
 * @warning
 * These callbacks should be used for hard real-time system, and the priority of these
 * tasks are above everything else in the system (above the PRIORITY_CRITICAL).
 * The period functions SHOULD NEVER block and SHOULD NEVER run over their time slot.
 * For example, the 1000Hz take slot runs periodically every 1ms, and whatever you
 * do must be completed within 1ms.  Running over the time slot will reset the system.
 */

#include <stdint.h>
#include "printf_lib.h"
#include "io.hpp"
#include "periodic_callback.h"
#include "lpc_pwm.hpp"
#include "stdio.h"
#include "gpio.hpp"
#include "utilities.h"
#include "can.h"
#include "motors_module.h"
#include "_can_dbc/generated_can.h"

const uint32_t				MOTOR_SIGNAL__MIA_MS = 3000;
const MOTOR_SIGNAL_t 		MOTOR_SIGNAL__MIA_MSG = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0};

MOTOR_SIGNAL_t MOTOR_DATA;
can_t CAN_1 = can1;


/// This is the stack size used for each of the period tasks (1Hz, 10Hz, 100Hz, and 1000Hz)
const uint32_t PERIOD_TASKS_STACK_SIZE_BYTES = (512 * 4);


/**
 * This is the stack size of the dispatcher task that triggers the period tasks to run.
 * Minimum 1500 bytes are needed in order to write a debug file if the period tasks overrun.
 * This stack size is also used while calling the period_init() and period_reg_tlm(), and if you use
 * printf inside these functions, you need about 1500 bytes minimum
 */
const uint32_t PERIOD_MONITOR_TASK_STACK_SIZE_BYTES = (512 * 3);


/// Called once before the RTOS is started, this is a good place to initialize things once
bool period_init(void)
{

	if(CAN_init(CAN_1, 100, 80, 80, NULL, NULL))
	{
		u0_dbg_printf("init up\n");
		CAN_bypass_filter_accept_all_msgs();
		CAN_reset_bus(CAN_1);
	}

	MOTOR_DATA.mia_info.is_mia = false;

    return true; // Must return true upon success
}

/// Register any telemetry variables
bool period_reg_tlm(void)
{
    // Make sure "SYS_CFG_ENABLE_TLM" is enabled at sys_config.h to use Telemetry
    return true; // Must return true upon success
}



void period_1Hz(uint32_t count)
{
	if(CAN_is_bus_off(CAN_1))
	{
		CAN_reset_bus(CAN_1);
		//u0_dbg_printf("buss is off & reseted! \n");
	}
}

void period_10Hz(uint32_t count)
{

}

void period_100Hz(uint32_t count)
{
	can_msg_t message;
    //MOTOR_SIGNAL_t motor_data_received = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	if(CAN_rx(CAN_1, &message, 0))
	{
		dbc_msg_hdr_t header_motors = {0};

		header_motors.mid = message.msg_id;
		header_motors.dlc = message.frame_fields.data_len;
		dbc_decode_MOTOR_SIGNAL(&MOTOR_DATA, message.data.bytes, &header_motors);

	}
	else if(dbc_handle_mia_MOTOR_SIGNAL(&MOTOR_DATA, 10))
	{
		if(MOTOR_DATA.MOTOR_DRIVE_REVERSE == 1)					//Changes to be made for MIA condition...Currently, going reverse.
		{
			LD.setLeftDigit(1);
		}
		LE.toggle(4);
	}




	/*
	 ** DC Motor code - Get motor_drive & speed (MOTOR_DRIVE_SPEED) from Master module.
	 **	Return value of dc_motor_drive is boolean -> Can be used for head lights.
	 */

	//	OLD CODE:
	//	if (MOTOR_DATA.MOTOR_DRIVE_SPEED == 0)
	//	{
	//		DC_motor.set(10.1);
	//	}
	//	else if(motor_signal.MOTOR_DRIVE_SPEED == 5)
	//	{
	//		DC_motor.set(17.16);
	//	}
	//	else if(motor_signal.MOTOR_DRIVE_SPEED == 15)
	//	{
	//		DC_motor.set(17.26);
	//	}
	//	else if(motor_signal.MOTOR_DRIVE_SPEED == 25)
	//	{
	//		DC_motor.set(17.33);
	//	}

	if (MOTOR_DATA.MOTOR_DRIVE_FORWARD)							// Receive speed as 17.16 , 17.26, OR 17.33.
	{
		 dc_motor_drive (forward_drive, MOTOR_DATA.MOTOR_DRIVE_SPEED);
	}
	else if (MOTOR_DATA.MOTOR_DRIVE_REVERSE)					// Receive speed as 10.
	{
		 dc_motor_drive (reverse_drive, MOTOR_DATA.MOTOR_DRIVE_SPEED);
	}
	else if (MOTOR_DATA.MOTOR_DRIVE_NEUTRAL)					// Receive speed as 15.
	{
		 LE.set(4, dc_motor_drive (neutral_drive, MOTOR_DATA.MOTOR_DRIVE_SPEED));
	}
	else														// Receive speed as 15.
	{
		 LE.set(4, dc_motor_drive (neutral_drive, MOTOR_DATA.MOTOR_DRIVE_SPEED));
	}


	/*
	 ** Servo Motor code - Get motor_steer_angle & angle (MOTOR_STEER_ANGLE) from Master module.
	 **	Return value of servo_motor_steer_angle is boolean -> Can be used for light indicators.
	 */

	if (MOTOR_DATA.MOTOR_STEER_STRAIGHT)					// Get angle = 15.4
	{
        servo_motor_steer_angle (straight_dir, MOTOR_DATA.MOTOR_STEER_ANGLE);
        //servo_motor.set(15.4);
	}
	else if (MOTOR_DATA.MOTOR_STEER_FULL_LEFT)				// Get angle = 10.1
	{
		LE.set(1, servo_motor_steer_angle (full_left_dir, MOTOR_DATA.MOTOR_STEER_ANGLE));
	}
	else if (MOTOR_DATA.MOTOR_STEER_FULL_RIGHT)				// Get angle = 19.8
	{
        LE.set(4, servo_motor_steer_angle (full_right_dir, MOTOR_DATA.MOTOR_STEER_ANGLE));
	}

	else if (MOTOR_DATA.MOTOR_STEER_SLIGHT_LEFT)			// Get angle = 17.5
	{
		LE.set(1, servo_motor_steer_angle (slt_left_dir, MOTOR_DATA.MOTOR_STEER_ANGLE));
	}
	else if (MOTOR_DATA.MOTOR_STEER_SLIGHT_RIGHT)			// Get angle = 13.3
	{
        LE.set(4, servo_motor_steer_angle (slt_right_dir, MOTOR_DATA.MOTOR_STEER_ANGLE));
	}


}

// 1Khz (1ms) is only run if Periodic Dispatcher was configured to run it at main():
// scheduler_add_task(new periodicSchedulerTask(run_1Khz = true));
void period_1000Hz(uint32_t count)
{
    //LE.on(4);
}

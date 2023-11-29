package frc.robot.subsystems;

import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.helpers.CCSparkMax;
import frc.maps.RobotMap;

public class Intake extends SubsystemBase {
    private CCSparkMax intake1 = new CCSparkMax("Intake One", "in1", RobotMap.INTAKE_1, MotorType.kBrushless, IdleMode.kBrake, RobotMap.INTAKE_1_REVERSED);
    private CCSparkMax intake2 = new CCSparkMax("Intake Two", "in2", RobotMap.INTAKE_2, MotorType.kBrushless, IdleMode.kBrake, RobotMap.INTAKE_2_REVERSED);
    MotorControllerGroup intake = new MotorControllerGroup(intake1, intake2);

    public void spin(double speed) {
        intake.set(speed);
    }
}

package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import org.littletonrobotics.junction.Logger;

import com.kauailabs.navx.frc.AHRS;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.commands.FollowPathWithEvents;
import com.pathplanner.lib.commands.PPSwerveControllerCommand;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.helpers.CCSparkMax;
import frc.maps.RobotMap;
import frc.robot.RobotContainer;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;

/**
 * Class for controlling a swerve drive chassis. Consists of 4 SwerveModules and
 * a gyro.
 */
public class SwerveDrive extends SubsystemBase {

        private boolean test = false;

        // Initializing swerve modules. Must include full CCSparkMax object
        // declarations.
        private final Field2d m_field = new Field2d();
        private final SwerveModule frontRight = new SwerveModule(
                        new CCSparkMax(
                                        "Front Right Drive",
                                        "frd",
                                        RobotMap.FRONT_RIGHT_DRIVE,
                                        MotorType.kBrushless,
                                        IdleMode.kBrake,
                                        RobotMap.FRONT_RIGHT_DRIVE_REVERSE,
                                        RobotMap.HORIZONTAL_DISTANCE_TRAVELLED_PER_MOTOR_REVOLUTION,
                                        RobotMap.DRIVE_MOTOR_METERS_PER_SECOND),
                        new CCSparkMax(
                                        "Front Right Turn",
                                        "frt",
                                        RobotMap.FRONT_RIGHT_TURN,
                                        MotorType.kBrushless,
                                        IdleMode.kBrake,
                                        RobotMap.FRONT_RIGHT_TURN_REVERSE,
                                        RobotMap.TURN_MOTOR_ROTATIONS_TO_WHEEL_ROTATIONS_RADIANS,
                                        RobotMap.TURN_MOTOR_RADIANS_PER_SECOND),
                        RobotMap.FRONT_RIGHT_ABSOLUTE_ENCODER,
                        RobotMap.FRONT_RIGHT_ABSOLUTE_ENCODER_OFFSET,
                        "Front Right");

        private final SwerveModule frontLeft = new SwerveModule(
                        new CCSparkMax(
                                        "Front Left Drive",
                                        "fld",
                                        RobotMap.FRONT_LEFT_DRIVE,
                                        MotorType.kBrushless,
                                        IdleMode.kBrake,
                                        RobotMap.FRONT_LEFT_DRIVE_REVERSE,
                                        RobotMap.HORIZONTAL_DISTANCE_TRAVELLED_PER_MOTOR_REVOLUTION,
                                        RobotMap.DRIVE_MOTOR_METERS_PER_SECOND),
                        new CCSparkMax(
                                        "Front Left Turn",
                                        "flt",
                                        RobotMap.FRONT_LEFT_TURN,
                                        MotorType.kBrushless,
                                        IdleMode.kBrake,
                                        RobotMap.FRONT_LEFT_TURN_REVERSE,
                                        RobotMap.TURN_MOTOR_ROTATIONS_TO_WHEEL_ROTATIONS_RADIANS,
                                        RobotMap.TURN_MOTOR_RADIANS_PER_SECOND),
                        RobotMap.FRONT_LEFT_ABSOLUTE_ENCODER,
                        RobotMap.FRONT_LEFT_ABSOLUTE_ENCODER_OFFSET,
                        "Front Left");

        private final SwerveModule backRight = new SwerveModule(
                        new CCSparkMax(
                                        "Back Right Drive",
                                        "brd",
                                        RobotMap.BACK_RIGHT_DRIVE,
                                        MotorType.kBrushless,
                                        IdleMode.kBrake,
                                        RobotMap.BACK_RIGHT_DRIVE_REVERSE,
                                        RobotMap.HORIZONTAL_DISTANCE_TRAVELLED_PER_MOTOR_REVOLUTION,
                                        RobotMap.DRIVE_MOTOR_METERS_PER_SECOND),
                        new CCSparkMax(
                                        "Back Right Turn",
                                        "brt",
                                        RobotMap.BACK_RIGHT_TURN,
                                        MotorType.kBrushless,
                                        IdleMode.kBrake,
                                        RobotMap.BACK_RIGHT_TURN_REVERSE,
                                        RobotMap.TURN_MOTOR_ROTATIONS_TO_WHEEL_ROTATIONS_RADIANS,
                                        RobotMap.TURN_MOTOR_RADIANS_PER_SECOND),
                        RobotMap.BACK_RIGHT_ABSOLUTE_ENCODER,
                        RobotMap.BACK_RIGHT_ABSOLUTE_ENCODER_OFFSET,
                        "Back Right");

        private final SwerveModule backLeft = new SwerveModule(
                        new CCSparkMax(
                                        "Back Left Drive",
                                        "bld",
                                        RobotMap.BACK_LEFT_DRIVE,
                                        MotorType.kBrushless,
                                        IdleMode.kBrake,
                                        RobotMap.BACK_LEFT_DRIVE_REVERSE,
                                        RobotMap.HORIZONTAL_DISTANCE_TRAVELLED_PER_MOTOR_REVOLUTION,
                                        RobotMap.DRIVE_MOTOR_METERS_PER_SECOND),
                        new CCSparkMax(
                                        "Back Left Turn",
                                        "blt",
                                        RobotMap.BACK_LEFT_TURN,
                                        MotorType.kBrushless,
                                        IdleMode.kBrake,
                                        RobotMap.BACK_LEFT_TURN_REVERSE,
                                        RobotMap.TURN_MOTOR_ROTATIONS_TO_WHEEL_ROTATIONS_RADIANS,
                                        RobotMap.TURN_MOTOR_RADIANS_PER_SECOND),
                        RobotMap.BACK_LEFT_ABSOLUTE_ENCODER,
                        RobotMap.BACK_LEFT_ABSOLUTE_ENCODER_OFFSET,
                        "Back Left");
        // Initialize gyro
        private AHRS gyro = new AHRS(SPI.Port.kMXP);

        SwerveDriveOdometry odometer;
        PIDController xPID, yPID;
        PIDController turnPID;
        ProfiledPIDController turnPIDProfiled;
        // ProfiledPIDController turnPID;

        /** Module positions used for odometry */
        SwerveModulePosition[] swerveModulePositions = new SwerveModulePosition[4];

        // ** NetworkTableEntry for the encoders of the turn motors */
        private GenericEntry abs_Enc_FR_Offset_Entry, abs_Enc_FL_Offset_Entry, abs_Enc_BR_Offset_Entry,
                        abs_Enc_BL_Offset_Entry;
        private GenericEntry abs_Enc_FR_Entry, abs_Enc_FL_Entry, abs_Enc_BR_Entry, abs_Enc_BL_Entry;
        
        private GenericEntry enc_FR_pos_Entry, enc_FL_pos_Entry, enc_BR_pos_Entry, enc_BL_pos_Entry;
        private GenericEntry enc_FR_vel_Entry, enc_FL_vel_Entry, enc_BR_vel_Entry, enc_BL_vel_Entry;
       
        // ShuffleBoardLayouts for putting encoders onto the board
        private ShuffleboardLayout absolute_encoders_offset_list = Shuffleboard.getTab("Encoders")
                        .getLayout("Absolute Encoders Offset", BuiltInLayouts.kGrid).withSize(2, 2);

        private ShuffleboardLayout absolute_encoders_no_offset_list = Shuffleboard.getTab("Encoders")
                        .getLayout("Absolute Encoders No Offset", BuiltInLayouts.kGrid).withSize(2, 2);
        private ShuffleboardLayout turn_encoders_positions = Shuffleboard.getTab("Encoders")
                        .getLayout("Turn Encoders Position(Rad)", BuiltInLayouts.kGrid)
                        .withSize(2, 2);

        private ShuffleboardLayout turn_encoder_velocities = Shuffleboard.getTab("Encoders")
                        .getLayout("Turn Encoders Velocity (Rad / Sec)", BuiltInLayouts.kList)
                        .withSize(2, 2);

        /**
         * Creates a new SwerveDrive object. Delays 1 second before setting gyro to 0 to
         * account for gyro calibration time.
         */
        public SwerveDrive() {
                swerveModulePositions[0] = new SwerveModulePosition(0,
                                new Rotation2d(frontRight.getAbsoluteEncoderRadiansOffset()));
                swerveModulePositions[1] = new SwerveModulePosition(0,
                                new Rotation2d(frontLeft.getAbsoluteEncoderRadiansOffset()));
                swerveModulePositions[2] = new SwerveModulePosition(0,
                                new Rotation2d(backRight.getAbsoluteEncoderRadiansOffset()));
                swerveModulePositions[3] = new SwerveModulePosition(0,
                                new Rotation2d(backLeft.getAbsoluteEncoderRadiansOffset()));

                odometer = new SwerveDriveOdometry(RobotMap.DRIVE_KINEMATICS, new Rotation2d(0), swerveModulePositions);

                xPID = new PIDController(.7, .1, 0);
                yPID = new PIDController(.7, .1, 0);
                // xPID = new PIDController(1, 0, 0);
                // yPID = new PIDController(1, 0, 0);

                // *TODO: Possibly research profiled PID
                // turnPID = new ProfiledPIDController(0.5, 0, 0,
                // RobotMap.thetaControllConstraints);
                turnPID = new PIDController(.7, 0, 0);
                turnPIDProfiled = new ProfiledPIDController(.7, 0, 0, new Constraints(
                                RobotMap.MAX_ANGULAR_SPEED_RADIANS_PER_SECOND, RobotMap.TURN_RATE_LIMIT));
                turnPID.enableContinuousInput(-Math.PI, Math.PI);

                initShuffleBoardEncoders();

                SmartDashboard.putData("Field", m_field);
                new Thread(() -> {
                        try {
                                Thread.sleep(1000);
                                zeroHeading();
                        } catch (Exception e) {
                        }
                }).start();
        }

        /**
         * Resets chassis gyro to 0. For all gyro purposes, "heading" refers to the
         * facing direction of the gyro.
         */
        public void zeroHeading() {
                gyro.reset();
        }

        public double getRoll() {
                return gyro.getRoll();
        }

        /**
         * Method to get the facing direction of the gyro.
         * 
         * @return The facing direction of the gyro, between -360 and 360 degrees.
         */
        public double getHeading() {
                return Math.IEEEremainder(gyro.getYaw(), 360);
        }

        /**
         * Gets the Rotation2d value of the facing direction of the robot.
         * 
         * @return The facing direction of the robot in Rotation2d format.
         */
        public Rotation2d getRotation2d() {
                return gyro.getRotation2d();
                // return Rotation2d.fromDegrees(getHeading());
        }

        /**
         * Debug function.
         */
        @Override
        public void periodic() {
                SmartDashboard.putNumber("Robot Heading", getRotation2d().getRadians());
                SmartDashboard.putNumber("Wheel Velocity", frontRight.getDriveVelocity());
                SmartDashboard.putNumber("X", odometer.getPoseMeters().getX());
                SmartDashboard.putNumber("Y", odometer.getPoseMeters().getY());
                SmartDashboard.putNumber("Pitch", this.getPitch());
                SmartDashboard.putNumber("Roll", gyro.getRoll());
                SmartDashboard.putNumber("Yaw", gyro.getYaw());

                SmartDashboard.putNumber("FR", frontRight.getDriveVelocity());
                SmartDashboard.putNumber("FL", frontLeft.getDriveVelocity());
                SmartDashboard.putNumber("BR", backRight.getDriveVelocity());
                SmartDashboard.putNumber("BL", backLeft.getDriveVelocity());

                SmartDashboard.putBoolean("Event Test", test);

                m_field.setRobotPose(odometer.getPoseMeters());
                updateShuffleBoardEncoders();

                updateOdometer();
        }

        public void toggleEvent() {
                test = !test;
        }

        /**
         * Sets all 4 modules' drive and turn speeds to 0.
         */
        public void stopModules() {
                SwerveModuleState[] states = new SwerveModuleState[] {
                                new SwerveModuleState(0, new Rotation2d(frontRight.getAbsoluteEncoderRadiansOffset())),
                                new SwerveModuleState(0, new Rotation2d(frontLeft.getAbsoluteEncoderRadiansOffset())),
                                new SwerveModuleState(0, new Rotation2d(backRight.getAbsoluteEncoderRadiansOffset())),
                                new SwerveModuleState(0, new Rotation2d(backLeft.getAbsoluteEncoderRadiansOffset()))
                };
                setModuleStates(states);
        }

        /**
         * Sets all 4 modules' drive and turn speeds with the SwerveModuleState format.
         * 
         * @param desiredStates The array of the states that each module will be set to
         *                      in the SwerveModuleState format.
         */
        public void setModuleStates(SwerveModuleState[] desiredStates) {
                boolean openLoop = true;
                SwerveDriveKinematics.desaturateWheelSpeeds(desiredStates, RobotMap.MAX_DRIVE_SPEED_METERS_PER_SECOND);
                Logger.getInstance().recordOutput("SwerveModuleStates/SetpointsOptimized", desiredStates);
                frontRight.setDesiredState(desiredStates[0], openLoop);
                frontLeft.setDesiredState(desiredStates[1], openLoop);
                backRight.setDesiredState(desiredStates[2], openLoop);
                backLeft.setDesiredState(desiredStates[3], openLoop);
        }

        /**
         * Gets the position of the robot in Pose2d format. Uses odometer reading.
         * Includes the x, y, and theta values of the robot.
         * 
         * @return The Pose2d of the robot.
         */
        public Pose2d getPose() {
                return odometer.getPoseMeters();
        }

        /**
         * Resets the odometer readings using the gyro, SwerveModulePositions (defined
         * in constructor), and Pose2d.
         */
        public void resetOdometry() {
                odometer.resetPosition(getRotation2d(), swerveModulePositions, getPose());
        }

        public void setOdometry(Pose2d pos) {
                odometer.resetPosition(getRotation2d(), swerveModulePositions, pos);
        }

        public void updateModulePositions() {
                swerveModulePositions[0] = new SwerveModulePosition(frontRight.getDrivePosition(),
                                new Rotation2d(frontRight.getTurnPosition()));
                swerveModulePositions[1] = new SwerveModulePosition(frontLeft.getDrivePosition(),
                                new Rotation2d(frontLeft.getTurnPosition()));
                swerveModulePositions[2] = new SwerveModulePosition(backRight.getDrivePosition(),
                                new Rotation2d(backRight.getTurnPosition()));
                swerveModulePositions[3] = new SwerveModulePosition(backLeft.getDrivePosition(),
                                new Rotation2d(backLeft.getTurnPosition()));
        }

        public void updateOdometer() {
                updateModulePositions();
                Logger.getInstance().recordOutput("Odometry/Pose2D", odometer.getPoseMeters());
                odometer.update(getRotation2d(), swerveModulePositions);
        }

        // Autonomous
        // Odometer used to get Pose2d of the robot.

        // xPID and yPID should have the same values.

        // PPHolonomicDriveController holonomicDriveController = new
        // PPHolonomicDriveController(xPID, yPID, turnPID);

        /**
         * Follows a PathPlanner path. Referenced in autonomous classes.
         * 
         * @param traj      The PathPlannerTrajectory to be followed.
         * @param firstPath Whether or not this is the first path being followed in
         *                  auto. If so, resets the gyro before starting.
         * @return A command that follows a path.
         */

        // docs:
        // https://github.com/mjansen4857/pathplanner/wiki/PathPlannerLib:-Java-Usage
        // public Command followPath(PathPlannerTrajectory traj) {
        // return new SequentialCommandGroup(
        // new InstantCommand(() -> {
        // setOdometry(traj.getInitialHolonomicPose());
        // }),
        // new ParallelCommandGroup(
        // new FollowPathWithEvents(
        // // Path following command
        // new PPSwerveControllerCommand(traj, this::getPose,
        // RobotMap.DRIVE_KINEMATICS, xPID,
        // yPID, turnPID, this::setModuleStates,
        // false, this),
        // )
        // // new RunCommand(() -> System.out.println(getPose()))
        // ));
        // }

        // * I copied this one from documentation */
        public Command followTrajectoryCommand(PathPlannerTrajectory traj, boolean isFirstPath) {
                return new SequentialCommandGroup(
                                new InstantCommand(() -> {
                                        // Reset odometry for the first path you run during auto
                                        if (isFirstPath) {
                                                this.setOdometry(traj.getInitialHolonomicPose());
                                        }
                                }),
                                new WaitCommand(1),
                                new FollowPathWithEvents(new PPSwerveControllerCommand(
                                                traj,
                                                this::getPose, // Pose supplier
                                                RobotMap.DRIVE_KINEMATICS, // SwerveDriveKinematics
                                                xPID, // X controller. Tune these values for your robot. Leaving them 0
                                                      // will only use feedforwards.
                                                yPID, // Y controller (usually the same values as X controller)
                                                turnPID, // Rotation controller. Tune these values for your robot.
                                                         // Leaving them 0 will only use feedforwards.
                                                this::setModuleStates, // Module states consumer
                                                true, // Should the path be automatically mirrored depending on alliance
                                                      // color. Optional, defaults to true
                                                this // Requires this drive subsystem
                                ), traj.getMarkers(),
                                                RobotContainer.eventMap));
        }

        public Command moveCommand() {
                List<Translation2d> midpts = new ArrayList<Translation2d>();
                midpts.add(new Translation2d(1, 0));
                TrajectoryConfig trajectoryConfig = new TrajectoryConfig(RobotMap.MAX_DRIVE_SPEED_METERS_PER_SECOND, 1)
                                .setKinematics(RobotMap.DRIVE_KINEMATICS);
                Trajectory trajectory = TrajectoryGenerator.generateTrajectory(new Pose2d(0, 0, new Rotation2d(0)),
                                midpts, new Pose2d(2.5, 0, new Rotation2d(0)), trajectoryConfig);
                return new SwerveControllerCommand(trajectory, this::getPose, RobotMap.DRIVE_KINEMATICS, xPID, yPID,
                                turnPIDProfiled, this::setModuleStates, this);
        }
        // public Command moveTo(double position) {
        // return new RunCommand(() -> {
        // double speed = yPID.calculate(frontRight.getDrivePosition(), position) *
        // RobotMap.MAX_DRIVE_SPEED_METERS_PER_SECOND;
        // SwerveModuleState[] states = {new SwerveModuleState(speed, new
        // Rotation2d(frontRight.getTurnPosition())), new SwerveModuleState(speed, new
        // Rotation2d(frontLeft.getTurnPosition())), new SwerveModuleState(speed, new
        // Rotation2d(backRight.getTurnPosition())), new SwerveModuleState(speed, new
        // Rotation2d(backLeft.getTurnPosition()))};
        // setModuleStates(states);
        // }, this) {
        // @Override
        // public boolean isFinished() {
        // return Math.abs(frontRight.getDrivePosition() - position) < 0.2;
        // }
        // };
        // }

        public void test(double driveSpeed, double turnSpeed) {
                backRight.driveAndTurn(driveSpeed, turnSpeed);
                backRight.printEncoders();
        }

        public void resetAbsoluteEncoders() {
                frontRight.resetAbsoluteEncoder();
                frontLeft.resetAbsoluteEncoder();
                backRight.resetAbsoluteEncoder();
                backLeft.resetAbsoluteEncoder();
        }

        public void printAbsoluteEncoders() {
                frontRight.printAbsoluteEncoder();
                frontLeft.printAbsoluteEncoder();
                backRight.printAbsoluteEncoder();
                backLeft.printAbsoluteEncoder();
        }

        public void resetEncoders() {
                frontRight.resetEncoders();
                frontLeft.resetEncoders();
                backRight.resetEncoders();
                backLeft.resetEncoders();
        }

        public void printFrontRight() {
                frontRight.printEncoders();
                frontRight.printAbsoluteEncoder();
        }

        public void printFrontLeft() {
                frontLeft.printEncoders();
                frontLeft.printAbsoluteEncoder();
        }

        public void printBackRight() {
                backRight.printEncoders();
                backRight.printAbsoluteEncoder();
        }

        public void printBackLeft() {
                backLeft.printEncoders();
                backLeft.printAbsoluteEncoder();
        }

        public void printPos2d() {
                System.out.println(odometer.getPoseMeters());
        }

        public void initShuffleBoardEncoders() {
                abs_Enc_FR_Offset_Entry = Shuffleboard.getTab("Encoders").getLayout(absolute_encoders_offset_list.getTitle())
                                .add(frontRight.getName(), frontRight.getAbsoluteEncoderRadiansOffset()).getEntry();
                abs_Enc_FL_Offset_Entry = Shuffleboard.getTab("Encoders").getLayout(absolute_encoders_offset_list.getTitle())
                                .add(frontLeft.getName(), frontLeft.getAbsoluteEncoderRadiansOffset()).getEntry();
                abs_Enc_BR_Offset_Entry = Shuffleboard.getTab("Encoders").getLayout(absolute_encoders_offset_list.getTitle())
                                .add(backRight.getName(), backRight.getAbsoluteEncoderRadiansOffset()).getEntry();
                abs_Enc_BL_Offset_Entry = Shuffleboard.getTab("Encoders").getLayout(absolute_encoders_offset_list.getTitle())
                                .add(backLeft.getName(), backLeft.getAbsoluteEncoderRadiansOffset()).getEntry();

                enc_FR_pos_Entry = Shuffleboard.getTab("Encoders").getLayout(turn_encoders_positions.getTitle())
                                .add(frontRight.getName(), frontRight.getTurnPosition()).getEntry();
                enc_FL_pos_Entry = Shuffleboard.getTab("Encoders").getLayout(turn_encoders_positions.getTitle())
                                .add(frontLeft.getName(), frontLeft.getTurnPosition()).getEntry();
                enc_BR_pos_Entry = Shuffleboard.getTab("Encoders").getLayout(turn_encoders_positions.getTitle())
                                .add(backRight.getName(), backRight.getTurnPosition()).getEntry();
                enc_BL_pos_Entry = Shuffleboard.getTab("Encoders").getLayout(turn_encoders_positions.getTitle())
                                .add(backLeft.getName(), backLeft.getTurnPosition()).getEntry();

                abs_Enc_FR_Entry = Shuffleboard.getTab("Encoders").getLayout(absolute_encoders_no_offset_list.getTitle())
                                .add(frontRight.getName(), frontRight.getAbsoluteEncoderRadiansNoOffset()).getEntry();
                abs_Enc_FL_Entry = Shuffleboard.getTab("Encoders").getLayout(absolute_encoders_no_offset_list.getTitle())
                                .add(frontLeft.getName(), frontLeft.getAbsoluteEncoderRadiansNoOffset()).getEntry();
                abs_Enc_BR_Entry = Shuffleboard.getTab("Encoders").getLayout(absolute_encoders_no_offset_list.getTitle())
                                .add(backRight.getName(), backRight.getAbsoluteEncoderRadiansNoOffset()).getEntry();
                abs_Enc_BL_Entry = Shuffleboard.getTab("Encoders").getLayout(absolute_encoders_no_offset_list.getTitle())
                                .add(backLeft.getName(), backLeft.getAbsoluteEncoderRadiansNoOffset()).getEntry();

        }

        public void updateShuffleBoardEncoders() {
                abs_Enc_FR_Offset_Entry.setDouble(frontRight.getAbsoluteEncoderRadiansOffset());
                abs_Enc_FL_Offset_Entry.setDouble(frontLeft.getAbsoluteEncoderRadiansOffset());
                abs_Enc_BR_Offset_Entry.setDouble(backRight.getAbsoluteEncoderRadiansOffset());
                abs_Enc_BL_Offset_Entry.setDouble(backLeft.getAbsoluteEncoderRadiansOffset());

                abs_Enc_FR_Entry.setDouble(frontRight.getAbsoluteEncoderRadiansOffset());
                abs_Enc_FL_Entry.setDouble(frontLeft.getAbsoluteEncoderRadiansOffset());
                abs_Enc_BR_Entry.setDouble(backRight.getAbsoluteEncoderRadiansOffset());
                abs_Enc_BL_Entry.setDouble(backLeft.getAbsoluteEncoderRadiansOffset());


                enc_FR_pos_Entry.setDouble(frontRight.getTurnPosition());
                enc_FL_pos_Entry.setDouble(frontLeft.getTurnPosition());
                enc_BR_pos_Entry.setDouble(backRight.getTurnPosition());
                enc_BL_pos_Entry.setDouble(backLeft.getTurnPosition());

                // enc_FR_vel_Entry.setDouble(frontRight.getTurnVelocity());
                // enc_FL_vel_Entry.setDouble(frontLeft.getTurnVelocity());
                // enc_BR_vel_Entry.setDouble(backRight.getTurnVelocity());
                // enc_BL_vel_Entry.setDouble(backLeft.getTurnVelocity());
        }

        public void printWorld() {
                System.out.println("Hello World!");
        }

        public double getPitch() {
                return gyro.getPitch() - 1.14;
        }

}

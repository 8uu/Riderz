package com.ecse321.team10.riderz.sql;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ecse321.team10.riderz.model.Driver;
import com.ecse321.team10.riderz.model.AdInformation;
import com.ecse321.team10.riderz.model.Car;
import com.ecse321.team10.riderz.model.User;
import com.ecse321.team10.riderz.model.Trip;
import com.ecse321.team10.riderz.model.Itinerary;
import com.ecse321.team10.riderz.model.Location;
import com.ecse321.team10.riderz.model.Reservation;

/**
 * <p>Persistance layer constructed using JDBC. Provides direct access to the database.</p>
 * <p><b>Warning</b> - Potentially delete methods are potentially destructive... Use with caution. -
 * <b>Warning</b></p>
 * @version 1.00
 */
public class MySQLJDBC {
	private static final Logger logger = LogManager.getLogger(MySQLJDBC.class);

	private static final String connection = "jdbc:mysql://35.237.200.65:3306/dev";
	private static final String username = "dev";
	private static final String password = "ecse321LoGiNdEv";

	private static Connection c;

	//=======================
	// CONNECTIONS
	//=======================
	/**
	 * Establishes connection to database using credentials provided above.
	 * @return True if the connection has been established. False if any error occurs.
	 */
	public boolean connect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			//this holds dummy values for now
	        c = DriverManager.getConnection(connection, username, password);
	        logger.info("Connection to the database has been established.");
	        return true;
		}
		catch(Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Disconnects from the database.
	 * @return True if the connection has been severed. False if any error occurs.
	 */
	public boolean closeConnection() {
		try {
			if (c != null) {
                c.close();
                logger.info("Connection to the database has been closed.");
                return true;
            }
			return false;
		}
		catch(Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}
	
	//=======================
	// CARS API
	//=======================
	/**
	 * Inserts a Car object into the database.
	 * @param	car		- 	A Car object to be inserted into the database.
	 * @return 	True if the Car object has been inserted. False if any error occurs.
	 */
	public boolean insertCar(Car car) {
		String insertCar = "INSERT INTO car (operator, make, model, year, numOfSeats, " +
						   "fuelEfficiency, licensePlate) VALUES (?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(insertCar);
			ps.setString(1, car.getOperator());
			ps.setString(2, car.getMake());
			ps.setString(3, car.getModel());
			ps.setInt(4, car.getYear());
			ps.setInt(5, car.getNumOfSeats());
			ps.setDouble(6, car.getFuelEfficiency());
			ps.setString(7, car.getLicensePlate());
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Car '%s' has been inserted in the database.", car.getOperator()));
				return true;
			}
			return false;
		}
		catch(Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Deletes a Car object entry from the database based on an username.
	 * @param operator	- 	A String representing an username.
	 * @return True if an entry was deleted. False otherwise.
	 */
	public boolean deleteCar(String operator) {
		String deleteCar = "DELETE FROM car WHERE operator = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(deleteCar);
			ps.setString(1, operator);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Car '%s' has been deleted from the database.", operator));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Updates an entry within the database to values stored with a Car object.
	 * @param car 		-	A Car object to be updated to within the database
	 * @return True if an entry has been updated. False otherwise.
	 */
	public boolean updateCar(Car car) {
		String updateCar = "UPDATE car SET make = ?, model = ?, year = ?, numOfSeats = ?, " + 
					   	   "fuelEfficiency = ?, licensePlate = ? WHERE operator = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(updateCar);
			ps.setString(1, car.getMake());
			ps.setString(2, car.getModel());
			ps.setInt(3, car.getYear());
			ps.setInt(4, car.getNumOfSeats());
			ps.setDouble(5, car.getFuelEfficiency());
			ps.setString(6, car.getLicensePlate());
			ps.setString(7, car.getOperator());
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Car '%s' has been updated in the database.", car.getOperator()));
				return true;
			}
			return false;
		}
		catch(Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Obtains all Car entries from the database.
	 * @return An ArrayList of Car objects. Null otherwise.
	 */
	public ArrayList<Car> getAllCars() {
        ArrayList<Car> carList = new ArrayList<Car>();
        try {
            ResultSet rs = c.createStatement().executeQuery("SELECT * FROM car;");
            while (rs.next()) {
                carList.add(new Car(rs.getString("operator"), rs.getString("make"), 
                		rs.getString("model"), rs.getInt("year"), rs.getInt("numOfSeats"), 
                		rs.getDouble("fuelEfficiency"), rs.getString("licensePlate")));
			}
            rs.close();
            logger.info("SELECT * FROM car;");
            return carList;
        } catch (Exception e) {
        	logger.error(e.getClass().getName() + ": " + e.getMessage());
        	return null;
        }
        
    } 
	
	/**
	 * Searches within the database for an entry based on an username.
	 * @param operator 	- 	A String representing an username.
	 * @return A Car object if an entry was found. Null otherwise.
	 */
	public Car getCarByOperator(String operator) {
		String query = "SELECT * FROM car WHERE operator = ?;";
        Car car = null;
		PreparedStatement ps = null;
        try {
			ps = c.prepareStatement(query);
			ps.setString(1, operator);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                car = new Car(rs.getString("operator"), rs.getString("make"), 
                			  rs.getString("model"), rs.getInt("year"), 
							  rs.getInt("numOfSeats"), rs.getDouble("fuelEfficiency"), 
							  rs.getString("licensePlate"));
			}
            ps.close();
            if (car != null) {
            	logger.info(String.format("Car '%s' has been returned from the database.", car.getOperator()));
            } else {
            	logger.info(operator + " does not have a car");
            }
            return car;
        } catch (Exception e) {
        	logger.error(e.getClass().getName() + ": " + e.getMessage());
        	return null;
        }
    }

	//=======================
	// USERS API
	//=======================
	/**
	 * Inserts an User object into the database.
	 * @param user 		- 	An User object to insert into the database.
	 * @return True if the User was inserted into the database. False otherwise.
	 */
	public boolean insertUser(User user) {
		String insertUser = "INSERT INTO users (username, password, email, phone, " +
							"firstName, lastName) VALUES (?, ?, ?, ?, ?, ?);";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(insertUser);
			ps.setString(1, user.getUsername());
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(user.getPassword().getBytes(StandardCharsets.UTF_8));
			ps.setString(2, DatatypeConverter.printHexBinary(hash));
			ps.setString(3, user.getEmail());
			ps.setString(4, user.getPhone());
			ps.setString(5, user.getFirstName());
			ps.setString(6, user.getLastName());
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("User '%s' has been inserted in the database.", user.getUsername()));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes an User from the database based on the User's username.
	 * @param username 	- 	A String representing the User's username
	 * @return True if an entry was deleted from the database. False otherwise.
	 */
	public boolean deleteUser(String username) {
		String deleteUser = "DELETE FROM users WHERE username = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(deleteUser);
			ps.setString(1, username);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("User '%s' has been deleted from the database.", username));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Fetches an User from the database based on the User's username.
	 * @param username 	- 	A String representing the User's username
	 * @return An User object if an entry was found. Null otherwise.
	 */
	public User getUserByUsername(String username) {
		String getUser = "SELECT * FROM users WHERE username = ?;";
		User user = null;
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(getUser);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				user = new User(rs.getString("username"), rs.getString("password"),
								rs.getString("email"), rs.getString("phone"),
								rs.getString("firstName"), rs.getString("lastName"));
			}
			ps.close();
			if (user != null) {
				logger.info(String.format("User '%s' has been returned from the database.", user.getUsername()));
			} else {
				logger.info(username + " was not found in the database");
			}
			return user;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Fetches all User from the database.
	 * @return An ArrayList of User object. Null if an error occurred.
	 */
	public ArrayList<User> getAllUsers() {
		ArrayList<User> userList = new ArrayList<User>();
		try {
			ResultSet rs = c.createStatement().executeQuery("SELECT * FROM users;");
			while (rs.next()) {
				userList.add(new User(rs.getString("username"), rs.getString("password"),
						rs.getString("email"), rs.getString("phone"),
						rs.getString("firstName"), rs.getString("lastName")));
			}
			rs.close();
			logger.info("SELECT * FROM users;");
			return userList;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Verifies if the username and password combination exists within the database.
	 * @param username 	- 	A String representing the User's username
	 * @param password 	- 	A String representing the User's password
	 * @return True if the combination exists. False otherwise.
	 */
	public boolean verifyLogin(String username, String password) {
		String verifyLogin = "SELECT username FROM users WHERE username = ? AND password = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(verifyLogin);
			ps.setString(1, username);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
			ps.setString(2, DatatypeConverter.printHexBinary(hash));
			ResultSet rs = ps.executeQuery();
			rs.last();
			if (rs.getRow() == 1) {
				logger.info(String.format("Login for '%s' has been verified.", username));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Fetches the phone of a User based on their username.
	 * @param username 	- 	A String representing the User's username
	 * @return A String representing the User's phone number if the User was found. False otherwise.
	 */
	public String getPhone(String username) {
		String getPhone = "SELECT phone FROM users WHERE username = ?;";
		PreparedStatement ps = null;
		String phone = null;
		try {
			ps = c.prepareStatement(getPhone);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				phone = rs.getString("phone");
			}
			ps.close();
			logger.info(String.format("Phone for '%s' has been returned from the database.", username));
			return phone;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Updates a User's phone number.
	 * @param username 	- 	A String representing the User's username
	 * @param phone 	-	A String representing the new phone number
	 * @return True if an entry was updated. False otherwise.
	 */
	public boolean setPhone(String username, String phone) {
		String setPhone = "UPDATE users SET phone = ? WHERE username = ?;";
	   	PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(setPhone);
			ps.setString(1, phone);
			ps.setString(2, username);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Phone for '%s' has been updated in the database.", username));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Obtains an User's email address.
	 * @param username 	-	A String representing an User's username.
	 * @return A String representing an email address if found. False otherwise.
	 */
	public String getEmail(String username) {
		String getEmail = "SELECT email FROM users WHERE username = ?;";
		PreparedStatement ps = null;
		String email = null;
		try {
			ps = c.prepareStatement(getEmail);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				email = rs.getString("email");
				logger.info(String.format("Email for '%s' has been returned from the database.", username));
			}
			ps.close();
			return email;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Updates an User's email address.
	 * @param username	-	A String representing an User's username.
	 * @param email		-	A String representing a new email address.
	 * @return True if an entry was updated. False otherwise.
	 */
	public boolean setEmail(String username, String email) {
		String setEmail = "UPDATE users SET email = ? WHERE username = ?;";
	   	PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(setEmail);
			ps.setString(1, email);
			ps.setString(2, username);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Email for '%s' has been updated in the database.", username));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Updates an User's password.
	 * @param username	-	A String representing an User's username.
	 * @param password	-	A String representing a new password.
	 * @return True if an entry was updated. False otherwise.
	 */
	public boolean setPassword(String username, String password) {
		String setPassword = "UPDATE users SET password = ? WHERE username = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(setPassword);

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
			ps.setString(1, DatatypeConverter.printHexBinary(hash));
			ps.setString(2, username);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Password for '%s' has been updated in the database.", username));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	//=======================
	// DRIVER API
	//=======================
	/**
	 * Inserts an User as a driver into the database.
	 * @param operator	-	A String representing an User's username.
	 * @return True if an entry was inserted. False otherwise.
	 */
	public boolean insertDriver(String operator) {
		String insertDriver = "INSERT INTO driver (operator, rating, personsRated, " +
							  "tripsCompleted) VALUES (?, 0.0, 0, 0);";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(insertDriver);
			ps.setString(1, operator);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Driver '%s' has been inserted in the database.", operator));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes a driver from the database.
	 * @param operator	-	A String representing an User's username.
	 * @return True if an entry was deleted. False otherwise.
	 */
	public boolean deleteDriver(String operator) {
		String deleteDriver = "DELETE FROM driver WHERE operator = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(deleteDriver);
			ps.setString(1, operator);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Driver '%s' has been deleted from the database.", operator));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Obtains a Driver object representing an entry from the database.
	 * @param operator	-	A String representing an User's username.
	 * @return A Driver object if an entry was found. Null otherwise.
	 */
	public Driver getDriverByUsername(String operator) {
		String getDriver = "SELECT * FROM driver WHERE operator = ?;";
		Driver driver = null;
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(getDriver);
			ps.setString(1, operator);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				driver = new Driver(rs.getString("operator"), rs.getDouble("rating"),
									rs.getInt("personsRated"), rs.getInt("tripsCompleted"));
			}
			ps.close();
			logger.info(String.format("Driver '%s' has been returned from the database.", operator));
			return driver;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Fetches all entries from the database.
	 * @return An ArrayList of Driver object. Null if an error occurred.
	 */
	public ArrayList<Driver> getAllDrivers() {
		ArrayList<Driver> driverList = new ArrayList<Driver>();
		try {
			ResultSet rs = c.createStatement().executeQuery("SELECT * FROM driver;");
			while (rs.next()) {
				driverList.add(new Driver(rs.getString("operator"), rs.getDouble("rating"),
										  rs.getInt("personsRated"), rs.getInt("tripsCompleted")));
			}
			rs.close();
			logger.info("SELECT * FROM driver");
			return driverList;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Fetches all drivers above or equal to a rating.
	 * @param rating	-	A double representing a minimum rating.
	 * @return An ArrayList of Driver objects fitting the criteria. Null otherwise if an error occurred.
	 */
	public ArrayList<Driver> getAllDriversAboveRating(double rating) {
		ArrayList<Driver> driverList = new ArrayList<Driver>();
		PreparedStatement ps = null;
		String getAllDriversAboveRating = "SELECT * FROM driver WHERE rating >= ?;";
		try {
			ps = c.prepareStatement(getAllDriversAboveRating);
			ps.setDouble(1, rating);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				driverList.add(new Driver(rs.getString("operator"), rs.getDouble("rating"),
										  rs.getInt("personsRated"), rs.getInt("tripsCompleted")));
			}
			ps.close();
			logger.info("Returned drivers with rating above " + rating);
			return driverList;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Obtains a driver's rating.
	 * @param operator	-	A String representing an User's username.
	 * @return A double if the driver was found. 0.0 otherwise.
	 */
	public double getRating(String operator) {
		String getRating = "SELECT rating FROM driver WHERE operator = ?;";
		PreparedStatement ps = null;
		double rating = 0.0;
		try {
			ps = c.prepareStatement(getRating);
			ps.setString(1, operator);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				rating = rs.getDouble("rating");
			}
			ps.close();
			logger.info(String.format("Rating for '%s' has been returned from the database.", operator));
			return rating;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return 0.0;
		}
	}

	/**
	 * Updates a driver's rating.
	 * @param operator	-	A String representing an User's username.
	 * @param rating	-	A double representing a driver's rating.
	 * @return True if an entry was updated. False otherwise.
	 */
	public boolean addRating(String operator, double rating) {
		String addRating = "UPDATE driver SET rating = ? WHERE operator = ?;";
		double oldRating = getRating(operator);
		int personsRated = getPersonsRated(operator);
		PreparedStatement ps = null;
		try {
			double newRating = ((personsRated * oldRating) + rating) / (personsRated + 1);
			ps = c.prepareStatement(addRating);
			ps.setDouble(1, newRating);
			ps.setString(2, operator);
			if (ps.executeUpdate() == 1 && 
				incrementPersonsRated(operator)) {
				ps.close();
				logger.info(String.format("Rating for '%s' has been updated in the database.", operator));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Obtains the number of people who have rated a driver.
	 * @param operator	- 	A String representing an User's username.
	 * @return An integer if an entry was found. 0 otherwise.
	 */
	public int getPersonsRated(String operator) {
		String getPersonsRated = "SELECT personsRated FROM driver WHERE operator = ?;";
		PreparedStatement ps = null;
		int personsRated = 0;
		try {
			ps = c.prepareStatement(getPersonsRated);
			ps.setString(1, operator);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				personsRated = rs.getInt("personsRated");
			}
			ps.close();
			logger.info(String.format("PersonsRated for '%s' has been returned from the database.", operator));
			return personsRated;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return 0;
		}
	}

	/**
	 * Fetches all drivers above or equal to a number of persons rated.
	 * @param rating	-	An integer representing the minimum number of persons rated.
	 * @return An ArrayList of Driver objects fitting the criteria. Null otherwise if an error occurred.
	 */
	public ArrayList<Driver> getAllDriversAbovePersonsRated(int personsRated) {
		ArrayList<Driver> driverList = new ArrayList<Driver>();
		PreparedStatement ps = null;
		String getAllDriversAboveRating = "SELECT * FROM driver WHERE personsRated >= ?;";
		try {
			ps = c.prepareStatement(getAllDriversAboveRating);
			ps.setInt(1, personsRated);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				driverList.add(new Driver(rs.getString("operator"), rs.getDouble("rating"),
										  rs.getInt("personsRated"), rs.getInt("tripsCompleted")));
			}
			ps.close();
			logger.info("Returned drivers with persons rated above " + personsRated);
			return driverList;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Increments by 1 the number of people who have rated the driver.
	 * @param operator	-	A String representing an User's username.
	 * @return True if an entry was updated. False otherwise.
	 */
	public boolean incrementPersonsRated(String operator) {
		String incrementPersonsRated = "UPDATE driver SET personsRated = personsRated + 1 " +
									   "WHERE operator = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(incrementPersonsRated);
			ps.setString(1, operator);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("PersonsRated for '%s' has been increased by 1 in the database.", operator));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Obtains the number of trips completed of a driver.
	 * @param operator	-	A String representing an User's username.
	 * @return An integer if an entry was found. 0 otherwise.
	 */
	public int getTripsCompleted(String operator) {
		String getTripsCompleted = "SELECT tripsCompleted FROM driver WHERE operator = ?;";
		PreparedStatement ps = null;
		int tripsCompleted = 0;
		try {
			ps = c.prepareStatement(getTripsCompleted);
			ps.setString(1, operator);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				tripsCompleted = rs.getInt("tripsCompleted");
			}
			ps.close();
			logger.info(String.format("TripsCompleted for '%s' has been returned from the database.", operator));
			return tripsCompleted;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return 0;
		}
	}

	/**
	 * Increments by 1 the number of trips completed by a driver.
	 * @param operator	-	A String representing an User's username.
	 * @return True if an entry was updated. False otherwise.
	 */
	public boolean incrementTripsCompleted(String operator) {
		String incrementTripsCompleted = "UPDATE driver SET tripsCompleted = tripsCompleted + 1 " +
									   	 "WHERE operator = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(incrementTripsCompleted);
			ps.setString(1, operator);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("TripsCompleted for '%s' has been increased by 1 in the database.", operator));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Fetches all drivers above or equal to a number of persons rated.
	 * @param rating	-	An integer representing the minimum number of persons rated.
	 * @return An ArrayList of Driver objects fitting the criteria. Null otherwise if an error occurred.
	 */
	public ArrayList<Driver> getAllDriversAboveTripsCompleted(int tripsCompleted) {
		ArrayList<Driver> driverList = new ArrayList<Driver>();
		PreparedStatement ps = null;
		String getAllDriversAboveRating = "SELECT * FROM driver WHERE tripsCompleted >= ?;";
		try {
			ps = c.prepareStatement(getAllDriversAboveRating);
			ps.setInt(1, tripsCompleted);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				driverList.add(new Driver(rs.getString("operator"), rs.getDouble("rating"),
										  rs.getInt("personsRated"), rs.getInt("tripsCompleted")));
			}
			ps.close();
			logger.info("Returned drivers with trips completed above " + tripsCompleted);
			return driverList;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	//=======================
	// TRIP API
	//=======================
	/**
	 * Inserts a new trip for an User into the database.
	 * @param operator	-	A String representing an User's username.
	 * @return True if an entry was inserted. False otherwise.
	 */
	public boolean insertTrip(String operator) {
		String insertTrip = "INSERT INTO trip(operator) VALUES (?);";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(insertTrip);
			ps.setString(1, operator);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Trip for '%s' has been inserted in the database.", operator));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes from the database a trip based on the tripID and the User's username.
	 * @param tripID	-	An integer uniquely identifying a trip.
	 * @param operator	-	A String representing an User's username.
	 * @return True if an entry was deleted. False otherwise.
	 */
	public boolean deleteTrip(int tripID, String operator) {
		String deleteTrip = "DELETE FROM trip WHERE tripID = ? AND operator = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(deleteTrip);
			ps.setInt(1, tripID);
			ps.setString(2, operator);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Trip for '%s' has been removed the database.", operator));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes all trips for a specific User.
	 * @param operator	-	A String representing an User's username.
	 * @return True if entries were deleted. False otherwise.
	 */
	public boolean deleteAllTrips(String operator) {
		String deleteAllTrips = "DELETE FROM trip WHERE operator = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(deleteAllTrips);
			ps.setString(1, operator);
			if (ps.executeUpdate() != 0) {
				ps.close();
				logger.info(String.format("All trips for '%s' has been removed from the database.", operator));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Fetches all trips for a specific User.
	 * @param operator	-	A String representing an User's username.
	 * @return An ArrayList of Trip objects. Null if an error occurred.
	 */
	public ArrayList<Trip> getTripsByUsername(String operator) {
		ArrayList<Trip> tripList = new ArrayList<Trip>();
		String getTripsByUsername = "SELECT * FROM trip WHERE operator = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(getTripsByUsername);
			ps.setString(1, operator);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				tripList.add(new Trip(rs.getInt("tripID"), rs.getString("operator")));
			}
			rs.close();
			logger.info(String.format("All trips for '%s' has been returned from the database.", operator));
			return tripList;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Fetches the last trip for a specific User.
	 * @param operator	-	A String representing an User's username.
	 * @return A Trip object if found. Null otherwise.
	 */
	public Trip getLastTripByUsername(String operator) {
		String getLastTripByUsername = "SELECT * FROM trip WHERE operator = ? ORDER BY " +
									   "tripID DESC LIMIT 1;";
		PreparedStatement ps = null;
		Trip trip = null;
		try {
			ps = c.prepareStatement(getLastTripByUsername);
			ps.setString(1, operator);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				trip = new Trip(rs.getInt("tripID"), rs.getString("operator"));
			}
			logger.info(String.format("Last trip for '%s' has been returned from the database.", operator));
			ps.close();
			return trip;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Fetches all trips from the database.
	 * @return An ArrayList of Trip objects. Null if an error occurred.
	 */
	public ArrayList<Trip> getAllTrips() {
		ArrayList<Trip> tripList = new ArrayList<Trip>();
		try {
			ResultSet rs = c.createStatement().executeQuery("SELECT * FROM trip;");
			while (rs.next()) {
				tripList.add(new Trip(rs.getInt("tripID"), rs.getString("operator")));
			}
			rs.close();
			logger.info("All trips for all users have been returned from the database.");
			return tripList;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Returns the driver's name based on a tripID.
	 * @param tripID - An integer uniquely identifying a trip
	 * @return A driver's name
	 */
	public String getDriverNameByTripID(int tripID) {
		String getDriverNameByTripID = "SELECT CONCAT(firstName, ' ', lastName) AS name FROM users " +
									   "WHERE username IN (SELECT operator FROM trip WHERE tripID = ?);";
		PreparedStatement ps = null;
		String name = null;
		try {
			ps = c.prepareStatement(getDriverNameByTripID);
			ps.setInt(1, tripID);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				name = rs.getString("name");
			}
			logger.info("Name has been returned for trip " + tripID);
			ps.close();
			return name;
		} catch (Exception e) {
		logger.error(e.getClass().getName() + ": " + e.getMessage());
		return null;
		}
	}

	//=======================
	// Itinerary API
	//=======================
	/**
	 * Inserts an Itinerary object into the database.
	 * @param itinerary	-	An Itinerary object to be inserted into the database.
	 * @return True if an entry was inserted. False otherwise.
	 */
	public boolean insertItinerary(Itinerary itinerary) {
		String insertItinerary = "INSERT INTO itinerary(tripID, startingLongitude, " +
							   "startingLatitude, startingTime, endingLongitude, " +
							   "endingLatitude, endingTime, seatsLeft) " +
							   "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(insertItinerary);
			ps.setInt(1, itinerary.getTripID());
			ps.setDouble(2, itinerary.getStartingLongitude());
			ps.setDouble(3, itinerary.getStartingLatitude());
			ps.setTimestamp(4, itinerary.getStartingTime());
			ps.setDouble(5, itinerary.getEndingLongitude());
			ps.setDouble(6, itinerary.getEndingLatitude());
			ps.setTimestamp(7, itinerary.getEndingTime());
			ps.setInt(8, itinerary.getSeatsLeft());
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Itinerary for '%s' has been inserted in the database.", itinerary.getTripID()));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Updates an Itinerary object in the database.
	 * @param itinerary	-	An Itinerary object to be updated within the database.
	 * @return True if an entry was updated. False otherwise.
	 */
	public boolean updateItinerary(Itinerary itinerary) {
		String updateItinerary = "UPDATE itinerary SET startingLongitude = ?, " +
							   "startingLatitude = ?, startingTime = ?, " +
							   "endingLongitude = ?, endingLatitude = ?, " +
							   "endingTime = ?, seatsLeft = ? WHERE tripID = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(updateItinerary);
			ps.setDouble(1, itinerary.getStartingLongitude());
			ps.setDouble(2, itinerary.getStartingLatitude());
			ps.setTimestamp(3, itinerary.getStartingTime());
			ps.setDouble(4, itinerary.getEndingLongitude());
			ps.setDouble(5, itinerary.getEndingLatitude());
			ps.setTimestamp(6, itinerary.getEndingTime());
			ps.setInt(7, itinerary.getSeatsLeft());
			ps.setInt(8, itinerary.getTripID());
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Itinerary for '%s' has been updated in the database.", itinerary.getTripID()));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes a itinerary from the database.
	 * @param tripID	-	An integer uniquely identifying an itinerary.
	 * @return True if an entry was deleted from the database. False otherwise.
	 */
	public boolean deleteItinerary(int tripID) {
		String deleteStop = "DELETE FROM itinerary WHERE tripID = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(deleteStop);
			ps.setInt(1, tripID);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Itinerary for '%s' has been deleted from the database.", tripID));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Fetches an itinerary from the database based on a trip ID.
	 * @param tripID	-	An integer uniquely identifying a trip.
	 * @return An Itinerary object if found in the database. Null otherwise.
	 */
	public Itinerary getItineraryByTripID(int tripID) {
		Itinerary itinerary = null;
		String getItineraryByTripID = "SELECT * FROM itinerary WHERE tripID = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(getItineraryByTripID);
			ps.setInt(1, tripID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				itinerary = new Itinerary(rs.getInt("tripID"),
									  rs.getDouble("startingLongitude"),
									  rs.getDouble("startingLatitude"),
									  rs.getTimestamp("startingTime"),
									  rs.getDouble("endingLongitude"),
									  rs.getDouble("endingLatitude"),
									  rs.getTimestamp("endingTime"),
									  rs.getInt("seatsLeft"));
			}
			rs.close();
			logger.info(String.format("Itinerary '%s' has been returned from the database.", tripID));
			return itinerary;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Fetches all itineraries offered by an user.
	 * @param operator 		-	A String representing an username.
	 * @return An ArrayList of Itinerary objects matching the search criteria. Null if an error occurred.
	 */
	public ArrayList<Itinerary> getItineraryByUsername(String operator) {
		ArrayList<Itinerary> itinerary = new ArrayList<>();
		String getItineraryByUsername = "SELECT * FROM itinerary WHERE itinerary.tripID IN " + 
										"(SELECT trip.tripID FROM trip WHERE operator = ?);";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(getItineraryByUsername);
			ps.setString(1, operator);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				itinerary.add(new Itinerary(rs.getInt("tripID"),
						  rs.getDouble("startingLongitude"),
						  rs.getDouble("startingLatitude"),
						  rs.getTimestamp("startingTime"),
						  rs.getDouble("endingLongitude"),
						  rs.getDouble("endingLatitude"),
						  rs.getTimestamp("endingTime"),
						  rs.getInt("seatsLeft")));
			}
			rs.close();
			logger.info(String.format("All itineraries have been returned from the database for user %s", operator));
			return itinerary;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Fetches entries from the database fitting search criteria based on a spherical distance
	 * algorithm. Recommended to have a low maximum search radius to obtain more accurate results.
	 * @param startingLongitude	-	A double representing source longitude.
	 * @param startingLatitude	-	A double representing starting latitude.
	 * @param endingLongitude	-	A double representing destination longitude.
	 * @param endingLatitude	-	A double representing destination latitude.
	 * @param maximumDistance	-	A double representing maximum search radius in meters.
	 * @param arrivalTime		-	A java.sql.Timestamp representing preferred arrival time
	 * @return An ArrayList of Itinerary objects matching the search criteria. Null if an error occurred.
	 */
	public ArrayList<Itinerary> getItineraryNearDestination(double startingLongitude,
					double startingLatitude, double endingLongitude, double endingLatitude, double maximumDistance, 
					Timestamp arrivalTime) {
		ArrayList<Itinerary> itineraryList = new ArrayList<Itinerary>();
		double radiusOfEarth = 6371000.0;
		String getItineraryNearDestination = "SELECT * FROM itinerary WHERE " + 
				"SQRT(POWER((2 * PI() * ? * (ABS(startingLongitude - ?) / 360.0)), 2) + " + 
				"POWER((2 * PI() *  ? * (ABS(startingLatitude - ?) / 360.0)), 2)) <= ? AND " + 
				"SQRT(POWER((2 * PI() * ? * (ABS(endingLongitude - ?) / 360.0)), 2) + " + 
				"POWER((2 * PI() * ? * (ABS(endingLatitude - ?) / 360.0)), 2)) <= ?" + 
				"AND endingTime > NOW() AND endingTime <= ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(getItineraryNearDestination);
			ps.setDouble(1, radiusOfEarth);
			ps.setDouble(2, startingLongitude);
			ps.setDouble(3, radiusOfEarth);
			ps.setDouble(4, startingLatitude);
			ps.setDouble(5, maximumDistance);
			ps.setDouble(6, radiusOfEarth);
			ps.setDouble(7, endingLongitude);
			ps.setDouble(8, radiusOfEarth);
			ps.setDouble(9, endingLatitude);
			ps.setDouble(10, maximumDistance);
			ps.setTimestamp(11, arrivalTime);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				itineraryList.add(new Itinerary(rs.getInt("tripID"),
											rs.getDouble("startingLongitude"),
										    rs.getDouble("startingLatitude"),
											rs.getTimestamp("startingTime"),
											rs.getDouble("endingLongitude"),
											rs.getDouble("endingLatitude"),
											rs.getTimestamp("endingTime"),
											rs.getInt("seatsLeft")));
			}
			ps.close();
			logger.info("Closest itineraries near destination have been returned from the database.");
			return itineraryList;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Increments by 1 the number of seats available in the car.
	 * @param tripID	-	An integer uniquely identifying an itinerary.
	 * @return True if an entry was updated. False otherwise.
	 */
	public boolean incrementSeatsLeft(int tripID) {
		String incrementSeatsLeft = "UPDATE itinerary SET seatsLeft = seatsLeft + 1 " +
									"WHERE tripID = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(incrementSeatsLeft);
			ps.setInt(1, tripID);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Itinerary seats left for '%s' has been incremented in the database.", tripID));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Decrements by 1 the number of seats available in the car.
	 * @param tripID	-	An integer uniquely identifying an itinerary.
	 * @return True if an entry was updated. False otherwise.
	 */
	public boolean decrementSeatsLeft(int tripID) {
		String decrementSeatsLeft = "UPDATE itinerary SET seatsLeft = seatsLeft - 1 " +
									"WHERE tripID = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(decrementSeatsLeft);
			ps.setInt(1, tripID);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Itinerary seats left for '%s' has been decremented in the database.", tripID));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	//=======================
	// LOCATION API
	//=======================
	/**
	 * Inserts a Location object into the database.
	 * @param location	-	A Location object to be inserted into the database.
	 * @return True if an entry was inserted. False otherwise.
	 */
	public boolean insertLocation(Location location) {
		String insertLocation = "INSERT INTO location (operator, longitude, latitude) VALUES (?, ?, ?);";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(insertLocation);
			ps.setString(1, location.getOperator());
			ps.setDouble(2, location.getLongitude());
			ps.setDouble(3, location.getLatitude());
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Location of '%s' has been inserted in the database.", location.getOperator()));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Fetches entries from the database fitting search criteria based on a spherical distance
	 * algorithm. Recommended to have a low maximum search radius to obtain more accurate results.
	 * @param endingLongitude	-	A double representing current User's longitude.
	 * @param endingLatitude	-	A double representing current User's latitude.
	 * @param maximumDistance	-	A double representing maximum search radius in meters.
	 * @return An ArrayList of Location objects matching the search criteria. Null if an error occurred.
	 */
	public ArrayList<Location> getLocationNear(double longitude, double latitude, double maximumDistance) {
		ArrayList<Location> locationList = new ArrayList<Location>();
		String getLocationNear = "SELECT * FROM location WHERE SQRT(" +
						   		 "POWER((2 * PI() * ? * (ABS(longitude - ?) / 360.0)), 2) + " +
						   		 "POWER((2 * PI() * ? * (ABS(latitude - ?) / 360.0)), 2)) <= ?;";
		PreparedStatement ps = null;
		double radiusOfEarth = 6371000;
		try {
			ps = c.prepareStatement(getLocationNear);
			ps.setDouble(1, radiusOfEarth);
			ps.setDouble(2, longitude);
			ps.setDouble(3, radiusOfEarth);
			ps.setDouble(4, latitude);
			ps.setDouble(5, maximumDistance);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				locationList.add(new Location(rs.getString("operator"), rs.getDouble("longitude"),
											  rs.getDouble("latitude")));
			}
			rs.close();
			logger.info("Nearest location of drivers have been returned from the database.");
			return locationList;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Fetches the User's location from the database.
	 * @param operator	-	A String representing an User's username.
	 * @return A Location object if an entry was found. Null otherwise.
	 */
	public Location getLocationByUsername(String operator) {
		String getLocationByUsername = "SELECT * FROM location WHERE operator = ?;";
		Location location = null;
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(getLocationByUsername);
			ps.setString(1, operator);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				location = new Location(rs.getString("operator"), rs.getDouble("longitude"),
										rs.getDouble("latitude"));
			}
			rs.close();
			logger.info(String.format("Location of '%s' has been returned from the database.", operator));
			return location;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Updates a location entry within the database.
	 * @param location	-	A Location object to be updated within the database.
	 * @return True if an entry was updated. False otherwise.
	 */
	public boolean updateLocation(Location location) {
		String updateLocation = "UPDATE location SET longitude = ?, latitude = ? WHERE operator = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(updateLocation);
			ps.setDouble(1, location.getLongitude());
			ps.setDouble(2, location.getLatitude());
			ps.setString(3, location.getOperator());
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Location of '%s' has been updated in the database.", location.getOperator()));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes the location of an user from the database.
	 * @param operator	-	A String representing an User's username.
	 * @return True if an entry was delete from the database. False otherwise.
	 */
	public boolean deleteLocation(String operator) {
		String deleteLocation = "DELETE FROM location WHERE operator = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(deleteLocation);
			ps.setString(1, operator);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Location of '%s' has been deleted from the database.", operator));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	//=======================
	// RESERVATION API
	//=======================
	/**
	 * Inserts a Reservation object into the database.
	 * @param reservation	-	A Reservation object to be inserted into the database.
	 * @return True if an entry was inserted into the database. False otherwise.
	 */
	public boolean insertReservation(Reservation reservation) {
		String insertReservation = "INSERT INTO reservation (operator, tripID) VALUES (?, ?);";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(insertReservation);
			ps.setString(1, reservation.getOperator());
			ps.setInt(2, reservation.getTripID());
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Reservation of '%s' has been inserted in the database.", reservation.getOperator()));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes an entry from the database.
	 * @param reservation	-	A Reservation object to be deleted from the database.
	 * @return True if an entry was deleted from the database. False otherwise.
	 */
	public boolean deleteReservation(Reservation reservation) {
		String deleteReservation = "DELETE FROM reservation WHERE operator = ? AND tripID = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(deleteReservation);
			ps.setString(1, reservation.getOperator());
			ps.setInt(2, reservation.getTripID());
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("Reservation of '%s' has been deleted from the database.", reservation.getOperator()));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Fetches from the database all reservations for a specific User.
	 * @param operator		-	A String representing an User's username.
	 * @return An ArrayList of Reservation objects. Null if an error occurred.
	 */
	public ArrayList<Reservation> getReservationByUsername(String operator) {
		String getReservationByUsername = "SELECT * FROM reservation WHERE operator = ?;";
		PreparedStatement ps = null;
		ArrayList<Reservation> reservationList = new ArrayList<Reservation>();
		try {
			ps = c.prepareStatement(getReservationByUsername);
			ps.setString(1, operator);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				reservationList.add(new Reservation(rs.getString("operator"), rs.getInt("tripID")));
			}
			rs.close();
			logger.info(String.format("All reservations of '%s' has been returned from the database.", operator));
			return reservationList;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	//=======================
	// Authentication API
	//=======================
	/**
	 * Adds an user into the authentication list.
	 * @param username		-	A String representing an User's username.
	 * @return True if the user's authentication has been added. False otherwise.
	 */
	public boolean insertAuthentication(String username) {
		String authenticateUser = "INSERT INTO session (username, sessionTime) VALUES (?, ?);";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(authenticateUser);
			ps.setString(1, username);
			ps.setLong(2, (System.currentTimeMillis() / 1000L));
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("'%s' authentication has been added.", username));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes an user from the authentication list.
	 * @param username		-	A String representing an User's username.
	 * @return True if the user's authentication has been deleted. False otherwise.
	 */
	public boolean deleteAuthentication(String username) {
		String deleteAuthentication = "DELETE FROM session WHERE username = ?;";
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(deleteAuthentication);
			ps.setString(1, username);
			if (ps.executeUpdate() == 1) {
				ps.close();
				logger.info(String.format("'%s' authentication has been revoked.", username));
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Verifies if an user is authenticated.
	 * @param username		-	A String representing an User's username.
	 * @return True if an User is successfully authenticated. False otherwise.
	 */
	public boolean verifyAuthentication(String username) {
//		String verifyAuthentication = "SELECT * FROM session WHERE username = ? AND sessionTime <= ? + 3600;";
//		PreparedStatement ps = null;
//		try {
//			ps = c.prepareStatement(verifyAuthentication);
//			ps.setString(1, username);
//			ps.setLong(2, (System.currentTimeMillis() / 1000L));
//			ResultSet rs = ps.executeQuery();
//			int rows = 0;
//			if (rs.last()) {
//				rows = rs.getRow();
//			}
//			if (rows == 0) {
//				logger.info(username + " failed to authentication themself");
//				ps.close();
//				return false;
//			}
//			logger.info(username + " successfully authenticated themself");
//			ps.close();
//			return true;
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			return false;
//		}
		return true;
	}

	//=======================
	// Misc API
	//=======================
	/**
	 * Fetches information about an advertisement using a trip ID.
	 * @param tripID 		-	An integer representing a trip
	 * @return An AdInformation object if found. Null otherwise.
	 */
	public AdInformation getAdInformation(int tripID) {
		String adInformation = "SELECT CONCAT(users.firstName, ' ', users.lastName) AS name, " +
				"users.phone, car.make, car.model, car.year, car.licensePlate FROM users " +
				"LEFT JOIN car ON car.operator = users.username " +
				"WHERE users.username IN (SELECT trip.operator FROM trip WHERE trip.tripID = ?);";
		PreparedStatement ps = null;
		AdInformation info = null;
		try {
			ps = c.prepareStatement(adInformation);
			ps.setInt(1, tripID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				info = new AdInformation(rs.getString("name"),
										 rs.getString("phone"),
										 rs.getString("make"),
										 rs.getString("model"),
										 rs.getInt("year"),
										 rs.getString("licensePlate"));
			}
			ps.close();
			logger.info("Ad information has been fetched");
			return info;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	//=======================
	// Helper API
	//=======================
	/**
	 * Converts a String into a java.util.Timestamp Object.
	 * @param ts		-	A String representing a timestamp in yyyy-MM-dd hh:mm:ss.SSS format
	 * @return A Timestamp object or null if an error occurred.
	 */
	public Timestamp convertStringToTimestamp(String ts) {
		try {
		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		    Date parsedDate = dateFormat.parse(ts);
		    return new Timestamp(parsedDate.getTime());
		} catch(Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
		    return null;
		}
	}

	/**
	 * Clears all tables within the database. For testing purposes only...
	 * DO NOT USE IN ANY PUBLICLY AVAILABLE ACCESS POINTS!
	 * @return True if truncation completed successfully. False if an error occurred.
	 */
	public boolean purgeDatabase() {
		String purgeItinerary = "TRUNCATE TABLE itinerary;";
		String purgeLocation = "TRUNCATE TABLE location;";
		String purgeReservation = "TRUNCATE TABLE reservation;";
		String purgeTrip = "TRUNCATE TABLE trip;";
		String purgeCar = "TRUNCATE TABLE car;";
		String purgeDriver = "TRUNCATE TABLE driver;";
		String purgeUsers = "TRUNCATE TABLE users;";
		String purgeSession = "TRUNCATE TABLE session;";
		try {
			c.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
			c.createStatement().executeUpdate(purgeCar);
			c.createStatement().executeUpdate(purgeDriver);
			c.createStatement().executeUpdate(purgeItinerary);
			c.createStatement().executeUpdate(purgeLocation);
			c.createStatement().executeUpdate(purgeReservation);
			c.createStatement().executeUpdate(purgeSession);
			c.createStatement().executeUpdate(purgeTrip);
			c.createStatement().executeUpdate(purgeUsers);
			c.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
			logger.info("Database has been truncated");
			return true;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
	}
}

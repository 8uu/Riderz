package com.ecse321.team10.riderz.controller;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecse321.team10.riderz.dto.AdInformationDto;
import com.ecse321.team10.riderz.dto.ItineraryDto;
import com.ecse321.team10.riderz.dto.LocationDto;
import com.ecse321.team10.riderz.dto.ReservationDto;
import com.ecse321.team10.riderz.model.AdInformation;
import com.ecse321.team10.riderz.model.Itinerary;
import com.ecse321.team10.riderz.model.Location;
import com.ecse321.team10.riderz.model.Reservation;
import com.ecse321.team10.riderz.sql.MySQLJDBC;

/**
 * Route Controller handling URLS and routing to the specified information within the DB
 * Handles all relevant information concerning itineraries
 */

@RestController
public class RouteController {

	@Autowired
	private MySQLJDBC sql;
	
	@Autowired
	private ModelMapper modelMapper;
	
	private LocationDto locationConvertToDto(Location location) {
		return modelMapper.map(location, LocationDto.class);
	}
	
	private ReservationDto reservationConvertToDto(Reservation reservation) {
		return modelMapper.map(reservation, ReservationDto.class);
	}
	
	private ItineraryDto intineraryConvertToDto(Itinerary itinerary) {
		return modelMapper.map(itinerary, ItineraryDto.class);
	}

	private AdInformationDto adInformationConvertToDto(AdInformation adInfo) {
		return modelMapper.map(adInfo, AdInformationDto.class);
	}

	/**
	 * Insert an itinerary
	 * 
	 * @param tripID            - An integer uniquely identifying a trip
	 * @param startingLongitude - A double representing the starting longitude
	 * @param startingLatitude  - A double representing the starting latitude
	 * @param startingTime      - A String representing the starting time
	 * @param endingLongitude  	- A double representing the ending longitude
	 * @param endingLatitude    - A double representing the ending latitude
	 * @param endingTime        - A String representing the ending time
	 * @param seatsLeft         - An integer representing the number of seats left
	 * @param operator 			- A String representing the username of the operator
	 * @return The created itinerary
	 */
	@PostMapping(path = "/insertItinerary/{tripID}/{startingLongitude}/{startingLatitude}/{startingTime}/{endingLongitude}/{endingLatitude}/{endingTime}/{seatsLeft}/{operator}")
	public ItineraryDto insertItinerary(@PathVariable("tripID") int tripID,
										@PathVariable("startingLongitude") double startingLongitude,
										@PathVariable("startingLatitude") double startingLatitude,
										@PathVariable("startingTime") String startingTime,
										@PathVariable("endingLongitude") double endingLongitude,
										@PathVariable("endingLatitude") double endingLatitude,
										@PathVariable("endingTime") String endingTime,
										@PathVariable("seatsLeft") int seatsLeft,
										@PathVariable("operator") String operator) {
		
		Timestamp startingTimeStamp = sql.convertStringToTimestamp(startingTime);
		Timestamp endingTimeStamp = sql.convertStringToTimestamp(endingTime);
		
		Itinerary itinerary = new Itinerary(tripID, startingLongitude, startingLatitude, startingTimeStamp, endingLongitude, endingLatitude, endingTimeStamp, seatsLeft);
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		if (sql.insertItinerary(itinerary)) {
			sql.closeConnection();
			return intineraryConvertToDto(itinerary);
		}
		sql.closeConnection();
		return null;
	}
	
	
	/**
	 * Update an itinerary
	 * 
	 * @param tripID            - An integer uniquely identifying a trip
	 * @param startingLongitude - A double representing the starting longitude
	 * @param startingLatitude  - A double representing the starting latitude
	 * @param startingTime      - A String representing the starting time
	 * @param endingLongitude   - A double representing the ending longitude
	 * @param endingLatitude    - A double representing the ending latitude
	 * @param endingTime        - A String representing the ending time
	 * @param seatsLeft         - An integer representing the number of seats left
	 * @param operator 			- A String representing the username of the operator
	 * @return The updated itinerary
	 */
	@PutMapping("/updateItinerary/{tripID}/{startingLongitude}/{startingLatitude}/{startingTime}/{endingLongitude}/{endingLatitude}/{endingTime}/{seatsLeft}/{operator}")
	public ItineraryDto updateItinerary(@PathVariable("tripID") int tripID,
										@PathVariable("startingLongitude") double startingLongitude,
										@PathVariable("startingLatitude") double startingLatitude,
										@PathVariable("startingTime") String startingTime,
										@PathVariable("endingLongitude") double endingLongitude,
										@PathVariable("endingLatitude") double endingLatitude,
										@PathVariable("endingTime") String endingTime,
										@PathVariable("seatsLeft") int seatsLeft,
										@PathVariable("operator") String operator) {
		
		Timestamp startingTimeStamp = sql.convertStringToTimestamp(startingTime);
		Timestamp endingTimeStamp = sql.convertStringToTimestamp(endingTime);
		
		Itinerary updatedItinerary = new Itinerary(tripID, startingLongitude, startingLatitude, startingTimeStamp, endingLongitude, endingLatitude, endingTimeStamp, seatsLeft);
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		if (sql.updateItinerary(updatedItinerary)) {
			sql.closeConnection();
			return intineraryConvertToDto(updatedItinerary);
		}
		sql.closeConnection();
		return null;
		
	}
	
	/**
	 * Delete an itinerary
	 * 
	 * @param tripID    - An integer uniquely identifying a trip
	 * @param operator 	- A String representing the username of the operator
	 * @return A string to indicate if the itinerary was deleted successfully or not.
	 */
	@DeleteMapping("/deleteItinerary/{tripID}/{operator}")
	public String deleteItinerary(@PathVariable("tripID") int tripID,
								  @PathVariable("operator") String operator) {
		
		int deleteTripID = tripID;
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		if (sql.deleteItinerary(deleteTripID)) {
			sql.closeConnection();
			return String.format("Itinerary %s was deleted.", deleteTripID);
		}
		sql.closeConnection();
		return "Itinerary does not exist.";	
	}
	
	/**
	 * Obtain the Itinerary information base on the tripID
	 * 
	 * @param tripID    - An integer uniquely identifying a trip
	 * @param operator 	- A String representing the username of the operator
	 * @return The Itinerary object if found, null otherwise.
	 */
	@GetMapping("/getItineraryByTripID/{tripID}/{operator}")
	public ItineraryDto getItineraryByTripID(@PathVariable("tripID") int tripID,
											 @PathVariable("operator") String operator) {
		Itinerary itinerary = null;
		
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		itinerary = sql.getItineraryByTripID(tripID);
		if (itinerary != null) {
			sql.closeConnection();
			return intineraryConvertToDto(itinerary);
		}
		sql.closeConnection();
		return null;
	}
	
	/**
	 * Obtains all the Itineraries fitting search criteria based on a spherical distance
	 * algorithm. Low search radius is recommended for accurate results.
	 * 
	 * @param endingLongitude	-	A double representing destination longitude.
	 * @param endingLatitude	-	A double representing destination latitude.
	 * @param maximumDistance	-	A double representing maximum search radius in meters. 
	 * @param arrivalTime       -   A String representing preferred arrival time
	 * @return A List of Itinerary representing the itineraries found base on the criteria. Return Null if none was found.
	 */
	@GetMapping("/getItineraryNearDestination")
	public List<ItineraryDto> getItineraryNearDestination(	@RequestParam double startingLongitude,
															@RequestParam double startingLatitude,
															@RequestParam double endingLongitude,
															@RequestParam double endingLatitude,
															@RequestParam double maximumDistance,
															@RequestParam String arrivalTime,
															@RequestParam String operator) {
		
		Timestamp arrivalTimeStamp = sql.convertStringToTimestamp(arrivalTime);
		
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		List<ItineraryDto> itineraryList = new ArrayList<ItineraryDto>();
		for(Itinerary itinerary : sql.getItineraryNearDestination(startingLongitude, startingLatitude, endingLongitude, endingLatitude, maximumDistance, arrivalTimeStamp))
			itineraryList.add(intineraryConvertToDto(itinerary));
		sql.closeConnection();
		return itineraryList;
	}
	
	/**
	 * Obtains all itinerary fitting search criteria.
	 * @param operator 			- A String representing an user. 
	 * @return A List of Itinerary representing the itineraries found based on the search criteria.
	 */
	@GetMapping("/getItineraryByUsername")
	public List<ItineraryDto> getItineraryByUsername(@RequestParam String operator) {
		sql.connect();
		List<ItineraryDto> itineraryList = new ArrayList<ItineraryDto>();
		for(Itinerary itinerary : sql.getItineraryByUsername(operator)) {
			itineraryList.add(intineraryConvertToDto(itinerary));
		}
		sql.closeConnection();
		return itineraryList;
	}
	
	/**
	 * Increment by 1 the number of seats left in the itinerary given by the tripID
	 * 
	 * @param tripID   	- An integer uniquely identifying a trip
	 * @param operator 	- A String representing the operator of the driver
	 * @return A string indicating if the number of seats was incremented successfully for the tripID
	 */
	@PutMapping("/incrementSeatsLeft/{tripID}/{operator}")
	public String incrementSeatsLeft(@PathVariable("tripID")int tripID,
									 @PathVariable("operator") String operator) {
		
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		if (sql.incrementSeatsLeft(tripID)) {
			sql.closeConnection();
			return String.format("The number of seats left in the itinerary %s was incremented.", tripID);
		}
		sql.closeConnection();
		return String.format("Itinerary %s does not exist.", tripID);
	}
	
	/**
	 * Decrements by 1 the number of seats left in the itinerary given by the tripID
	 * 
	 * @param tripID 	- An integer uniquely identifying a trip
	 * @param operator 	- A String representing the operator of the operator
	 * @return A string indicating if the number of seats was incremented successfully for the tripID
	 */
	@PutMapping("/decrementSeatsLeft/{tripID}/{operator}")
	public String decrementSeatsLeft(@PathVariable("tripID")int tripID,
									 @PathVariable("operator") String operator) {
		
		sql.connect();
		if (sql.decrementSeatsLeft(tripID)) {
			sql.closeConnection();
			return String.format("The number of seats left in the itinerary %s was decremented.", tripID);
		}
		sql.closeConnection();
		return String.format("Itinerary %s does not exist.", tripID);
	}
	
	/**
	 * Insert a Location object into the database. Note the operator must already exist.
	 * 
	 * Location object: operator, longitude, latitude
	 * @param operator  - A String representing the name of the driver
	 * @param longitude - A double representing the longitude
	 * @param latitude  - A double representing the latitude
	 * @return A String representing if the location was successfully inserted or not.
	 */
	@PostMapping("/insertLocation/{operator}/{longitude}/{latitude}")
	public String insertLocation(@PathVariable("operator") String operator,
								 @PathVariable("longitude") double longitude,
								 @PathVariable("latitude") double latitude) {
		
		Location location = new Location(operator, longitude, latitude);
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		if (sql.insertLocation(location)) {
			sql.closeConnection();
			return String.format("Location of %s has been inserted into the database.", location.getOperator());
		}
		sql.closeConnection();
		return String.format("Location of %s could not be inserted into the database.", location.getOperator());
	}
	
	/**
	 * Fetches entries from the database fitting search criteria based on a spherical distance
	 * algorithm. Recommended to have a low maximum search radius to obtain more accurate results.
	 * 
	 * @param endingLongitude	-	A double representing current User's longitude.
	 * @param endingLatitude	-	A double representing current User's latitude.
	 * @param maximumDistance	-	A double representing maximum search radius in meters.
	 * @return A List of Location objects matching the search criteria. Null if an error occurred.
	 */
	@GetMapping("/getLocationNear/{longitude}/{latitude}/{maximumDistance}/{operator}")
	public List<LocationDto> getLocationNear(@PathVariable("longitude") double longitude,
											 @PathVariable("latitude") double latitude,
											 @PathVariable("maximumDistance") double maximumDistance,
											 @PathVariable("operator") String operator){
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		List<LocationDto> locationList = new ArrayList<LocationDto>();
		for(Location location : sql.getLocationNear(longitude, latitude, maximumDistance))
			locationList.add(locationConvertToDto(location));
		sql.closeConnection();
		return locationList;
	}
		
	/**
	 * Fetches the User's location from the database.
	 * @param operator	-	A String representing an User's userName.
	 * @return A Location object if an entry was found. Null otherwise.
	 */
	@GetMapping("/getLocationByUsername/{operator}")
	public Location getLocationByUsername(@PathVariable("operator") String operator) {
		
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		Location location = sql.getLocationByUsername(operator);
		if (location != null) {
			sql.closeConnection();
			return location;
		}
		sql.closeConnection();
		return null;
	}
	
	/**
	 * Updates a location entry within the database.
	 * @param operator	-	A String representing the name of the operator.
	 * @param longitude -   A String representing the longitude.
	 * @param latitude  -   A String representing the latitude.
	 * @return A String representing if the location was successfully updated or not.
	 */
	@PutMapping("/updateLocation/{operator}/{longitude}/{latitude}")
	public String updateLocation(@PathVariable("operator") String operator,
								 @PathVariable("longitude") double longitude,
								 @PathVariable("latitude") double latitude) {
		
		Location location = new Location(operator, longitude, latitude);
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		if (sql.updateLocation(location)) {
			sql.closeConnection();
			return String.format("Location of %s has been updated.", operator);
		}
		sql.closeConnection();
		return String.format("Location of %s does not exist", operator);
	}
	
	/**
	 * Deletes the location of an user from the database.
	 * 
	 * @param operator  -	A String representing the name of the operator.
	 * @return A String representing if the location was successfully deleted or not.
	 */
	@DeleteMapping("/deleteLocation/{operator}")
	public String deleteLocation(@PathVariable("operator") String operator) {
		
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		if (sql.deleteLocation(operator)) {
			sql.closeConnection();
			return String.format("Location of %s has been deleted.", operator);
		}
		sql.closeConnection();
		return String.format("Location of %s does not exist", operator);
	}
	
	/**
	 * Inserts a Reservation object into the database.The operator and the tripId must exist
	 * @param opeartor	-	A String representing the name of the operator.
	 * @param tripID    -   An integer uniquely identifying a trip.
	 * @return A String representing if the Reservation was successfully inserted or not.
	 */
	@PostMapping("/insertReservation/{operator}/{tripID}")
	public String insertReservation(@PathVariable("operator") String operator,
									@PathVariable("tripID") int tripID) {
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		Reservation reservation = new Reservation(operator, tripID);
		if (sql.insertReservation(reservation)) {
			sql.closeConnection();
			return "Reservation of " + operator + " has been inserted for the tripID: " + tripID;
		}
		sql.closeConnection();
		return "Reservation of " + operator + " is invalid for the tripID: " + tripID;
	}
	
	/**
	 * Deletes an entry from the database.
	 * 
	 * @param operator  - A String representing the name of the operator.
	 * @param tripID    - An integer uniquely identifying a trip.
	 * @return A String representing if the Reservation was successfully deleted or not.
	 */
	@DeleteMapping("/deleteReservation/{operator}/{tripID}")
	public String deleteReservation(@PathVariable("operator") String operator,
									@PathVariable("tripID") int tripID) {
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		Reservation reservation = new Reservation(operator, tripID);
		if (sql.deleteReservation(reservation)) {
			sql.closeConnection();
			return "Reservation of " + operator + " has been deleted for the tripID: " + tripID;
		}
		sql.closeConnection();
		return "Reservation of " + operator + " is invalid for the tripID: " + tripID;
	}
	
	/**
	 * Fetches from the database all reservations for a specific User.
	 * 
	 * @param operator         - A String representing the name of the operator.
	 * @return A List of reservation for a specific user
	 */
	@GetMapping("/getReservationByUsername/{operator}")
	public List<ReservationDto> getReservationByUsername(@PathVariable("operator") String operator) {
		
		sql.connect();
		if (!sql.verifyAuthentication(operator)) {
			sql.closeConnection();
			return null;
		}
		List<ReservationDto> reservationList = new ArrayList<ReservationDto>();
		for(Reservation reservation : sql.getReservationByUsername(operator))
			reservationList.add(reservationConvertToDto(reservation));
		sql.closeConnection();
		return reservationList;
	}

	/**
	 * Obtains an information about an advertisement using a tripID.
	 * @param tripID
	 * @return
	 */
	@RequestMapping(value = "adInfo", method = RequestMethod.GET)
	public AdInformationDto getAdInfo( @RequestParam int tripID) {
		AdInformationDto ad = null;
		if (sql.connect()) {
			ad = adInformationConvertToDto(sql.getAdInformation(tripID));
		}
		sql.closeConnection();
		return ad;
	}
}

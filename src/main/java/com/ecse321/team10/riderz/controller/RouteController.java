package com.ecse321.team10.riderz.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecse321.team10.riderz.dto.ItineraryDto;
import com.ecse321.team10.riderz.dto.UserDto;
import com.ecse321.team10.riderz.model.Itinerary;
import com.ecse321.team10.riderz.model.User;
// Add 3 imports: itinerarydto, locationdto, reservationdto
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
	/*
	private LocationDto locationConvertToDto(Location location) {
		return modelMapper.map(location, LocationDto.class);
	}

	private ReservationDto reservationConvertToDto(Reservation reservation) {
		return modelMapper.map(reservation, ReservationDto.class);
	}
	 */
	private ItineraryDto intineraryConvertToDto(Itinerary itinerary) {
		return modelMapper.map(itinerary, ItineraryDto.class);
	}
	
	private static final Logger logger = LogManager.getLogger(RiderzController.class);
	
	//For Testing purpose:
	//localhost:8088/insertItinerary/36/15.33534/12.44412/2019-01-01 01:00:00.000/40.33245/34.33214/2019-01-01 01:30:00.000/3
	
	/**
	 * Insert an itinerary
	 * 
	 * @param tripID             - An integer uniquely identifying a trip
	 * @param startingLongitude  - A double representing the starting longitude
	 * @param startingLatitude   - A double representing the starting latitude
	 * @param startingTime       - A String representing the starting time
	 * @param endingLongitude    - A double representing the ending longitude
	 * @param endingLatitude     - A double representing the ending latitude
	 * @param endingTime         - A String representing the ending time
	 * @param seatsLeft          - An integer representing the number of seats left
	 * @return itinerary         - The created itinerary
	 */
	@GetMapping("/insertItinerary/{tripID}/{startingLongitude}/{startingLatitude}/{startingTime}/{endingLongitude}/{endingLatitude}/{endingTime}/{seatsLeft}")
	public ItineraryDto insertItinerary(@PathVariable("tripID") int tripID,
										@PathVariable("startingLongitude") double startingLongitude,
										@PathVariable("startingLatitude") double startingLatitude,
										@PathVariable("startingTime") String startingTime,
										@PathVariable("endingLongitude") double endingLongitude,
										@PathVariable("endingLatitude") double endingLatitude,
										@PathVariable("endingTime") String endingTime,
										@PathVariable("seatsLeft") int seatsLeft) {
		
		Timestamp startingTimeStamp = stringtoTimeStamp(startingTime);
		Timestamp endingTimeStamp = stringtoTimeStamp(endingTime);
		
		Itinerary itinerary = new Itinerary(tripID, startingLongitude, startingLatitude, startingTimeStamp, endingLongitude, endingLatitude, endingTimeStamp, seatsLeft);
		sql.connect();
		if (sql.insertItinerary(itinerary)) {
			sql.closeConnection();
			return intineraryConvertToDto(itinerary);
		}
		sql.closeConnection();
		return null;
	}
	
	//For Testing purpose:
	//localhost:8088/updateItinerary/36/25.1111/26.3114/2019-01-01 02:00:00.000/40.33245/34.33214/2019-01-01 02:30:05.000/1
	
	/**
	 * Update an itinerary
	 * 
	 * @param tripID             - An integer uniquely identifying a trip
	 * @param startingLongitude  - A double representing the starting longitude
	 * @param startingLatitude   - A double representing the starting latitude
	 * @param startingTime       - A String representing the starting time
	 * @param endingLongitude    - A double representing the ending longitude
	 * @param endingLatitude     - A double representing the ending latitude
	 * @param endingTime         - A String representing the ending time
	 * @param seatsLeft          - An integer representing the number of seats left
	 * @return updatedItinerary  - the updated itinerary
	 */
	@GetMapping("/updateItinerary/{tripID}/{startingLongitude}/{startingLatitude}/{startingTime}/{endingLongitude}/{endingLatitude}/{endingTime}/{seatsLeft}")
	public ItineraryDto updateItinerary(@PathVariable("tripID") int tripID,
										@PathVariable("startingLongitude") double startingLongitude,
										@PathVariable("startingLatitude") double startingLatitude,
										@PathVariable("startingTime") String startingTime,
										@PathVariable("endingLongitude") double endingLongitude,
										@PathVariable("endingLatitude") double endingLatitude,
										@PathVariable("endingTime") String endingTime,
										@PathVariable("seatsLeft") int seatsLeft) {
		
		Timestamp startingTimeStamp = stringtoTimeStamp(startingTime);
		Timestamp endingTimeStamp = stringtoTimeStamp(endingTime);
		
		Itinerary updatedItinerary = new Itinerary(tripID, startingLongitude, startingLatitude, startingTimeStamp, endingLongitude, endingLatitude, endingTimeStamp, seatsLeft);
		sql.connect();
		if (sql.updateItinerary(updatedItinerary)) {
			sql.closeConnection();
			return intineraryConvertToDto(updatedItinerary);
		}
		sql.closeConnection();
		return null;
		
	}
	
	//For Testing purpose:
	//localhost:8088/deleteItinerary/36
	
	/**
	 * Delete an itinerary
	 * 
	 * @param tripID     - An integer uniquely identifying a trip
	 * @return message   - A string to indicate if the itinerary was deleted successfully or not.
	 */
	@GetMapping("/deleteItinerary/{tripID}")
	public String deleteItinerary(@PathVariable("tripID") int tripID) {
		
		int deleteTripID = tripID;
		sql.connect();
		if (sql.deleteItinerary(deleteTripID)) {
			sql.closeConnection();
			return String.format("Itinerary %s was deleted.", deleteTripID);
		}
		sql.closeConnection();
		return "Itinerary does not exist.";	
	}
	
	//For Testing purpose:
	//localhost:8088/getItineraryByTripID/36
	/**
	 * Obtain the Itinerary information base on the tripID
	 * 
	 * @param tripID     - An integer uniquely identifying a trip
	 * @return itinerary - The Itinerary object if found, null otherwise.
	 */
	@GetMapping("/getItineraryByTripID/{tripID}")
	public ItineraryDto getItineraryByTripID(@PathVariable("tripID") int tripID) {
		Itinerary itinerary = null;
		
		sql.connect();
		itinerary = sql.getItineraryByTripID(tripID);
		if (itinerary != null) {
			sql.closeConnection();
			return intineraryConvertToDto(itinerary);
		}
		sql.closeConnection();
		return null;
	}
	
	
	//For Testing purpose:
	// *** Delete old stuff in DB ***
	//localhost:8088/insertItinerary/35/45.419980/-73.883442/2019-01-01 02:00:00.000/45.456180/-73.862320/2019-01-01 02:30:00.000/2
	//localhost:8088/getItineraryNearDestination/45.456181/-73.862321/0.500000/2019-01-01 03:30:00.000
	//localhost:8088/getItineraryNearDestination/45.456181/-73.862321/0.160000/2050-02-02 02:30:00.000
	//localhost:8088/getItineraryNearDestination/10.456181/-10.862321/500000000.160000/2019-01-02 03:30:00.000
	
	//localhost:8088/insertItinerary/36/15.33534/12.44412/2019-01-01 01:00:00.000/45.456580/-73.869320/2019-01-01 01:30:00.000/3
	//localhost:8088/getItineraryNearDestination/45.456880/-73.869920/1000.00000/2019-01-01 04:30:00.000
	/**
	 * Obtains all the Itineraries fitting search criteria based on a spherical distance
	 * algorithm. Low search radius is recommended for accurate results.
	 * 
	 * @param endingLongitude	-	A double representing destination longitude.
	 * @param endingLatitude	-	A double representing destination latitude.
	 * @param maximumDistance	-	A double representing maximum search radius in meters. 
	 * @param arrivalTime       -   A String representing preferred arrival time
	 * @return itineraryList    -   A List of Itinerary representing the itineraries found base on the criteria. Return Null if none was found.
	 */
	@GetMapping("/getItineraryNearDestination/{endingLongitude}/{endingLatitude}/{maximumDistance}/{arrivalTime}")
	public List<ItineraryDto> getItineraryNearDestination(	@PathVariable("endingLongitude") double endingLongitude,
															@PathVariable("endingLatitude") double endingLatitude,
															@PathVariable("maximumDistance") double maximumDistance,
															@PathVariable("arrivalTime") String arrivalTime){
		
		Timestamp arrivalTimeStamp = stringtoTimeStamp(arrivalTime);
		
		sql.connect();		
		List<ItineraryDto> itineraryList = new ArrayList<ItineraryDto>();
		for(Itinerary itinerary : sql.getItineraryNearDestination(endingLongitude, endingLatitude, maximumDistance, arrivalTimeStamp))
			itineraryList.add(intineraryConvertToDto(itinerary));
		sql.closeConnection();
		return itineraryList;
	}
	
	
	
	//localhost:8088/incrementSeatsLeft/35
	//localhost:8088/getItineraryByTripID/35
	/**
	 * Increment by 1 the number of seats left in the itinerary given by the tripID
	 * 
	 * @param tripIDAn integer uniquely identifying a trip
	 * @return message - A string indicating if the number of seats was incremented successfully for the tripID
	 */
	@GetMapping("/incrementSeatsLeft/{tripID}")
	public String incrementSeatsLeft(@PathVariable("tripID")int tripID) {
		
		sql.connect();
		if (sql.incrementSeatsLeft(tripID)) {
			sql.closeConnection();
			return String.format("The number of seats left in the itinerary %s was incremented.", tripID);
		}
		sql.closeConnection();
		return String.format("Itinerary %s does not exist.", tripID);
	}
	
	
	//localhost:8088/decrementSeatsLeft/35
	//localhost:8088/getItineraryByTripID/35
	/**
	 * Decrements by 1 the number of seats left in the itinerary given by the tripID
	 * 
	 * @param tripID - An integer uniquely identifying a trip
	 * @return message - A string indicating if the number of seats was incremented successfully for the tripID
	 */
	@GetMapping("/decrementSeatsLeft/{tripID}")
	public String decrementSeatsLeft(@PathVariable("tripID")int tripID) {
		
		sql.connect();
		if (sql.decrementSeatsLeft(tripID)) {
			sql.closeConnection();
			return String.format("The number of seats left in the itinerary %s was decremented.", tripID);
		}
		sql.closeConnection();
		return String.format("Itinerary %s does not exist.", tripID);
	}
	
	
	/**
	 * Helper Method: convert a string to a timeStamp
	 * 
	 * @param timeString - A time represented in a string
	 * @return timeStamp - A time represented by a timeStamp
	 */
	private Timestamp stringtoTimeStamp (String timeString) {
		try {
		    SimpleDateFormat dateLayout = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		    Date date = dateLayout.parse(timeString);
		    Timestamp timeStamp = new java.sql.Timestamp(date.getTime());
		    return timeStamp;
		} catch(Exception e) {
			return null;
		}
	}			
}

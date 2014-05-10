package fi.aalto.tripchain;

import fi.aalto.tripchain.Client;

interface ServiceConnectionApi {
	/**
	* Start recording trip.
	*/
	void start();
	
	/**
	* Stop recording trip.
	*/
	void stop();
	
	/**
	* Returns if trip recording is on-going.
	*/
	boolean recording();
	
	/**
	* Subscribes to location updates.
	* @param hashCode Hashcode for client.
	*/
	void subscribe(Client client, int hashCode);
	
	/**
	* Unsubscribes client
	* @param hashCode Client's hashcode.
	*/
	void unsubscribe(int hashCode);
}

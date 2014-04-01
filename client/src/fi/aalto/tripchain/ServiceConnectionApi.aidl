package fi.aalto.tripchain;

import fi.aalto.tripchain.Client;

interface ServiceConnectionApi {
	void start();
	void stop();
	boolean recording();
	
	void subscribe(Client client, int hashCode);
	void unsubscribe(int hashCode);
}

package fi.aalto.tripchain;

interface ServiceConnectionApi {
	void start();
	void stop();
	boolean recording();
}
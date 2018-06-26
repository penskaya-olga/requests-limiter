package app.test.annotation;

import java.time.LocalDateTime;

public class AddressData {
	
	private LocalDateTime startIntervalTime;
	
	private Integer currentRequestsCount;
	
	private Integer previousRequestsCount;

	
	public AddressData(LocalDateTime startIntervalTime,
			Integer requestsCount, Integer oldRequestCount) {
		this.startIntervalTime = startIntervalTime;
		this.currentRequestsCount = requestsCount;
		this.previousRequestsCount = oldRequestCount;
	}


	public LocalDateTime getStartIntervalTime() {
		return startIntervalTime;
	}

	public Integer getCurrentRequestsCount() {
		return currentRequestsCount;
	}
		
	public Integer getPreviousRequestsCount() {
		return previousRequestsCount;
	}

	@Override
	public String toString() {
		return "startIntervalTime: " + startIntervalTime 
				+ ", currentRequestsCount: " + currentRequestsCount
				+ ", previousRequestsCount: " + previousRequestsCount;
		
	}
}

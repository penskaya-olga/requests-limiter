package app.test.test_1;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.management.timer.TimerMBean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import app.test.annotation.AddressData;
import app.test.exception.BadGatewayException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CheckRequestTest {
	
	private final String IP_ADDRESS_MASK = "192.168.0.%d";	
	private final int THREADS_COUNT = 5;
	private final int REQUESTS_COUNT = 5;
	
	private Map<String, Integer> ipsArray;
	
	private static AtomicReference<ConcurrentHashMap<String, AtomicReference<AddressData>>> currentIpsMap = 
			new AtomicReference<ConcurrentHashMap<String, AtomicReference<AddressData>>>(
					new ConcurrentHashMap<String, AtomicReference<AddressData>>());
	private static AtomicReference<ConcurrentHashMap<String, AtomicReference<AddressData>>> previousIpsMap = 
			new AtomicReference<ConcurrentHashMap<String, AtomicReference<AddressData>>>(
					new ConcurrentHashMap<String, AtomicReference<AddressData>>());
	
	private static AtomicReference<LocalDateTime> currentIntervalTime =  new AtomicReference<LocalDateTime>();
			
	
	@Before
	public void init() {
		ipsArray = new HashMap<String, Integer>();
		for(int i = 1; i <= THREADS_COUNT; i++) {
			String ip = String.format(IP_ADDRESS_MASK, i);
			ipsArray.put(ip, REQUESTS_COUNT);			
		}
	}
	
	@Test
    public void checkRequest() throws InterruptedException{		
//		CountDownLatch counter = new CountDownLatch(THREADS_COUNT*REQUESTS_COUNT);
//		Executor executor = Executors.newFixedThreadPool(THREADS_COUNT);		
//		
//		for(int i = 1; i <= THREADS_COUNT; i++) {
//			final int currentThreadNumber = i;
//			String ip = String.format(IP_ADDRESS_MASK, currentThreadNumber);
//			executor.execute(new CheckRequestRunnable(counter, ip, REQUESTS_COUNT));
//		}
//		counter.await();
//		
//		assertThat(currentIpsMap.get()).hasSameSizeAs(ipsArray);
//		assertThat(previousIpsMap.get()).hasSameSizeAs(ipsArray);
//		
//		for (String key : currentIpsMap.get().keySet()) {
//			assertThat(key).isIn(ipsArray.keySet());
//		}
//		
//		for (String key : previousIpsMap.get().keySet()) {
//			assertThat(key).isIn(ipsArray.keySet());
//		}
//		
//		for (Entry<String,Integer> entry : ipsArray.entrySet()) {
//			assertThat(currentIpsMap.get().get(entry.getKey())).isSameAs(entry.getValue());
//		}
//		
//		//check previousIpsMap
//		for (Entry<String,Integer> entry : ipsArray.entrySet()) {
//			assertThat(previousIpsMap.get().get(entry.getKey())).isSameAs(entry.getValue());
//		}
	}
	
	@Test
	public void checkTimeInterval() throws InterruptedException, ParseException {
//		
//	    CountDownLatch counter = new CountDownLatch(126);	    
//	    
//		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//		ScheduledExecutorService executor2 = Executors.newSingleThreadScheduledExecutor();
//		
//		executor.scheduleAtFixedRate(new CheckRequestRunnable(counter, 
//				String.format(IP_ADDRESS_MASK, 1), 1), 0, 60, TimeUnit.SECONDS);	
//		
//		executor.schedule(new CheckRequestRunnable(counter, 
//				String.format(IP_ADDRESS_MASK, 1), 49), 50, TimeUnit.SECONDS);		
//		
//		executor.schedule(new CheckRequestRunnable(counter, 
//				String.format(IP_ADDRESS_MASK, 1), 2), 70, TimeUnit.SECONDS);
//		
//		executor.schedule(new CheckRequestRunnable(counter, 
//				String.format(IP_ADDRESS_MASK, 1), 5), 130, TimeUnit.SECONDS);
//		
//				
//		executor2.scheduleAtFixedRate(new CheckRequestRunnable(counter, 
//				String.format(IP_ADDRESS_MASK, 2), 30), 10, 60, TimeUnit.SECONDS);
//		
//		executor2.schedule(new CheckRequestRunnable(counter, 
//				String.format(IP_ADDRESS_MASK, 2), 5), 90, TimeUnit.SECONDS);		
//		
//		executor2.schedule(new CheckRequestRunnable(counter, 
//				String.format(IP_ADDRESS_MASK, 2), 3), 260, TimeUnit.SECONDS);		
//		
//		
//		counter.await();
//		executor.shutdown();
//		executor2.shutdown();
	}
	
	
	class CheckRequestRunnable implements Runnable {		
		private final Integer MAX_COUNT_CONNECTION_PER_MINUTE = 50;
		private String ipAddress;		
		private CountDownLatch counter;
		private int requests;
		   
		CheckRequestRunnable(CountDownLatch counter, String ipAddress, int requests) {
		    this.counter = counter;
		    this.ipAddress = ipAddress;
		    this.requests = requests;
		}
		
		@Override
		public void run() {			
			
		    for (int j = 1; j <= requests; j++) {		       
		       
		    	boolean exchangeSucceed = false;
		    	
		    	String ipAddress = this.ipAddress;		    	
		    	LocalDateTime requestTime = LocalDateTime.now();
		    	
		    	System.out.println(ipAddress + " request time: " + requestTime);
		    	
		    	if (currentIntervalTime.get() == null) {
		    		exchangeSucceed = false;
		    		while(!exchangeSucceed)
		    			exchangeSucceed = currentIntervalTime.compareAndSet(null, requestTime.truncatedTo(ChronoUnit.MINUTES));
		    	}
		    			    	
		    	//check that requests were during last minute
				if (currentIntervalTime.get().plusMinutes(1).isBefore(requestTime) 
						|| currentIntervalTime.get().plusMinutes(1).isEqual(requestTime)) {
					
					exchangeSucceed = false;
		    		while(!exchangeSucceed)
		    			exchangeSucceed = currentIntervalTime.compareAndSet(currentIntervalTime.get(), requestTime.truncatedTo(ChronoUnit.MINUTES));

					exchangeSucceed = false;
			    	while(!exchangeSucceed)
			    		exchangeSucceed = previousIpsMap.compareAndSet(previousIpsMap.get(), currentIpsMap.get());
					
					exchangeSucceed = false;
		    		while(!exchangeSucceed)
		    			exchangeSucceed = currentIpsMap.compareAndSet(currentIpsMap.get(), 
		    					new ConcurrentHashMap<String, AtomicReference<AddressData>>());
				}
								
				if (currentIpsMap.get().get(ipAddress) == null) {
					if (previousIpsMap.get().get(ipAddress) != null) {						
						currentIpsMap.get().put(ipAddress, previousIpsMap.get().get(ipAddress));
					} else {
						currentIpsMap.get().put(ipAddress, new AtomicReference<AddressData>(
								new AddressData(requestTime, 0, 0)));
					}
				} 
				
				AddressData currentData = currentIpsMap.get().get(ipAddress).get();
				
				if (currentData.getStartIntervalTime().plusMinutes(1).isAfter(requestTime) 
						|| currentData.getStartIntervalTime().plusMinutes(1).isEqual(requestTime)) {
					
					exchangeSucceed = false;
					while(!exchangeSucceed) {
						exchangeSucceed = currentIpsMap.get().get(ipAddress).compareAndSet(currentData, 
								new AddressData(currentData.getStartIntervalTime(), 
										currentData.getCurrentRequestsCount() + 1, 
										currentData.getPreviousRequestsCount()));
					}					
				} else if (ChronoUnit.MINUTES.between(requestTime, 
						currentIpsMap.get().get(ipAddress).get()
						.getStartIntervalTime()) < 2){	
					
					exchangeSucceed = false;
					while(!exchangeSucceed)
						exchangeSucceed = currentIpsMap.get().get(ipAddress).compareAndSet(currentData, 
								new AddressData(currentData.getStartIntervalTime().plusMinutes(1), 
										1, currentData.getCurrentRequestsCount()));									
				} else {
					exchangeSucceed = false;
					while(!exchangeSucceed)
						exchangeSucceed = currentIpsMap.get().get(ipAddress).compareAndSet(
								currentIpsMap.get().get(ipAddress).get(), new AddressData(requestTime, 1, 0));					
				}
				
				System.out.println("currentIpsMap: " + currentIpsMap.get() + " previousIpsMap: " + previousIpsMap.get());

				if (currentIpsMap.get().get(ipAddress)
						.get().getCurrentRequestsCount() > MAX_COUNT_CONNECTION_PER_MINUTE
						|| currentIpsMap.get().get(ipAddress)
						.get().getPreviousRequestsCount() > MAX_COUNT_CONNECTION_PER_MINUTE) {
					counter.countDown();
					//throw new BadGatewayException();
					System.out.println(ipAddress + " throw new BadGatewayException()");
				} else {
					counter.countDown();
				}
		    }	
		    System.out.println("_________________________ " + requests);
		}		
	}
		
		
}

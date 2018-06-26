package app.test.annotation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import app.test.exception.BadGatewayException;
import app.test.exception.InternalServerException;

@Aspect
@Component
public class CheckRequestAspect {
	
	private static AtomicReference<ConcurrentHashMap<String, AtomicReference<AddressData>>> currentIpsMap = 
			new AtomicReference<ConcurrentHashMap<String, AtomicReference<AddressData>>>(
					new ConcurrentHashMap<String, AtomicReference<AddressData>>());
	private static AtomicReference<ConcurrentHashMap<String, AtomicReference<AddressData>>> previousIpsMap = 
			new AtomicReference<ConcurrentHashMap<String, AtomicReference<AddressData>>>(
					new ConcurrentHashMap<String, AtomicReference<AddressData>>());
	
	private static AtomicReference<LocalDateTime> currentIntervalTime =  new AtomicReference<LocalDateTime>();
	
	private final static Integer MAX_COUNT_CONNECTION_PER_MINUTE = 50;
	
	
	@Around("@annotation(CheckRequest)")
	public Object checkRequest(ProceedingJoinPoint joinPoint) throws BadGatewayException, InternalServerException {
		
        boolean exchangeSucceed = false;
					
        LocalDateTime requestTime = LocalDateTime.now();
        String ipAddress = getIpAddress();
		
        //set first interval time
        if (currentIntervalTime.get() == null) {
    		exchangeSucceed = false;
    		while(!exchangeSucceed)
    			exchangeSucceed = currentIntervalTime.compareAndSet(null, requestTime.truncatedTo(ChronoUnit.MINUTES));
    	}
    	
        //change maps references each minutes
    	if (currentIntervalTime.get().plusMinutes(1).isBefore(requestTime) 
				|| currentIntervalTime.get().plusMinutes(1).isEqual(requestTime)) {
			
			exchangeSucceed = false;
    		while(!exchangeSucceed)
    			exchangeSucceed = currentIntervalTime.compareAndSet(currentIntervalTime.get(), 
    					requestTime.truncatedTo(ChronoUnit.MINUTES));

			exchangeSucceed = false;
	    	while(!exchangeSucceed)
	    		exchangeSucceed = previousIpsMap.compareAndSet(previousIpsMap.get(), currentIpsMap.get());
			
			exchangeSucceed = false;
    		while(!exchangeSucceed)
    			exchangeSucceed = currentIpsMap.compareAndSet(currentIpsMap.get(), 
    					new ConcurrentHashMap<String, AtomicReference<AddressData>>());
		}
		
    	//requests count updating by ip address
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
		
		if (currentIpsMap.get().get(ipAddress)
				.get().getCurrentRequestsCount() > MAX_COUNT_CONNECTION_PER_MINUTE
				|| currentIpsMap.get().get(ipAddress)
				.get().getPreviousRequestsCount() > MAX_COUNT_CONNECTION_PER_MINUTE) {			
			throw new BadGatewayException();			
		}
		
		Object proceed = null;
		try {
			proceed = joinPoint.proceed();			
		} catch (Throwable e) {
			throw new InternalServerException();			
		}
		
		return proceed;		
	}
	
	private String getIpAddress() {
		
		String ipAddress = "";		
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes != null) {
			HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
			if (servletRequest != null) {
				ipAddress = servletRequest.getHeader("X-FORWARDED-FOR");
				if (ipAddress == null || ipAddress.isEmpty()) {
					ipAddress = servletRequest.getRemoteAddr();
				}
			} 
		}
		return ipAddress;
	}

}
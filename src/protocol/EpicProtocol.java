package protocol;

import java.util.Random;

/**
 * Created by Sander on 18/02/2016.
 */
public class EpicProtocol implements IMACProtocol {

	boolean firstPacketSent;
	boolean sending;
	int waitingTimeSlots;
	
	
	public EpicProtocol() {
		firstPacketSent = false;
		sending = false;
		
	}
	
	
    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState,
                                              int controlInformation, int localQueueLength) {
        // No data to send, just be quiet
        if (localQueueLength == 0) {
            System.out.println("SLOT - No data to send.");
            firstPacketSent = false;
            sending = false;
            return new TransmissionInfo(TransmissionType.Silent, 0);
        }
        
        //Als deze aan het zenden is, blijf doorzenden totdat de queue length 0 is
        if (sending) {
        	return new TransmissionInfo(TransmissionType.Data, localQueueLength);
        }
        
        //Als deze een queue heeft en in het vorige timeslot geen pakketje heeft ontvangen
        //Verstuur een pakketje
        if (!firstPacketSent && !sending && (controlInformation == 0 || previousMediumState == MediumState.Idle)) {
        	firstPacketSent = true;
        	return new TransmissionInfo(TransmissionType.Data, localQueueLength);
        }
        
        
        
        
        //Als deze het eerste pakketje heeft verzonden en er is geen collision, zet sending op true
        if (firstPacketSent && previousMediumState != MediumState.Collision) {
        	sending = true;
        	return new TransmissionInfo(TransmissionType.Data, localQueueLength);
        } 
        //Als er wel een collisions was, zet de timer op een random getal tussen 0 en 4.
        //Dan wachten enzo
        else if (firstPacketSent && previousMediumState == MediumState.Collision) {
        	waitingTimeSlots = (int) Math.round(Math.random() * 4);
        }
        
        //Wachten
        //Als wachten voorbij, verstuur pakketje
        if (firstPacketSent && !sending) {
        	waitingTimeSlots--;
        	if (waitingTimeSlots <= 0){
        		return new TransmissionInfo(TransmissionType.Data, localQueueLength);
        	} else {
        		return new TransmissionInfo(TransmissionType.Silent, 0);
        	}
        }
        
        
        return new TransmissionInfo(TransmissionType.Silent, 0);
    }
}

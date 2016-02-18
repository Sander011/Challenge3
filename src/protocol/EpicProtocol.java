package protocol;

import java.util.Random;

/**
 * Created by Sander on 18/02/2016.
 */
public class EpicProtocol implements IMACProtocol {

	int waitingTimeSlots;
    State state;
	
	
	public EpicProtocol() {
        state = State.INITIAL;
		
	}
	
	
    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState,
                                              int controlInformation, int localQueueLength) {

        if(state.equals(State.INITIAL)) {
            // No data to send, just be quiet
            if (localQueueLength == 0) {
                System.out.println("SLOT - No data to send.");
                return new TransmissionInfo(TransmissionType.Silent, 100);
            } else if (controlInformation == 0 || previousMediumState == MediumState.Idle) {
                state = State.FIRSTSEND;
                System.out.println("Send first packet");
                return new TransmissionInfo(TransmissionType.Data, localQueueLength);
            }
        }

        else if (state.equals(State.FIRSTSEND)) {
            //Als deze het eerste pakketje heeft verzonden en er is geen collision, zet sending op true
            if (previousMediumState != MediumState.Collision) {
                state = State.SENDING;
                System.out.println("Start sending");
                return new TransmissionInfo(TransmissionType.Data, localQueueLength);
            }
            //Als er wel een collisions was, zet de timer op een random getal tussen 0 en 4.
            //Dan wachten enzo
            else {
                state = State.WAITING;
                System.out.println("Start waiting");
                waitingTimeSlots = (int) Math.round(Math.random() * 4);
                return new TransmissionInfo(TransmissionType.Silent, 100);
            }
        }

        else if (state.equals(State.WAITING)) {
            waitingTimeSlots--;
            if (waitingTimeSlots <= 0){
                System.out.println("Waited long enough, send!");
                state = State.FIRSTSEND;
                return new TransmissionInfo(TransmissionType.Data, localQueueLength);
            } else {
                System.out.println("Continue waiting");
                return new TransmissionInfo(TransmissionType.Silent, 100);
            }
        }

        else if (state.equals(State.SENDING)) {
            System.out.println("Sending...");
            if(localQueueLength == 0) {
                state = State.INITIAL;
                System.out.println("Completed current queue");
                return new TransmissionInfo(TransmissionType.Silent, 100);
            }
            return new TransmissionInfo(TransmissionType.Data, localQueueLength);
        }


        System.out.println(state);
        return new TransmissionInfo(TransmissionType.Silent, 100);
    }
}

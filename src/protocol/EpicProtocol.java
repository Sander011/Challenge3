package protocol;

import java.util.Random;

/**
 * Created by Sander on 18/02/2016.
 */
public class EpicProtocol implements IMACProtocol {


    private final int MAX_SEND = (int) Math.pow(2, 3);
    private final int WAIT_TIME = 4;
    private final int HEADER_LAST_PACKET = 12345;

    int currentWaiting;
    int counter;
    int waitingTimeSlots;
    State state;


    public EpicProtocol() {
    	currentWaiting = WAIT_TIME;
        state = State.INITIAL;
        counter = 0;
    }


    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState,
                                              int controlInformation, int localQueueLength) {

        if (state.equals(State.INITIAL)) {
            // No data to send, just be quiet
            if (localQueueLength == 0) {
                System.out.println("SLOT - No data to send.");
                return new TransmissionInfo(TransmissionType.Silent, 0);
            } else if (controlInformation == HEADER_LAST_PACKET || previousMediumState == MediumState.Idle) {
                state = State.FIRSTSEND;
                System.out.println("Send first packet");
                return new TransmissionInfo(TransmissionType.Data, localQueueLength);
            } else if (controlInformation == HEADER_LAST_PACKET) {
                System.out.println("Sending limit reached, try sending");
                state = State.FIRSTSEND;
                return new TransmissionInfo(TransmissionType.Data, localQueueLength);
            } else {
                System.out.println("Someone is sending packets :(");
                return new TransmissionInfo(TransmissionType.Silent, 0);
            }
        } else if (state.equals(State.FIRSTSEND)) {
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
                waitingTimeSlots = (int) Math.round(Math.random() * currentWaiting);
                return new TransmissionInfo(TransmissionType.Silent, 0);
            }
        } else if (state.equals(State.WAITING)) {
            waitingTimeSlots--;
            if (waitingTimeSlots <= 0 && previousMediumState == MediumState.Idle) {
                System.out.println("Waited long enough, send!");
                state = State.FIRSTSEND;
                return new TransmissionInfo(TransmissionType.Data, localQueueLength);
            } else if (waitingTimeSlots <= 0) {
            	currentWaiting *= 2;
                state = State.INITIAL;
                return new TransmissionInfo(TransmissionType.Silent, 0);
            } else if (previousMediumState == MediumState.Succes) {
            	state = State.INITIAL;
                return new TransmissionInfo(TransmissionType.Silent, 0);
            }
            
            else {
                System.out.println("Continue waiting");
                return new TransmissionInfo(TransmissionType.Silent, 0);
            }
        } else if (state.equals(State.SENDING)) {
            counter++;
            currentWaiting = WAIT_TIME;
            if (localQueueLength == 0) {
                state = State.INITIAL;
                System.out.println("Completed current queue");
                return new TransmissionInfo(TransmissionType.NoData, HEADER_LAST_PACKET);
            }else if(counter >= MAX_SEND) {
                counter = 0;
                state = State.AFTERSEND;
                System.out.println("Sending limit reached");
                return new TransmissionInfo(TransmissionType.Data, HEADER_LAST_PACKET);
            }
            System.out.println("Sending...");
            return new TransmissionInfo(TransmissionType.Data, localQueueLength);
        } else if (state.equals(State.AFTERSEND)) {
            if(controlInformation == HEADER_LAST_PACKET) {
                System.out.println("Wait one turn");
                if(localQueueLength == 0) {
                    state = State.INITIAL;
                }
                return new TransmissionInfo(TransmissionType.Silent, 0);
            }
            if(previousMediumState == MediumState.Idle) {
                state = State.FIRSTSEND;
                return new TransmissionInfo(TransmissionType.Data, localQueueLength);
            } else if (previousMediumState == MediumState.Collision) {
                state = State.WAITING;
                System.out.println("Start waiting");
                waitingTimeSlots = 4;
                return new TransmissionInfo(TransmissionType.Silent, 0);
            } else {
                System.out.println("Other node sending succesfully");
                state = State.INITIAL;
                return new TransmissionInfo(TransmissionType.Silent, 0);
            }
        }


        System.out.println("Meh");
        return new TransmissionInfo(TransmissionType.Silent, 0);
    }
}

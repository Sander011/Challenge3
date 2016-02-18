package protocol;

import client.MACChallengeClient;

/**
 * Created by Sander on 18/02/2016.
 */
public class LessEpicProtocol implements IMACProtocol {

    private int node = 0;
    int count = -1;
    int counter = 8;
    int waitingTime = (int) Math.round(Math.random() * 4);
    State state = State.INITIAL;
    boolean tried = false;


    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        if(state == State.INITIAL) {
            if(previousMediumState == MediumState.Succes && tried) {
                System.out.println("Victory!");
                node = controlInformation;
                System.out.println(node);
                state = State.WAITING;
                return new TransmissionInfo(TransmissionType.Silent, 0);
            }
            if(previousMediumState == MediumState.Succes) {
                System.out.println("Someone won!");
                count = controlInformation;
                return new TransmissionInfo(TransmissionType.Silent, 0);
            }
            tried = false;
            if(previousMediumState == MediumState.Idle && waitingTime <= 0) {
                System.out.println("Lets try!");
                tried = true;
                return new TransmissionInfo(TransmissionType.NoData, count + 1);
            } else if (previousMediumState == MediumState.Collision) {
                System.out.println("Collide");
                waitingTime = (int) Math.round(Math.random() * 4);
                return new TransmissionInfo(TransmissionType.Silent, 0);
            } else {
                waitingTime--;
                System.out.println("Waiting");
            }
        } else if(state == State.WAITING) {
            if(controlInformation == 3 || node == 3) {
                state = State.SENDING;
            }
        } else if (state == State.SENDING) {
            if((controlInformation  % 4)  != node) {
                System.out.println("Not my turn!");
                return new TransmissionInfo(TransmissionType.Silent, 0);
            } else {
                if(counter != 0 && localQueueLength != 0) {
                    counter--;
                    return new TransmissionInfo(TransmissionType.Data, node);
                } else if(counter == 0) {
                    return new TransmissionInfo(TransmissionType.NoData, node+1);
                }else if(localQueueLength != 0) {
                    System.out.println("My turn!");
                    counter = 8;
                    return new TransmissionInfo(TransmissionType.Data, node);
                } else {
                    System.out.println("I'm broke :(");
                    return new TransmissionInfo(TransmissionType.NoData, node+1);
                }
            }
        }
        return new TransmissionInfo(TransmissionType.Silent, 0);
    }
}

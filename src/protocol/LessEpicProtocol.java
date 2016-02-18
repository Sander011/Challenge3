package protocol;

import java.util.Random;

import client.MACChallengeClient;
import sun.management.snmp.util.MibLogger;

/**
 * Created by Sander on 18/02/2016.
 */
public class LessEpicProtocol implements IMACProtocol {

	
	private static final int HEADER_LAST_PACKET = 12345;
	
	boolean[] nodesWithQueue = {false, false, false, false};
	
	private int node = 0;
	int count = -1;
	int counter = 0;
	int waitingTime = (int) Math.round(Math.random() * 4);
	State state = State.INITIAL;
	boolean tried = false;
	boolean stopWaiting = false;
	boolean isSending = false;

	int previousToken = -1;
	boolean checking = false;

	
	
	
	public int calculateNewNumber(int counter){
		if (checking) {
			int temp = counter;
			temp = (temp + 1) % 4;
			if (temp == 3){
				checking = false;
			}
			return temp;
		} else {
			int temp = counter;
			
			for (int i=0; i<4; i++) {
				temp = temp + 1 % 4;
				if (nodesWithQueue[temp]){
					return temp;
				}
			}
			
			temp = (temp + 1) % 4;
			return temp;
		}
	}
	
	
	@Override
	public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation, int localQueueLength) {
		if(state == State.INITIAL) {

			if (previousMediumState == MediumState.Succes){
				previousToken = controlInformation;
				if (tried){
					node = controlInformation;	
					state = State.WAITING;
					System.out.println("IK BEN: "+node);
					return new TransmissionInfo(TransmissionType.Silent, 0);
				}else if (controlInformation == 2) {
					node = 3;
					state = State.SENDING;
				}
			}
			tried = false;

			if (new Random().nextInt(100) < 33) {
				tried = true;
				return new TransmissionInfo(TransmissionType.NoData, previousToken + 1); 
			}
		} 

		else if(state == State.WAITING) {

			if (previousMediumState == MediumState.Succes && controlInformation == 2) {
				state = State.SENDING;
			}
			return new TransmissionInfo(TransmissionType.Silent, 0);
		} 
		else if (state == State.SENDING) {

			//Bijhouden welke nodes er aan het zenden zijn
			if (previousMediumState == MediumState.Succes) {
				int temp = counter;
				
				if (controlInformation != HEADER_LAST_PACKET) {
					nodesWithQueue[temp] = true;
				} else {
					nodesWithQueue[temp] = false;
				}
			}
			
			counter = calculateNewNumber(counter);
			if (counter == 3) {
				checking = false;
			}
			
			//Doen als er een collision is
			if (previousMediumState == MediumState.Collision) {
				counter = 0;
				checking = true;
			}
			
			//Laat merken dat je aan het senden bent.
			if (isSending == false && localQueueLength > 0) {
				isSending = true;
				return new TransmissionInfo(TransmissionType.Data, node + 1);
			}
			
			
			if (node == counter) {
				if (localQueueLength > 0){
					System.out.println("SENDING");
					return new TransmissionInfo(TransmissionType.Data, node + 1);	
				} else {
					isSending = false;
					System.out.println("NO DATA TO SEND");
					return new TransmissionInfo(TransmissionType.NoData, HEADER_LAST_PACKET);
				}
			}
			System.out.println("NOT MY TURN");
		}
		return new TransmissionInfo(TransmissionType.Silent, 0);
	}
}

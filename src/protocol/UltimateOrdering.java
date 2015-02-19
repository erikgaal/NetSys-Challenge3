package protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class UltimateOrdering implements IMACProtocol {

    public static final int CLIENTS = 4;
    public static final int BLOCKSIZE = 7;

    private int count;
    private int atTurn;
    private int sent;

    //@invariant 0 <= turn < CLIENTS
    private int turn;

    private MediumState[] states;
    private boolean initDone;

    public UltimateOrdering() {
        initDone = false;
        count = CLIENTS;
        turn = randomTurn();
        states = new MediumState[]{
                MediumState.Collision,
                MediumState.Collision,
                MediumState.Collision,
                MediumState.Collision
        };
    }

    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        if (!initDone) {
            // Phase one: Genesis

            // Add the previous state to the table.
            states[(count - 1) % CLIENTS] = previousMediumState;

            if (count % CLIENTS == 0) {
                int countCollisions = countCollisions(states);
                if (countCollisions == 0) {
                    // When there are no collisions we are done with the first phase.
                    initDone = true;
                } else if (countCollisions == 1) {
                    // When there is one collision, and this client has collided, we have to generate a new identifier.
                    if (states[turn] == MediumState.Collision) {
                        turn = randomTurn(freeSlots(states));
                    }
                } else if (countCollisions >= 2) {
                    // When there are two or more collisions, we'll give everyone a new identifier.
                    turn = randomTurn();
                }

                // Debugging and clean the state list.
                System.out.println(Arrays.toString(states));
                states = new MediumState[CLIENTS];
            }

            // If this is my turn, send a packet.
            if (count++ % CLIENTS == turn) {
                if (localQueueLength > 0) {
                    System.out.println("[INFO] Sending... turn:" + turn);
                    return new TransmissionInfo(TransmissionType.Data, 0);
                } else {
                    System.out.println("[INFO] Nothing to send... turn" + turn);
                    return new TransmissionInfo(TransmissionType.NoData, 0);
                }
            }
        } else {
            // Phase two: Exodus

            // If there is a collision, we have dun goofed, so we stop.
            if (previousMediumState == MediumState.Collision) System.exit(1);

            if (atTurn != turn && previousMediumState == MediumState.Idle || controlInformation < 1) {
                // If it is not my turn, and the previous was idle OR it was the last data of the previous client, turnplusplus.
                turnplusplus();
            }
            if (atTurn == turn) {
                if (localQueueLength > 0 && sent < BLOCKSIZE) {
                    ++sent;
                    System.out.println("[INFO] Sending... sent:" + sent + " turn:" + turn + " atturn:" + atTurn);
                    return new TransmissionInfo(TransmissionType.Data, Math.min(localQueueLength - 1, BLOCKSIZE - sent));
                }
            }
        }
        System.out.println("[INFO] Shhhh turn:" + turn + " atturn:" + atTurn);
        return new TransmissionInfo(TransmissionType.Silent, 0);
    }

    private void turnplusplus() {
        sent = 0;
        atTurn = (atTurn + 1) % CLIENTS;
    }

    private int countCollisions(MediumState[] states) {
        int result = 0;
        for (int i = 0; i < states.length; i++) {
            if (states[i] == MediumState.Collision) ++result;
        }
        return result;
    }

    private List<Integer> freeSlots(MediumState[] states) {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < states.length; i++) {
            if (states[i] == MediumState.Collision || states[i] == MediumState.Idle) result.add(i);
        }
        return result;
    }

    private int randomTurn() {
        return new Random().nextInt(CLIENTS);
    }

    private int randomTurn(List<Integer> turns) {
        return turns.get(new Random().nextInt(turns.size()));
    }
}

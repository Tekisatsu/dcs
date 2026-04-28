import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/**
 * Interface that presents a few string constants used in time agreement.
 *
 * Protocol to be used:
 *
 * Each command ends with a line break (CRLF), even if not always explicitly mentioned below.
 * The client opens a TCP connection to the server port (by default 2026).
 *
 * Starting a time agreement:
 *
 * The client sends the message "STARTAGREEMENT: agreementId timeout"
 * where agreementId is a string and timeout is the time in seconds after which
 * the agreement process is closed.
 * The server responds with 200 OK or a 5xx error code.
 *
 * Requesting ongoing agreements:
 *
 * The client sends the message "LISTAGREEMENTS:"
 * The server responds by sending a list of still ongoing agreements:
 * "LISTAGREEMENTS: id1 id2 id3 ..."
 *
 * Reporting suitable times:
 *
 * The client sends the message "MYTIMES: agreementId time1 time2 time3..."
 * where agreementId is a string and timeX is a positive integer.
 * The server responds with 200 OK or a 5xx error code.
 *
 * Making a decision:
 *
 * When the agreement timeout has expired, the server makes a decision
 * (the smallest integer that suits everyone, or -1 if no time is suitable).
 *
 * If the server still has a TCP connection with a client that has participated
 * in this agreement, the server pushes the decision via that TCP connection.
 * The server sends "DECISION: agreementId time"
 *
 * The client requests the decision with the message "DECISION: agreementId".
 * The server responds with "DECISION: agreementId time" or with a 400 error code
 * if the decision is not yet available.
 *
 */
public class AgreeProto {

    public static final String A_START = "STARTAGREEMENT";
    public static final String A_ERROR = "500 ERROR";
    public static final String A_PENDING = "400 agreement is still in progress";
    public static final String A_SUITABLE = "MYTIMES:";
    public static final String A_LISTAGREEMENTS = "LISTAGREEMENTS:";
    public static final String A_DECICION = "DECISION:";
    public static final String A_OK = "200 OK";
    public static final String A_STOP = "STOP"; // TODO not actually used
    public static final String EOL = "\r\n";    // end-of-line marker

    public static final int A_DEFAULPORT = 2026;

    // a couple of helper methods, may be used if desired
    // more hints in the AgreeServer and AgreeClientStart files

    /**
     * Convert a string to a list
     * @param timeList space-separated integers
     * @return list of integers
     */
    public static List<Integer> stringToList(String timeList) {
        Scanner sc = new Scanner(timeList);
        List<Integer> l = new ArrayList<>();
        while (sc.hasNextInt())
            l.add(sc.nextInt());
        return l;
    }

    /**
     * Convert a list of integers to a string
     * @param list input list
     * @return string representation
     */
    public static String listToString(List<Integer> list) {
        StringBuilder sb = new StringBuilder(list.size() * 4);
        for (Integer x : list) {
            sb.append(x);
            sb.append(" ");
        }
        return sb.toString();
    }

}

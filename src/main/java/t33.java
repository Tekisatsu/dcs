import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
public class t33 {
    private static String agreementId = null;
    private static int timeout = -1;

    public static void main(String[] args) {

        // Default values
        String serverAddress = "localhost";
        int port = AgreeProto.A_DEFAULPORT;

        if (args.length > 0) {
            serverAddress = args[0];
        }

        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        // Agreement id and decision timeout can also be read from the command line
        if (args.length > 2) {
            agreementId = args[2];
        }

        if (args.length > 3) {
            timeout = Integer.parseInt(args[3]);
        }

        t33 client = new t33();

        client.requestTimes(serverAddress, port);

    } // main()


    // Establish connection and start the interaction
    private boolean requestTimes(String address, int port) {

        // Establish connection
        Socket socket = null;
        try {
            socket = new Socket(address, port);     // connect
            System.out.println("Connection successful");
        } catch (Exception e) {
            // Connection most likely failed
            System.err.println("" + e);
            return false;
        }

        try {

            // Create communication channels to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // User input
            BufferedReader user = new BufferedReader(
                    new InputStreamReader(System.in));

            // Read values from the user if they were not provided on the command line
            if (agreementId == null) {
                System.out.println("Enter agreement id:");
                agreementId = user.readLine().trim();
            }

            // Send decision message to the server
            out.print(AgreeProto.A_DECICION + " " + agreementId + " " + timeout + AgreeProto.EOL);
            out.flush();

            // Read response and check result
            String response;
            boolean decicion = false;

            while (!decicion && (response = in.readLine()) != null) {
                if (response.startsWith(AgreeProto.A_DECICION)) {
                    System.out.println("Decicion: " + response);
                    decicion = true;
                }
                else if (response.startsWith(AgreeProto.A_PENDING)) {
                    System.out.println("Decision pending: " + response);
                    Thread.sleep(5000);
                } else
                    System.out.println("Failed to get decision: " + response);
            }

            socket.close();

        } catch (Exception e) {
            System.err.println("" + e);
            try {
                socket.close();  // Close again just in case
            } catch (Exception ignored) { }
            return false;
        }

        return true;

    }

}

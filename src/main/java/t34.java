import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class t34 {

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

        t34 client = new t34();
        BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

        try {
            switch (user.readLine()) {
                case AgreeProto.A_START ->
                        client.startAgreement(serverAddress, port);
                case AgreeProto.A_SUITABLE ->
                    client.addTimes(serverAddress, port);
                case AgreeProto.A_DECICION ->
                    client.requestTimes(serverAddress, port);
            }

        } catch (Exception e) {
            System.err.println("" + e);
        }

    } // main()


    // Establish connection and start the interaction
    private boolean startAgreement(String address, int port) {

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

            while (timeout <= 0) {
                System.out.println("Enter decision timeout in seconds:");
                String timeoutString = user.readLine();
                timeout = Integer.parseInt(timeoutString);
            }

            // Send start message to the server
            out.print(AgreeProto.A_START + " " + agreementId + " " + timeout + AgreeProto.EOL);
            out.flush();

            // Read response and check result
            String response = in.readLine();

            if (response.startsWith("2"))
                System.out.println("Time agreement started");
            else
                System.out.println("Failed to start time agreement: " + response);

            System.out.println("Enter Agreement id and suitable times seperated by spaces.");
            out.print(AgreeProto.A_SUITABLE + " "+ user.readLine() +" "+ AgreeProto.EOL);
            out.flush();
            System.out.println(in.readLine());
            socket.close();

        } catch (Exception e) {
            System.err.println("" + e);
            try {
                socket.close();  // Close again just in case
            } catch (Exception ignored) { }
            return false;
        }

        return true;

    }   // startAgreement()

    private boolean addTimes(String address, int port) {

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

            System.out.println("Enter Agreement id and suitable times seperated by spaces.");

            // Send suitable times to the server
            out.print(AgreeProto.A_SUITABLE+ " " + user.readLine() + " " + timeout + AgreeProto.EOL);
            out.flush();

            // Read response and check result
            String response = in.readLine();

            if (response.startsWith("2"))
                System.out.println("Times added");
            else
                System.out.println("Failed to add times: " + response);

            socket.close();

        } catch (Exception e) {
            System.err.println("" + e);
            try {
                socket.close();  // Close again just in case
            } catch (Exception ignored) { }
            return false;
        }

        return true;

    }   // startAgreement()
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

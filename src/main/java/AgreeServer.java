// AgreeServer.java
// server for tasks 32-36
// for task XX, remove comments from push decision



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

public class AgreeServer {

    private final HashSet<Connection> activeConnections = new HashSet<>();

    // Active clients (threads)
    // All agreements
    private final HashMap<String, Agreement> agreements = new HashMap<>();
    Timer timer = new Timer(true);
    // Listening to server socket
    private ServerSocket serverSocket = null;

    // Constructors open the listening socket
    private AgreeServer(int port) {

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Listening on port " + port);
        } catch (Exception e) {
            System.err.println("" + e);
            serverSocket = null;
        }
    }

    public static void main(String[] args) {

        AgreeServer server;

        if (args.length > 0)
            server = new AgreeServer(Integer.parseInt(args[0]));
        else
            server = new AgreeServer(AgreeProto.A_DEFAULPORT);

        server.waitForConnections();

    } // main()

    /**
     * Listening “thread”, lifecycle of the main program
     */
    private void waitForConnections() {

        if (serverSocket == null)
            return;

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        try {
            serverSocket.setSoTimeout(1 * 1000);

            System.out.println("Press ENTER to stop the server");

            while (true) {

                // Wait for a new connection, occasionally check user input
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                } catch (SocketTimeoutException ignored) { }

                if (userInput.ready()) {
                    stopServer();
                    return;
                }
                if (clientSocket == null) // accept() timed out
                    continue;

                // Create and start a new server thread

                Connection newConnection = new Connection(clientSocket);
                activeConnections.add(newConnection);
                newConnection.setDaemon(true);
                newConnection.start();
            }
        } catch (Exception e) {
            System.err.println("" + e);
            serverSocket = null;
        }
    }   // waitForConnections()


    /**
     * Start a new agreement.
     *
     * @param command full input line received from the client
     * @param connection connection through which the request was received
     * @return response to the client
     */
    String startAgreement(String command, Connection connection) {

        // try-catch in case Scanner encounters errors
        try {
            Scanner sc = new Scanner(command);

            sc.next(); // command
            String id = sc.next().trim();
            int timeout = sc.nextInt();

            System.out.println("startAgreement: id=" + id + " timeout=" + timeout);

            if (id.length() < 1)
                return AgreeProto.A_ERROR + " empty id";
            if (agreements.get(id) != null)
                return AgreeProto.A_ERROR + " agreement id already in use";

            // New agreement
            Agreement agreement = new Agreement(id, timeout);
            agreements.put(id, agreement);

            agreement.connections.add(connection);
            activeConnections.add(connection);
            return AgreeProto.A_OK;

        } catch (NoSuchElementException e) {
            System.out.println("startAgreement failed: " + e);
            return AgreeProto.A_ERROR;
        }
    }

    /**
     * Add times to an agreement
     *
     * @param command full input line received from the client
     * @param connection connection through which the request was received
     * @return response to the client
     */
    String addTimes(String command, Connection connection) {

        try {
            Scanner sc = new Scanner(command);

            sc.next(); // command
            String id = sc.next();

            if (id.length() < 1)
                return AgreeProto.A_ERROR + " empty agreement id";
            if (!agreements.containsKey(id))
                return AgreeProto.A_ERROR + " agreement id does not exist";

            List<Integer> times = new ArrayList<>();
            while (sc.hasNextInt())
                times.add(Integer.valueOf(sc.nextInt()));

            if (times.size() < 1)
                return AgreeProto.A_ERROR + " empty list of suitable times";

            System.out.println("addTimes: id=" + id + " times=" + times);

            Agreement agreement = agreements.get(id);
            agreement.addTimes(times);

            agreement.connections.add(connection);
            activeConnections.add(connection);

            return AgreeProto.A_OK;
        } catch (NoSuchElementException e) {
            System.out.println("addTimes failed: " + e);
            return AgreeProto.A_ERROR;
        }

    }

    /**
     * Request a decision
     *
     * @param command full input line received from the client
     * @param connection connection through which the request was received
     * @return response to the client
     */
    String getDecision(String command, Connection connection) {

        try {
            Scanner sc = new Scanner(command);

            sc.next(); // command
            String id = sc.next();

            if (id.length() < 1)
                return AgreeProto.A_ERROR + " empty agreement id";
            if (!agreements.containsKey(id))
                return AgreeProto.A_ERROR + " agreement id does not exist";

            Agreement agreement = agreements.get(id);

            System.out.println("getDecision: id=" + id + " decision=" + agreement.getDecision());
            agreement.connections.add(connection);
            return agreement.getDecision();

        } catch (NoSuchElementException e) {
            System.out.println("getDecision failed: " + e);
            return AgreeProto.A_ERROR;
        }

    }


    private void stopServer() {
        // Synchronize sending so that no new users are added during iteration
        // and everyone receives messages in the same order
        System.out.println("Sending shutdown message to all");
        synchronized (this) {
            for (Connection target : activeConnections) {
                target.sendMessage(AgreeProto.A_STOP);
            }
        }
        // Wait briefly so clients can respond
        try {
            Thread.sleep(1 * 1000);
        } catch (Exception ignored) {
        }

        for (Connection target : activeConnections) {
            target.close();
        }

    }


    // TODO not used
    private void sendMessageToAll(String message, String sender) {
        // Synchronize sending so that no new users are added during iteration
        // and everyone receives messages in the same order
        System.out.println("Message to all: " + sender + " > " + message);
        synchronized (this) {
            for (Connection target : activeConnections) {
                if (sender == null || !sender.equals(target.name))
                    target.sendMessage(AgreeProto.A_SUITABLE + " " + sender + AgreeProto.EOL + message);
            }
        }
    }

    private void removeConnection(Connection c) {
        System.out.println("Removing " + c.name);
        synchronized (this) {
            activeConnections.remove(c);
        }
    }

    private void addConnection(Connection c) {
        System.out.println("Adding " + c.name);
        synchronized (this) {
            activeConnections.add(c);
        }
    }


    private boolean isReserved(String name) {
        synchronized (activeConnections) {
            for (Connection target : activeConnections) {
                if (name.equals(target.name))
                    return true;
            }
            return false;
        }
    }


    /**
     * Class that encapsulates one time agreement (identified by agreement id)
     */
    class Agreement {

        String agreementId;
        long decisionTime;
        boolean decisionMade = false;
        int decision = -2;

        // Times suitable for everyone
        NavigableSet<Integer> suitableForAll = null;

        // Connections used in this agreement (some may have already disconnected)
        Set<Connection> connections = new HashSet<>();

        /**
         * Creates a new agreement whose decision is made after the given timeout
         *
         * @param id agreement identifier
         * @param timeout delay before making the decision, in seconds
         */
        public Agreement(String id, int timeout) {
            agreementId = id;
            decisionTime = System.currentTimeMillis() + 1000L * timeout;

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    makeDecision();
                }
            }, 1000L * timeout);
        }


        /**
         * Makes the decision by selecting from the times suitable for everyone.
         */
        synchronized void makeDecision() {
            if (suitableForAll == null || suitableForAll.isEmpty())
                decision = -1;    // no times (never existed or none left)
            else
                decision = suitableForAll.first();
            decisionMade = true;

            System.out.println("makeDecision: id=" + agreementId + " decision=" + decision);

            // Send the decision to connections related to this agreement (if
            // they are still active)
            // This does not apply to tasks 32–34.
            // When testing task 35, remove the comments below.

            for (Connection c : connections)
                if (activeConnections.contains(c))
                    c.sendDecision(decision);

        }

        /**
         * Returns the decision as a string to be sent
         *
         * @return error or time
         */
        synchronized String getDecision() {
            if (!decisionMade)
                return AgreeProto.A_PENDING;
            return AgreeProto.A_DECICION + " " + decision;
        }

        /**
         * Adds one user's suitable times to the agreement
         *
         * @param suitableTimes suitable times as a list
         */
        synchronized void addTimes(List<Integer> suitableTimes) {
            if (suitableForAll == null) {  // first contributor
                suitableForAll = new TreeSet<>(suitableTimes);
            } else  // later contributor
                suitableForAll.retainAll(suitableTimes);
        }

    } // class Agreement


    /**
     * Bundles a single time client connection and the thread handling that client
     */
    class Connection extends Thread {

        Socket client = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String name = "";
        boolean stop = false;

        Connection() {
            super();
        }

        Connection(Socket cs) {
            super();
            client = cs;
            name = client.getInetAddress() + " " + client.getPort();
        }

        /**
         * Sends the decision to one client
         *
         * @param decision chosen time
         */
        void sendDecision(int decision) {
            try {
                sendMessage(AgreeProto.A_DECICION + " " + decision);
            } catch (Exception e) {
                // TODO
            }
        }


        /**
         * Closes the client connection
         */
        void close() {

            try {
                stop = true;
                client.close();
            } catch (IOException ignored) {
            }
        }

        @Override
        public void run() {

            if (client == null || client.isClosed())
                return;

            try {

                // Handling a new client

                System.out.println("New connection: " + client.getInetAddress() +
                        ":" + client.getPort());

                // Streams in usable form
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);

                while (true) {

                    // Read line by line, identify command, call
                    // the appropriate operation

                    String command = in.readLine();
                    command = command.trim();
                    String response = "";

                    System.out.println("Received message: \"" + command + "\"");

                    if (command.startsWith(AgreeProto.A_START)) {
                        response = startAgreement(command, this);
                    } else if (command.startsWith(AgreeProto.A_SUITABLE)) {
                        response = addTimes(command, this);
                    } else if (command.startsWith(AgreeProto.A_DECICION)) {
                        response = getDecision(command, this);
                    } else
                        response = AgreeProto.A_ERROR;

                    // Send the response corresponding to the operation

                    sendMessage(response);

                }

                // TODO does not exit very gracefully
            } catch (Exception e) {
                // System.err.println("Connection.run: " + e);
            } finally {
                close();
                removeConnection(this);
            }

        }   // run()


        // Sends a prepared message
        // Used both by this thread and others (sendMessageToAll)
        synchronized void sendMessage(String message) {
            System.out.println("Sending message: \"" + message + "\"");
            out.print(message.trim());
            out.print(AgreeProto.EOL);
            out.flush();
        }

    } // class Connection

}   // class AgreeServer

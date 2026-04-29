import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class t37_38 {
    public static void main (String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/", new FileHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Server started: "+ server.getAddress());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] response = Files.readAllBytes(Paths.get("src/ANTIPLUG.html"));
            exchange.sendResponseHeaders(200,response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }
}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

//TODO: Review later. Worked all day & am tired.
public class JavaClient {

    /* Socket of the client */

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;

    public JavaClient(String host, int portNumber) throws UnknownHostException, IOException {

        /* Try to establish connection to the python client */

        socket = new Socket(host, portNumber);

        /* Instantiate writers, readers to the socket */

        input = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        output = new PrintWriter(
                socket.getOutputStream(), true);


        /* Send username data to the server */

        this.username = username;
        output.println(username);


        public Message sendMessage(String content) {
            output.println(content);
        }


        public static void main(String[]args) throws IOException {
            JavaClient client = new JavaClient("localhost", 1024);

            // Read input from console and send messages to server
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                try {
                    String message = input.readLine();
                    client.sendMessage(content);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}

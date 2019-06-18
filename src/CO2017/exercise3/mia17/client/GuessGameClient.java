/**
 * GuessGameClient
 *
 * Class controlling the client side of the Guessing Game system.
 * 
 * @author mia17 179025920
 */

package CO2017.exercise3.mia17.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class GuessGameClient extends java.lang.Object {

   public GuessGameClient() {}

   public static void main(java.lang.String[] args) throws IOException {
      String hostname = args[0];
      int port = Integer.parseInt(args[1]);

      try (Socket server = new Socket(hostname, port)) {
         String response;     //Server response
         String maxVal;       //Max value
         Float remTime;       //Time remaining

         // Set up input and output streams connected to the server socket
         BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream(), "UTF-8"));
         PrintWriter out = new PrintWriter(server.getOutputStream(), true);

         // Input stream from the user's terminal
         //BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

         // Show connected client & Read START message
         System.out.println("Connected to " + server.getInetAddress());
         response = in.readLine();

         // Position of : in response
         int sepIndex = response.indexOf(':');

         // Breakdown START message
         response = response.substring(sepIndex + 1);

         // Get max value
         sepIndex = response.indexOf(':');
         maxVal = response.substring(0, sepIndex);

         // Get Time limit
         sepIndex = response.indexOf(':');
         remTime = Float.parseFloat(response.substring(sepIndex + 1));

         System.out.printf("New guessing game. Range is 1..%s. Time limit is %.1fs \n \n",
                 maxVal, (remTime / 1000));

         ClientState cs = new ClientState(out, Long.parseLong(response.substring(sepIndex + 1)));

         // Create ClientState thread
         Thread clientState = new Thread(cs);
         clientState.start();

         do {
            // Read response from server
            response = in.readLine();

            // Print client-side message
            try {
               cs.userPrint(cs.isFinished(), response);
            } catch (TimeoutException e) {
               e.printStackTrace();
            }
         } while (cs.isFinished() == false);
         // Game over
         System.out.println("Game over!");
         server.close();
      } catch (UnknownHostException e) {
         System.err.println("Unknown host: " + hostname);
         System.err.println(e);
         System.exit(1);
      }
   } // Main end

}
// GuessGameClient

/**
 * GuessGameServer
 *
 * Class controlling the server side of the Guessing Game system.
 * 
 * @author mia17 179025920
 */

package CO2017.exercise3.mia17.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GuessGameServer extends Object {

   public GuessGameServer() {}

   public static void main(String[] args) throws IOException {
      int clientId = 65;                             //ClientId char starting at A
      int port = Integer.parseInt(args[0]);          //Port number
      int mv = Integer.parseInt(args[1]);            //Max value
      long tl = Long.parseLong(args[2]) * 1000;      //Time limit

      try (ServerSocket server = new ServerSocket(port)) {
         // Server-side initial start message
         System.out.printf("Starting GuessGame Server (%d, %d) on port %d \n", mv, tl, port);

         // Listen on port until manually closed
         while (true) {
            //System.out.println("Waiting for client..." + "\n");

            // Create connection
            Socket client = server.accept();
            GuessGameServerHandler ggsh = new GuessGameServerHandler(clientId, mv, tl, client);

            // Create thread to handle connection
            Thread guessGame = new Thread(ggsh);
            guessGame.start();

            // Increment clientId
            clientId++;
         }
      } catch (IOException e) {
         System.err.println(e);
      }
   }// Main end
}
// GuessGameServer

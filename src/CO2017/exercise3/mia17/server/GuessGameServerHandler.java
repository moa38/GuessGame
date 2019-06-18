/**
 * GuessGameServerHandler
 *
 * Class to handle server side of a single instance of the Guessing Game in its own thread.
 * Responsible for interactions with the client.
 * 
 * @author mia17 179025920
 */

package CO2017.exercise3.mia17.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

public class GuessGameServerHandler extends java.lang.Object implements java.lang.Runnable{

   private BufferedReader in;                //Server input stream
   private PrintWriter out;                  //Server output stream
   private int mv;                           //Max value
   private long tl;                          //Time limit
   private Socket client;                    //Client socket
   private GameState gs;                     //GameState object
   private int clientId;                     //Client Id
   boolean finished = false;

   public GuessGameServerHandler (int i, int mv, long tl, Socket cl) {
      this.clientId = i;
      this.mv = mv;
      this.tl = tl;
      this.client = cl;

      try {
         // Set up input and output streams connected to the client socket
         in = new BufferedReader(new InputStreamReader(client.getInputStream(),"UTF-8"));
         out = new PrintWriter(client.getOutputStream(), true);
      }
      catch (UnsupportedEncodingException e) {}
      catch (IOException e) {
         System.err.println("Unable to create input/output streams" );
         System.err.println(e);
      }
      finally {
         gs = new GameState(mv, tl, this);
         System.out.println((char)clientId + " ServerHandler is connected");
      }
   }

   public void run() {
      try{
         // Send the initial "START" message to the client
         out.printf("START:%d:%d%n", mv, tl);

         //Start GameState thread
         Thread gameState = new Thread(gs);
         gameState.start();

         // Get and display client's IP address
         InetAddress clientAddress = client.getInetAddress();
         System.out.println((char)clientId + " connection : " + clientAddress);
         System.out.println((char)clientId + " start watching");

         // Print out a server-side message indicating the target number
         System.out.println((char)clientId + " target is " + gs.target);

         String response;       //Response from client
         int guess = 0;         //Player guess
         String state;          //Game State message 

         // Repeatedly (until the game is over) read in guesses from the client socket
         do {
            try {
               // Read guess from client
               response = in.readLine();

               guess = Integer.parseInt(response);

               // Guess is within range
               if (guess >= GameState.MINVAL && guess <= mv) {
                  // Submit guess to GameState
                  gs.guess(guess);

                  // Get response from GameState
                  state = gs.toString();

                  // Print server-side GameState
                  log(state, guess);

                  // Send response to client
                  send(state);

                  // Is game over?
                  if(gs.finished() == true) guess = 999;
               }
               else {
                  // Guess is outside range
                  if (guess < 1 || guess > mv) {
                     // Print server-side
                     state = "ERR out of range";
                     log(state, guess);

                     // Send response to client
                     state = "ERR";
                     send(state);
                  }
               }
            } // End of turn
            // Guess is non-integer
            catch (NumberFormatException | NullPointerException ex) {
               if (gs.finished() == true) {
                  state = gs.toString();
                  // Print server-side
                  System.err.println(String.format((char)clientId + " -" + " (" + state +") - %.1fs/%d",
                        ((float)(gs.getTimeRemaining())/1000), gs.getGuesses()));
                  
                  // Send response to client
                  send(state);
                  break;
               }

               // Print server-side
               System.err.println(String.format((char)clientId + " **" + " (ERR non-integer) - %.1fs/%d",
                     ((float)(gs.getTimeRemaining())/1000), gs.getGuesses()));

               // Send response to client
               state = "ERR";
               send(state);
            } catch (IOException e) { e.printStackTrace(); }
            // End of DO loop
         } while (gs.finished() == false); 
      } finally {
         // Print game end over message
         System.out.printf((char)clientId + " Game over" + "\n" + "\n");

         // Close client connection
         try {
            client.close();
         } catch (IOException e) { e.printStackTrace(); }
      }
   }

   // Function that prints server-side messages
   void log(String msg, int guess) throws java.io.IOException{
      System.out.println(String.format((char)clientId + " " + guess + " (" + msg + ") - %.1fs/%d",
            ((float)(gs.getTimeRemaining())/1000), gs.getGuesses()));
   }

   // Function that sends server-client messages
   void send(String msg) {
      // Send (WIN, LOSE)
      if(msg.equals("WIN") || msg.equals("LOSE")) {
         out.println(String.format(msg + ":%d:%d", gs.getGuesses(), gs.getTarget()));
      }
      // Send (HIGH, LOW, ERR)
      else {
         out.println(String.format(msg + ":%d:%d", (gs.getTimeRemaining()), gs.getGuesses()));
      }   
   }

   // Function that closes Client Socket Input
   void shutdownInput() throws java.io.IOException{
      //Close client input
      client.shutdownInput();
   }

   //
   int getClientId() {
      return clientId;
   }

   public static boolean isNumeric(String strNum) {
      return strNum.matches("-?\\d+(\\.\\d+)?");
   }

}
// GuessGameServerHandler

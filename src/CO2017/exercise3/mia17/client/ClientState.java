/**
 * ClientState
 *
 * Class representing the state of the an instance of the guessing game on the client side
 * in its own thread.
 * Responsible for handling local user interaction.
 * 
 * @author mia17 179025920
 */

package CO2017.exercise3.mia17.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

public class ClientState extends Object implements Runnable{

   static final BufferedReader _tty = new BufferedReader (new InputStreamReader (System.in));
   PrintWriter out;                 //Output stream to server
   volatile boolean _finished;
   private String lastInput;        //Most recent user input
   String state;                    //Game state part of response
   private String turns;            //Turns played
   private String target;           //Target value
   private long tl;                 //Time Limit
   long targetTl;                   //Game end time
   Float remTime;                   //Time remaining
   GuessGameClient ggcl;            //GuessGameClient object

   public ClientState(PrintWriter o, long timeLimit) {
      this.out = o;
      this.tl = timeLimit;
   }

   // Method providing an interruptible input reader
   String readLineTimeout(BufferedReader reader, long timeout) throws TimeoutException, IOException {
      long starttime = System.currentTimeMillis();

      while (!reader.ready()) {
         if (System.currentTimeMillis() - starttime >= timeout)
            throw new TimeoutException();

         // short delay between polling the buffer
         try { Thread.sleep(50); } catch (Exception ignore) {}
      }

      return reader.readLine(); // won't block since reader is ready
   }

   // Thread to keep track of time limit & interact with user
   @Override
   public void run() {
      try {
         // Time when game will end
         targetTl = System.currentTimeMillis() + tl;
         System.out.print("Enter new guess: ");

         while (_finished == false) {
            //Read in guess
            lastInput = readLineTimeout(_tty, targetTl);

            // Send output to the server
            out.println(String.format("%s", lastInput));
         }
         System.out.println("Client shutdown.");
      } catch (IOException | TimeoutException e) { e.printStackTrace(); } 
   }

   // Function that prints client-side messages
   void userPrint(boolean end, String msg) throws TimeoutException, IOException {
      // Position of : in response
      int separatorIndex = msg.indexOf(':');

      // Game has ended (WIN | LOSE)
      if (msg.charAt(0) == 'W' || (msg.substring(0, separatorIndex).equals("LOSE"))) {
         // Get string part of response
         state = msg.substring(0, separatorIndex);

         if (state.equals("LOSE")) System.out.println("\n");

         // Drop string part of response
         msg = msg.substring(separatorIndex + 1);
         separatorIndex = msg.indexOf(':');

         // Get turns played
         turns = msg.substring(0, separatorIndex);

         // Get target value
         target = msg.substring(separatorIndex + 1);

         // Print client-side message
         System.out.printf("Turn %s: target was %s - %s \n", turns, target, state);

         _finished = true;
      }

      // Game is ongoing (HIGH, LOW, ERR)
      else {
         // Get string part of response
         state = msg.substring(0, separatorIndex);

         // Drop string part of response
         msg = msg.substring(separatorIndex + 1);

         // Get remaining time
         separatorIndex = msg.indexOf(':');
         remTime = Float.parseFloat(msg.substring(0, separatorIndex));

         // Get turns
         turns = msg.substring(separatorIndex + 1);

         // ERR
         if (state.equals("ERR")) {
            // Print message
            System.err.printf("ERROR: Turn %s: %.1fs remaining \n", turns, (remTime)/1000);
         }
         // HIGH | LOW
         else {
            // Print message
            System.out.printf("Turn %s: %s was %s, %.1fs remaining \n", turns, getLastInput(), state, (remTime)/1000);
         }
         try {
            // Thread sleep for 200ms (0.2s)
            Thread.sleep(20);
         } catch (InterruptedException e) { e.printStackTrace(); }

         // Prompt for next guess
         System.out.print("Enter new guess: ");
      }
   }

   // Accessor for _finished
   boolean isFinished() {
      //Game is over
      if (_finished == true) return true;
      //Game is ongoing
      return false;
   }

   // Accessor for console input (lastInput)
   String getLastInput() {
      // No input made
      if (this.lastInput.isEmpty()) return null;
      return this.lastInput;
   }
}
//ClientState

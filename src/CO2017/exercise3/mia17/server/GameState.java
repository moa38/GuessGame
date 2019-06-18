/**
 * GameState
 *
 * Class representing the state of the an instance of the Guessing Game on the server side
 * in its own thread.
 * Responsible for timer Guessing Game timer.
 * 
 * @author mia17 179025920
 */

package CO2017.exercise3.mia17.server;

import java.io.IOException;
import java.util.Random;

public class GameState extends Object implements Runnable{

   private static final Random RANDGEN = new Random();

   GuessGameServerHandler ggsh;     //GuessGameServerHandler object
   static final int MINVAL = 1;		//Min value allowed
   static int maxval;		 	      //Max value allowed
   int guess;			               //Guess made between 1..maxval
   int guesses = 0;	               //Number of guesses made
   int target;                      //Target value
   long tl;			                  //Time limit in milliseconds
   long remTime;                    //Remaining time in milliseconds
   String state = "";               //String state of the game
   boolean finished = false;        //Boolean state of game
   long TARGETTL;

   public GameState(int mv, long tl, GuessGameServerHandler ggsh) {
      GameState.maxval = mv;
      this.tl = tl;
      this.ggsh = ggsh;
      this.target = RANDGEN.nextInt((maxval - MINVAL) + 1) + MINVAL;
   }

   boolean finished() {
      // Game is ongoing
      if (this.state.equals("HIGH") || this.state.equals("LOW") || this.state.equals("ERR")) finished = false;
      // Game is over
      if (this.state.equals("WIN") || this.state.equals("LOSE")) finished = true;
      return finished;
   }

   // Accessor for Total Guesses Made
   int getGuesses() {
      return guesses;
   }

   // Accessor for Target Value
   int getTarget() {
      return target;
   }

   // Accessor for Remaining Time
   long getTimeRemaining() {
      // Calculate remaining time 
      remTime = TARGETTL - System.currentTimeMillis();
      if(remTime < 0.0) return 0;
      return remTime;
   }

   // Function processing Player Guess
   void guess(int guess) {
      // Increase guesses counter
      this.guesses++;

      // Guess is Low
      if(guess < target && guess > 0) {
         state = "LOW";
      }
      // Guess is High
      else if((guess > target) && (guess < GameState.maxval)) {
         state = "HIGH";
      }
      // Guess is Correct
      else if(guess == target) {
         state = "WIN";
      }
   }

   // Thread to keep track of time limit
   @Override
   public void run() {
      try {
         // Time when game will end
         TARGETTL = System.currentTimeMillis() + tl;

         while (!(finished == true)) {
            // Time limit has been exceeded
            if (System.currentTimeMillis() >= TARGETTL) {
               state = "LOSE";
               finished = true;

               // Send response to client
               ggsh.send(state);
               
               try {
                  // Close client
                  ggsh.shutdownInput();
               } catch (IOException e) {e.printStackTrace(); }
               break;
            }
            // Thread sleep for 200ms (0.2s)
            Thread.sleep(200);
         }
      } catch (InterruptedException e) {e.printStackTrace(); }
   }

   // Accessor for state of game
   public String toString(){
      if (!(state.isEmpty())) return state;
      return "";
   }

}
// GameState
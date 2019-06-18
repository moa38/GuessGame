The theme of the exercise is to produce a simple client/server timed number guessing game. The server will:

1. Start using command line arguments for:
      > port to listen on;
      > maximum number to use in the guessing game (for example, a value of 143 means that the numbers to guess will be between 1 and 143);
      > time limit for the guessing game (in seconds).

2. Accept connections from clients (game players); recieve guesses from the clients (players) and respond suitably; stop the game if the 
   client (player) guesses correctly, or if they run out of time.

3. All timing is carried out on the server side.

Meanwhile, the client program has to interact with both the server and the player of the game:

1. Start using command line arguments for:
      > hostname of the server;
      > port to connect to on the server.

2. Connect to the server on the specified host and port and play the guessing game. Read guesses made by the player on the terminal, 
   and show the responses made by the server.

3. Note that the client does not need to keep track of any details of the game.
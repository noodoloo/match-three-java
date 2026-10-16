README

Gameplay:
- This game is played like most other 3-match games. You click on a gem, and then select an adjacent gem in any of the 4 cardinal directions (North, South, East, West). If the gem you are swapping with makes a match, those gems will disappear giving you points, multiplier, or more moves depending on the gem type. Then any gems above will fall down to replace the gems that matched, and then more gems will fall from the top to fill that space. Repeat until you get enough points to move to the next level. Continue repeating until you reach the final level and either win or lose. You also lose multiplier each time you progress a level to make it a little harder, 1.00 off your multiplier that you had when you finished the previous level. Either way, you can play again by clicking the "Start Game" button to reset everything.

- The scoring is as follows:
A 3-of-a-kind match of any gem that isn't a diamond or emerald gives 150 base points + 50 per extra gem in a row. If you get any "combos", it will add 80 points to that base + 80 more per combo. The sum of the base points plus combo points then get multiplied by the multiplier. For example, if you match 3 gems with a 1.25x multiplier and it cascaded 2 times, that's 150 + 160 = 310 * 1.25 = 388 points. 

There are also special gems, diamonds (triangles) and emeralds (ovals). A match of 3 diamonds gives 0.25 mult for each match + 0.15 for each extra. A match of 3 emeralds gives 1 move for each match + 1 for each extra. 

Description of Program Internals:
- GemMatch.java:
Is the entry point for the jar file, and only has the main method. 
- GemMatchGUI.java:
Mostly things GUI related goes into here, the making of the JPanel, drawing, animation, mouse clicking, etc. Also has a game logic method called processMatches where it processes each match and depending on the gem type does any scoring necessary. The method also updates the UI in the GUI such as the score, moves remaining, and multiplier. And it checks for win or lose. This class also holds the method to play a sound whenever a match happens. The class also holds a lot of bookkeeping variables because I felt it was easier to keep them in the same class as the methods that used them, since I didn't know if I would need them or not when making GemManager.
- GemManager.java
This class holds most of the internal logic for the game to function. It has the fillBoard method which fills the board with random gems each time and also makes sure there are no matches already, the findMatches method which checks for any horizontal and vertical matches, marks them, then adds the coords of the gems into an int array which is part of a List. The class also has the swapGems and removeGems methods which swaps gems if they are able to be swapped, removes the gems, then fills the empty spots with the gems above/new gems at the top. It also has the toString method and a helper method for checking if a certain spot is valid or not. 
- GemTypes.java
A class that holds the Enum for the gem types 

Extras:
- I think all of the extra things were mentioned already, but I added two special gem types (diamond for multiplier and emerald for moves) and a jingle whenever a match happens. I also added levels as well to make it more challenging plus the nerf to your multiplier. 

Known Bugs:
- There might be a bug that happens when a cascade happens. If a 3 match falls onto another 3 match that forms an "L" shape, the two top most gems of the ones that fell stay. I'm not sure if this is a bug or the animation being kind of instant and 2 of that specific gem were just randomly generated as well to fill that space. 
- There is also a bug that happens with when you progress to the next level. Sometimes the multiplier instead of showing a clean number, will sometimes show a number with multiple repeating numbers. I'm assuming it has something to do with the floater point math for the nerfing of the multiplier being a little wonky. 
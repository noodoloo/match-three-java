/**
 * A class that holds the gem types in an enum
 * @author Brandon Pham
 * For CS251
 */

public class GemTypes {

    public enum Gems {

        JADE('+'),
        AMETHYST('.'),
        STAR('*'),
        RUBY('@'),
        DIAMOND('v'), // The special multiplier gem
        EMERALD('='); // The gem that adds more moves 

        public final char gem;

        Gems(char gem) {
            this.gem = gem;
        }

        public static final Gems[] ORIGINAL = Gems.values();

    }
}

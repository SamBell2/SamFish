package chess;

import java.util.Arrays;

public class Main {
  static String[] columnLetters = {"a", "b", "c", "d", "e", "f", "g", "h"};

  public static void main(String[] args) {
    boolean debug = Arrays.asList(args).contains("debug");
    boolean UCI = Arrays.asList(args).contains("UCI");
    boolean compress = Arrays.asList(args).contains("compress");
    boolean fast = Arrays.asList(args).contains("fast");
    String outFile = args[args.length - 1];
    Bot bot = new Bot(debug, UCI, outFile, compress, fast);
    bot.run();
    /* Board b = new Board(true);
    b.getFEN("8/8/8/8/8/8/8/kQK5");
    System.out.println(b);
    System.out.println(b.whiteWon(false, true)); */
  }
}

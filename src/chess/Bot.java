package chess;

import chess.eval.Eval;
import chess.frontend.*;
import chess.logging.Logger;
import java.util.Scanner;

public class Bot {
  Board board;
  Eval evaluator;
  Frontend frontend;
  boolean whitesTurn;
  Scanner scanner = new Scanner(System.in);
  boolean debug;
  String input;
  boolean useFormatting;
  public static Logger logger;
  String loggerOutFile;
  boolean useUCI;
  public String lastMove;
  boolean fast;

  public Bot(boolean debugOn, boolean UCI, String outFile, boolean compress, boolean fast) {
    this.fast = fast;
    logger = new Logger(outFile, debugOn, compress);
    loggerOutFile = outFile;
    useFormatting = !UCI;
    useUCI = UCI;
    evaluator = new Eval(this);
    if (UCI) {
      frontend = new UCI();
      board = new Board(false);
    } else {
      board = new Board(true);
      board.getFEN("8/8/8/8/8/3k4/8/BRK1r3");
      frontend = new Playable(this);
    }
    whitesTurn = true;
    debug = debugOn;
  }

  public void move(String move) {
    board.move(move);
    whitesTurn = !whitesTurn;
  }

  public void reset(boolean newLogger) {
    if (newLogger) logger.newLog();
    board = new Board(useFormatting);
    whitesTurn = true;
  }

  public void run() {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  logger.info("Shutdown detected.");
                  logger.close(); // your compression method
                }));
    // System.err.println("can output");
    while (scanner.hasNextLine()) {
      System.out.println();
      input = scanner.nextLine();
      logger.input(input);
      if (input.equals("quit") || input.equals("exit")) {
        logger.info(board.newBoardWithmove(lastMove).toString());
        if (lastMove != null) logger.info(lastMove);
        logger.info(
            Float.toString(evaluator.evaluate(board.newBoardWithmove(lastMove), whitesTurn)));
        logger.info(Integer.toString(board.newBoardWithmove(lastMove).whiteWon(!whitesTurn, true)));
        logger.close();
        break;
      }
      if (frontend.run(board, evaluator, whitesTurn, this, input, debug, this.fast)) {
        break;
      }
    }
  }

  public void loadFen(String position, boolean newWhitesTurn) {
    board.getFEN(position);
    whitesTurn = newWhitesTurn;
  }

  public boolean stopSearch() {
    return frontend.stopSearch();
  }
}

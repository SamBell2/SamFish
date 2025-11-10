package chess.frontend;

import chess.Board;
import chess.Bot;
import chess.eval.Eval;

public class Playable implements Frontend {
  String input;
  int depth;
  boolean moved;
  String move;

  public Playable(Bot bot) {
    Bot.logger.info("Playable frontend initialised.");
    System.out.print(" > ");
    //bot.move("a1a1");
  }

  public boolean run(
      Board board,
      Eval evaluator,
      boolean whitesTurn,
      Bot bot,
      String move,
      boolean debug,
      boolean fast) { // returns true for exit, false for continue
    moved = false;
    bot.move(move);
    move = evaluator.findMove(board, !whitesTurn, 2, null, bot, "/home/arco/syzygy", fast);
    bot.move(move);
    Bot.logger.output(move);

    Bot.logger.output(board.toString());

    if (board.whiteWon(whitesTurn, false) == 0) {
      Bot.logger.output("White won!");
      return true;
    } else if (board.whiteWon(whitesTurn, false) == 1) {
      Bot.logger.output("Black won!");
      return true;
    } else if (board.whiteWon(whitesTurn, false) == -2) {
      Bot.logger.output("Draw!");
      return true;
    }
    System.out.print(" > ");
    return false;
  }

  public boolean stopSearch() {
    return false;
  }
}

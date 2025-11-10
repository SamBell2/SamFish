package chess.frontend;

import chess.*;
import chess.eval.Eval;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UCI implements Frontend {
  String input;
  String[] parts;
  String move;
  Thread searchThread;
  volatile boolean stopSearch;
  String syzygyPath;
  Map<String, Object> options;

  public UCI() {
    syzygyPath = "/home/arco/syzygy";
    options = new HashMap<String, Object>();
  }

  public boolean run(
      Board board,
      Eval evaluator,
      boolean whitesTurn,
      Bot bot,
      String input,
      boolean debug,
      boolean fast) { // returns true for exit, false for continue
    parts = input.split(" ");
    if (parts[0].equals("uci")) {
      Bot.logger.output("id name SamFish");
      Bot.logger.output("id author Sam");
      Bot.logger.output("option name Debug Log File type string default <empty>\noption name NumaPolicy type string default auto\noption name Threads type spin default 1 min 1 max 1024\noption name Hash type spin default 16 min 1 max 33554432\noption name Clear Hash type button\noption name Ponder type check default false\noption name MultiPV type spin default 1 min 1 max 256\noption name Skill Level type spin default 20 min 0 max 20\noption name Move Overhead type spin default 10 min 0 max 5000\noption name nodestime type spin default 0 min 0 max 10000\noption name UCI_Chess960 type check default false\noption name UCI_LimitStrength type check default false\noption name UCI_Elo type spin default 1320 min 1320 max 3190\noption name UCI_ShowWDL type check default false\noption name SyzygyPath type string default <empty>\noption name SyzygyProbeDepth type spin default 1 min 1 max 100\noption name Syzygy50MoveRule type check default true\noption name SyzygyProbeLimit type spin default 7 min 0 max 7\noption name EvalFile type string default nn-1c0000000000.nnue\noption name EvalFileSmall type string default nn-37f18f62d772.nnue");
      Bot.logger.output("uciok");
      /* for (String option : options.keySet()) {
        Bot.logger.output()
      } */
    } else if (parts[0].equals("isready")) {
      if ((searchThread == null || !searchThread.isAlive())) Bot.logger.output("readyok");
    } else if (parts[0].equals("ucinewgame")) {
      // logger.debug(board.toString());
      Bot.logger.debug(board.genFEN());
      Bot.logger.debug(board.kingSquare(whitesTurn));
      String kingPos = board.kingSquare(whitesTurn);
      try {
        for (String nextMove : board.newBoardWithmove(move).nextPositions(!whitesTurn, false)) {
          if (kingPos == null) {
            Bot.logger.debug(kingPos);
            Bot.logger.debug(board.toString());
          }
          if (nextMove != null && kingPos != null) {
            if (nextMove.charAt(2) == kingPos.charAt(0)
                && nextMove.charAt(3) == kingPos.charAt(1)) {
              Bot.logger.debug("king");
              Bot.logger.debug(nextMove);
            }
          }
        }
      } catch (Exception e) {
        Bot.logger.warning("Got error " + e.toString() + " while trying to find move to take king.");
      }
      Bot.logger.debug(board.genFEN());
      bot.reset(true);
    } else if (parts[0].equals("go")) {
      stopSearch = false;
      searchThread =
          new Thread(
              () -> {
                String bestmove = "";
                if (parts[1].equals("movetime")) {
                  bestmove =
                      evaluator.findMove(board, whitesTurn, null, Integer.parseInt(parts[2]), bot, syzygyPath, fast);
                } else if (parts[1].equals("wtime")) {
                  if (parts[5].equals("winc")) {
                    if (whitesTurn) {
                      bestmove =
                          evaluator.findMove(
                              board,
                              whitesTurn,
                              null,
                              Integer.parseInt(parts[6]),
                              bot, syzygyPath,
                              fast);
                    } else {
                      bestmove =
                          evaluator.findMove(
                              board,
                              whitesTurn,
                              null,
                              Integer.parseInt(parts[8]),
                              bot, syzygyPath,
                              fast);
                    }
                  } else {
                    if (whitesTurn) {
                      bestmove =
                          evaluator.findMove(
                              board,
                              whitesTurn,
                              null,
                              Integer.parseInt(parts[2]) / Integer.parseInt(parts[6]) - 10,
                              bot, syzygyPath,
                              fast);
                    } else {
                      bestmove =
                          evaluator.findMove(
                              board,
                              whitesTurn,
                              null,
                              Integer.parseInt(parts[4]) / Integer.parseInt(parts[6]) - 10,
                              bot, syzygyPath,
                              fast);
                    }
                  }
                } else if (parts[1].equals("depth")) {
                  // System.out.println(board);
                  bestmove =
                      evaluator.findMove(board, whitesTurn, Integer.parseInt(parts[2]), null, bot, syzygyPath, fast);
                }
                Bot.logger.output("bestmove " + bestmove);
              });
      searchThread.start();
    } else if (parts[0].equals("position")) {
      bot.reset(false);
      if (parts.length > 2 && parts[1].equals("startpos")) {
        String[] moves = Arrays.copyOfRange(parts, 3, parts.length);
        for (String move : moves) {
          bot.move(move);
        }
      } else if (parts.length > 2) {
        String fen = parts[2];
        boolean newWhitesTurn = parts[3].equals("w");
        bot.loadFen(fen, newWhitesTurn);
      }
      Bot.logger.info(Integer.toString(parts.length));
      if (parts.length > 9 && parts[8].equals("moves")) {
        Bot.logger.debug("fen & moves");
        String[] moves = Arrays.copyOfRange(parts, 9, parts.length);
        for (String move : moves) {
          bot.move(move);
        }
      }
      Bot.logger.debug(board.genFEN());
    } else if (parts[0].equals("print")) {
      Bot.logger.output(board.toString());
      // logger.output(board.genFEN());
    } else if (parts[0].equals("stop")) {
      Bot.logger.info("stopped");
      stopSearch = true;
      if (searchThread != null && searchThread.isAlive()) {
        try {
          searchThread.join();
        } catch (InterruptedException e) {
          Bot.logger.fatal(e.getMessage());
        }
      }
    } else if (parts[0].equals("setoption")) {
      if (parts[2].equals("SyzygyPath")) {
        syzygyPath = parts[4];
      }
    }
    System.out.println("");
    System.out.flush();
    return false;
  }

  public boolean stopSearch() {
    return stopSearch;
  }
}
/*
 * Commands to support:
 * uci DONE
 * isready DONE
 * setoption NOT SUPPORTED
 * ucinewgame DONE
 * position DONE
 * go PARTIAL (depth, wtime btime movestogo, movetime, not yet winc binc etc., nodes, inf)
 * stop NOT YET
 * ponderhit NOT SUPPORTED
 * quit DONE
 */

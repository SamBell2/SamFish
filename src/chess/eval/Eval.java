package chess.eval;

import chess.Board;
import chess.Bot;
import chess.pieces.*;
import chess.syzygy.Syzygy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Eval {
  class BoardWithEval {
    Board board;
    float eval;
  }
  Bot bot;
  Syzygy syzygyCalculator = new Syzygy();

  public Eval(Bot bot) {
    this.bot = bot;
  }

  public float evaluate(Board board, boolean white /*boolean showReasons */) {
    float points = 0;
    int whiteWon = board.whiteWon(!white, false);
    if (white && whiteWon == 0) points += 50000;
    if (!white && whiteWon == 1) points += 50000;
    if (white && whiteWon == 1) points -= 50000;
    if (!white && whiteWon == 0) points -= 50000;
    if (board.whiteWon(white, false) == -2) points -= 20;
    if (board.whiteWon(white, false) == -3) points -= 20;
    if (whiteWon == -2) points -= 20;
    if (whiteWon == -3) points -= 20;
    if (points != 0) return points;
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        Piece piece = board.getPiece(new int[] {i, j});
        if (piece != null) {
          if (piece.isWhite() == white) { // Get piece points
            points += piece.value() * 2;
            if (piece.value() == 1) { // Push pawns
              if (white) {
                int rank = (int) piece.getSquare().charAt(1) - '0';
                points += rank;
              } else {
                int rank = (int) piece.getSquare().charAt(1) - '0';
                rank = 8 - rank;
                points += rank;
              }
            } else { // Take pieces off starting squares
              if (white) {
                if (piece.getSquare().charAt(1) != '1') {
                  points += 2;
                }
              } else {
                if (piece.getSquare().charAt(1) != '8') {
                  points += 2;
                }
              }
            }
            if (((i == 3) || (i == 4)) && ((j == 3) || (j == 4))) { // Check for centre pieces
              points += piece.value() / 2 + 1;
            }
          } else { // Lower poitns for opponent pieces
            points -= piece.value() * 2;
          }
        }
      }
    }
    return points;
  }

  public String pickMove(Board board, String[] moves, boolean white) {
    float[] points = new float[moves.length];
    int maxIndex = 0;
    float currentMax = -10000000;
    for (int i = 0; i < moves.length; i++) {
      points[i] = evaluate(board.newBoardWithmove(moves[i]), white);
      if (points[i] > currentMax) {
        maxIndex = i;
        currentMax = points[i];
      }
    }
    return moves[maxIndex];
  }

  public ArrayList<HashMap<String, BoardWithEval>> findPositions(
      Board board, boolean white, Integer depth, Integer time, Long finTime, String firstMove) {
    long millis = System.currentTimeMillis();
    ArrayList<HashMap<String, BoardWithEval>> positions = new ArrayList<HashMap<String, BoardWithEval>>();
    String[] nextMoves = board.nextPositions(white, false);
    String[] bestMoves = new String[5];
    float[] bestPoints = new float[] {-100000, -100000, -100000, -100000, -100000};
    ArrayList<String> skipped = new ArrayList<String>();
    for (String move : nextMoves) {
      if (bot.stopSearch()) {
        Bot.logger.info("stopping");
        continue;
      }
      Board pos = board.newBoardWithmove(move);
      long timeElapsed = System.currentTimeMillis() - millis;
        if (time != null && ((time - timeElapsed <= 10) || System.currentTimeMillis() >= finTime)) {
          HashMap<String, BoardWithEval> toAdd = new HashMap<String, BoardWithEval>();
          BoardWithEval p = new BoardWithEval();
          p.board = pos; p.eval = evaluate(board, white);
          toAdd.put(firstMove, p);
          positions.add(toAdd);
          continue;
        }
      String kingPos = board.kingSquare(white);
      boolean stop = false;
      for (String nextMove : pos.nextPositions(!white, false)) {
        if (kingPos == null) {
          Bot.logger.warning(kingPos);
          Bot.logger.warning(board.toString());
        }
        if (nextMove != null && kingPos != null) {
          if (nextMove.charAt(2) == kingPos.charAt(0) && nextMove.charAt(3) == kingPos.charAt(1)) {
            stop = true;
            skipped.add(move);
            break;
          }
        }
      }
      if (stop) {
        continue;
      }
      if (move == null) continue;
      float eval = evaluate(pos, white);
      if (eval > bestPoints[0]) {
        bestMoves[4] = bestMoves[3];
        bestMoves[3] = bestMoves[2];
        bestMoves[2] = bestMoves[1];
        bestMoves[1] = bestMoves[0];
        bestMoves[0] = move;
        bestPoints[4] = bestPoints[3];
        bestPoints[3] = bestPoints[2];
        bestPoints[2] = bestPoints[1];
        bestPoints[1] = bestPoints[0];
        bestPoints[0] = eval;
      } else if (eval > bestPoints[1]) {
        bestMoves[4] = bestMoves[3];
        bestMoves[3] = bestMoves[2];
        bestMoves[2] = bestMoves[1];
        bestMoves[1] = move;
        bestPoints[4] = bestPoints[3];
        bestPoints[3] = bestPoints[2];
        bestPoints[2] = bestPoints[1];
        bestPoints[1] = eval;
      } else if (eval > bestPoints[2]) {
        bestMoves[4] = bestMoves[3];
        bestMoves[3] = bestMoves[2];
        bestMoves[2] = move;
        bestPoints[4] = bestPoints[3];
        bestPoints[3] = bestPoints[2];
        bestPoints[2] = eval;
      } else if (eval > bestPoints[3]) {
        bestMoves[4] = bestMoves[3];
        bestMoves[3] = move;
        bestPoints[4] = bestPoints[3];
        bestPoints[3] = eval;
      } else if (eval > bestPoints[4]) {
        bestMoves[4] = move;
        bestPoints[4] = eval;
      }
    }
    for (String move : bestMoves) {
      if (skipped.contains(move)) {
        Bot.logger.warning("Failed to skip " + move);
      }
    }
    int count = 0;
    for (String s : bestMoves) {
      if (s != null) count++;
    }

    String[] cleanBestMoves = bestMoves;
    if (count != bestMoves.length) {
      cleanBestMoves = new String[count];
      int i = 0;
      for (String s : bestMoves) {
        if (s != null) cleanBestMoves[i++] = s;
      }
    }
    for (String move : cleanBestMoves) {
      Board pos = board.newBoardWithmove(move);
      if (depth != null && depth == 1) {
        HashMap<String, BoardWithEval> toAdd = new HashMap<String, BoardWithEval>();
        BoardWithEval p = new BoardWithEval();
        p.board = pos; p.eval = evaluate(board, white);
        toAdd.put(firstMove, p);
        positions.add(toAdd);
        continue;
      }
      if (depth != null) {
        positions.addAll(findPositions(pos, !white, depth - 1, null, null, firstMove));
      } else {
        long timeElapsed = System.currentTimeMillis() - millis;
        if ((time - timeElapsed <= 10) || System.currentTimeMillis() >= finTime) {
          HashMap<String, BoardWithEval> toAdd = new HashMap<String, BoardWithEval>();
          BoardWithEval p = new BoardWithEval();
          p.board = pos; p.eval = evaluate(board, white);
          toAdd.put(firstMove, p);
          positions.add(toAdd);
          continue;
        }
        positions.addAll(
            findPositions(pos, !white, null, (int) (time - timeElapsed) / 5, finTime, firstMove));
      }
    }
    return positions;
  }

  public String findMove(Board board, boolean white, Integer depth, Integer time, Bot bot, String syzygyPath, boolean firstMove) {
    if (firstMove) {
      String[] nextMoves = board.nextPositions(white, false);
      for (String move: nextMoves) {
        if (!board.check(white)) {
          return move;
        }
      }
      return nextMoves[0];
    }
    if (syzygyPath != null) {
      syzygyCalculator.newPath(syzygyPath);
      if (syzygyCalculator.newPos(board)) {
        Bot.logger.debug("using syzygy");
        return syzygyCalculator.bestmove(white);
      } else {
        Bot.logger.debug("not using syzygy");
      }
    }
    Bot.logger.info("Calculating...");
    // System.out.println(board);
    ArrayList<HashMap<String, BoardWithEval>> positions = new ArrayList<HashMap<String, BoardWithEval>>();
    String[] nextMoves;
    if (depth != null) {
      nextMoves = board.nextPositions(white, false);
      for (String move : nextMoves) {
        Board pos = board.newBoardWithmove(move);
        int whiteWon = pos.whiteWon(!white, false);
        if ((whiteWon == 1 && !white) || (whiteWon == 0 && white)) return move;
        if (whiteWon == -2) {
          HashMap<String, BoardWithEval> map = new HashMap<String, BoardWithEval>();
          BoardWithEval p = new BoardWithEval();
          p.board = pos;
          p.eval = evaluate(board, white);
          map.put(move, p);
          positions.add(map);
          continue;
        }
        if (pos.check(white)) {
          Bot.logger.info("move " + move + " results in check.");
          continue;
        }
        if (move == null) continue;
        positions.addAll(findPositions(pos, !white, depth * 2 - 1, null, null, move));
      }
    } else {
      nextMoves = board.nextPositions(white, false);
      for (String move : nextMoves) {
        Board pos = board.newBoardWithmove(move);
        int whiteWon = pos.whiteWon(!white, false);
        if ((whiteWon == 1 && !white) || (whiteWon == 0 && white)) return move;
        if (whiteWon == -2) {
          HashMap<String, BoardWithEval> map = new HashMap<String, BoardWithEval>();
          BoardWithEval p = new BoardWithEval();
          p.board = pos;
          p.eval = evaluate(board, white);
          map.put(move, p);
          positions.add(map);
          continue;
        }
        if (pos.check(white)) {
          Bot.logger.info("move " + move + " results in check.");
          continue;
        }
        if (move == null) continue;
        positions.addAll(
            findPositions(
                pos,
                !white,
                null,
                (time - 100) / nextMoves.length,
                System.currentTimeMillis() + time - 1000,
                move));
      }
    }
    Bot.logger.info("found moves");
    HashMap<String, ArrayList<BoardWithEval>> groupedPositions = new HashMap<String, ArrayList<BoardWithEval>>();
    for (HashMap<String, BoardWithEval> map : positions) {
      for (Map.Entry<String, BoardWithEval> entry : map.entrySet()) {
        String key = entry.getKey();
        BoardWithEval newBoard = entry.getValue();

        groupedPositions.computeIfAbsent(key, _ -> new ArrayList<>()).add(newBoard);
      }
    }
    Bot.logger.info("grouped moves");
    /*for (String move : groupedPositions.keySet()) {
      logger.info("found move " + move + "(" + Integer.toString((int)evaluate(board.newBoardWithmove(move), white)) + ")");
    }*/
    // Now find best
    HashMap<String, Integer> movesWithPoints = new HashMap<String, Integer>();
    for (String move : groupedPositions.keySet()) {
      int total = 0;
      for (BoardWithEval newBoard : groupedPositions.get(move)) {
        total += newBoard.eval;
      }
      movesWithPoints.put(move, total / groupedPositions.get(move).size());
    }
    Bot.logger.info("got points for moves");
    String bestMove = "a1a1";
    int currentMax = -1000000000;
    for (String move : movesWithPoints.keySet()) {
      // logger.info
      if (movesWithPoints.get(move) > currentMax) {
        bestMove = move;
        currentMax = movesWithPoints.get(move);
      }
    }
    Bot.logger.info("picked move");
    bot.lastMove = bestMove;
    Bot.logger.info("Move " + bestMove + " is " + Integer.toString(currentMax) + " points.");
    return bestMove;
  }
}

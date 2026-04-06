package chess.eval;

import chess.Board;
import chess.Bot;
import chess.pieces.*;
import chess.syzygy.Syzygy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Eval {
  class BoardWithEval {
    Board board;
    float eval;
    String firstMove;
    boolean whiteJustMoved;
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
    if (whiteWon == -2) points -= 30;
    if (whiteWon == -3) points -= 10;
    if (points < -20) return points;
    if (!board.moves.isEmpty()) {
      String lastMove = board.moves.getLast();
      //Bot.logger.debug("Last move was " + lastMove);
      int count = board.numMoves.get(lastMove);
      //Bot.logger.debug(count);
      points -= 5 * count;
    }
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        Piece piece = board.getPiece(new int[] {i, j});
        if (piece != null) {
          if (piece.isWhite() == white) { // Get piece points
            points += piece.value() * 2;
            if (piece.value() == 1) { // Push pawns
              if (white) {
                int rank = (int) piece.getSquare().charAt(1) - '0';
                points += rank/1.5;
              } else {
                int rank = (int) piece.getSquare().charAt(1) - '0';
                rank = 8 - rank;
                points += rank/1.5;
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
              points += piece.value();
            }
            if ((j == 0) || (j == 7)) { // Check for edge pieces
              points -= piece.value();
            }
          } else { // Lower poitns for opponent pieces
            points -= piece.value() * 2;
          }
        }
      }
    }
    String[] oppNextMoves = board.nextPositions(!white, false);
    ArrayList<Piece> pieces = board.getPieces(white);
    for (String move : oppNextMoves) {
      if (move == null) continue;
      for (Piece piece : pieces) {
        if (piece.getSquare() == null) continue;
        if (move.charAt(2) == piece.getSquare().charAt(0) && move.charAt(3) == piece.getSquare().charAt(1)) {
          points -= piece.value() * 2;
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

  public ArrayList<BoardWithEval> findPositions(
      Board board, boolean white, Integer depth, Integer time, Long finTime, String firstMove) {
    long millis = System.currentTimeMillis();
    ArrayList<BoardWithEval> positions = new ArrayList<BoardWithEval>();
    String[] nextMoves = board.nextPositions(white, false);
    String[] bestMoves = new String[5];
    ArrayList<String> skipped = new ArrayList<String>();
    for (String move : nextMoves) {
      BoardWithEval p = new BoardWithEval();
      Board b = board.newBoardWithmove(move);
      if (b.check(white)) continue;
      p.board = b;
      p.eval = evaluate(b, white);
      p.firstMove = move;
      p.whiteJustMoved = white;
      positions.add(p);
    }
    int i = 0;
    Float[] scores;
    List<Float> scoreList;
    int index;
    while (System.currentTimeMillis() < finTime) {
      BoardWithEval b = positions.get(i);
      nextMoves = b.board.nextPositions(!b.whiteJustMoved, false);
      scores = new Float[nextMoves.length];
      for (int j = 0; j < nextMoves.length; j++) scores[j] = evaluate(b.board.newBoardWithmove(nextMoves[j]), !b.whiteJustMoved);
      scoreList = new ArrayList<>(Arrays.asList(scores));
      for (int j = 0; j < 5; j++) {
        index = scoreList.indexOf(Collections.max(scoreList));
        String move = nextMoves[index];
        BoardWithEval p = new BoardWithEval();
        Board x = b.board.newBoardWithmove(move);
        if (x.check(!b.whiteJustMoved)) continue;
        p.board = x;
        p.eval = evaluate(x, !b.whiteJustMoved);
        p.firstMove = b.firstMove;
        p.whiteJustMoved = !b.whiteJustMoved;
        positions.add(p);
        scoreList.set(index, scoreList.get(index)-5000000f);
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
      i = 0;
      for (String s : bestMoves) {
        if (s != null) cleanBestMoves[i++] = s;
      }
    }
    for (String move : cleanBestMoves) {
      Board pos = board.newBoardWithmove(move);
      if (depth != null && depth == 1) {
        BoardWithEval p = new BoardWithEval();
        p.board = pos; p.eval = evaluate(board, white);
        p.firstMove = firstMove;
        positions.add(p);
        continue;
      }
      if (depth != null) {
        positions.addAll(findPositions(pos, !white, depth - 1, null, null, firstMove));
      } else {
        long timeElapsed = System.currentTimeMillis() - millis;
        if ((time - timeElapsed <= 10) || System.currentTimeMillis() >= finTime) {
          BoardWithEval p = new BoardWithEval();
          p.board = pos; p.eval = evaluate(board, white);
          p.firstMove = firstMove;
          positions.add(p);
          continue;
        }
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
    ArrayList<BoardWithEval> positions = new ArrayList<BoardWithEval>();
    if (depth != null) {
      positions.addAll(findPositions(board, white, depth * 2 - 1, null, null, ""));
    } else {
      positions.addAll(
            findPositions(
                board,
                white,
                null,
                (time - 100),
                System.currentTimeMillis() + time - 1000,
                ""));
    }
    Bot.logger.info("found moves");
    HashMap<String, ArrayList<BoardWithEval>> groupedPositions = new HashMap<String, ArrayList<BoardWithEval>>();
    for (BoardWithEval map : positions) {
      String key = map.firstMove;
      if (key != null) {
        groupedPositions.computeIfAbsent(key, _ -> new ArrayList<>()).add(map);
      }
    }
    Bot.logger.info("grouped moves");
    for (String move : groupedPositions.keySet()) {
      Bot.logger.info("found move " + move + "(" + Integer.toString((int)evaluate(board.newBoardWithmove(move), white)) + ")");
    }
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

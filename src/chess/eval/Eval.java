package chess.eval;

import chess.Board;
import chess.Bot;
import chess.pieces.*;
import chess.syzygy.Syzygy;
import java.util.ArrayList;

import javax.naming.TimeLimitExceededException;

public class Eval {
  class BoardWithEval {
    Board board;
    float eval;
    String firstMove;
    boolean whiteJustMoved;
  }
  class MoveWithPoints {
    String move;
    float eval;
  }
  Bot bot;
  Syzygy syzygyCalculator = new Syzygy();

  public Eval(Bot bot) {
    this.bot = bot;
  }

  public float evaluate(Board board, boolean evalForWhite, boolean whitesTurnNext, boolean showReasons ) {
    float points = 0;

    // Checkmates, stalemates & threefold
    int whiteWon = board.whiteWon(!whitesTurnNext, false);
    if (evalForWhite && whiteWon == 0) points += 50000; // Our checkmate as white
    if (!evalForWhite && whiteWon == 1) points += 50000; // Our checkmate as black
    if (evalForWhite && whiteWon == 1) points -= 50000; // Enemy checkmate; we are white
    if (!evalForWhite && whiteWon == 0) points -= 50000; // Enemy checkmate; we are black
    if (whiteWon == -2) points -= 100; // Draw
    if (whiteWon == -3) points -= 80; // Nearly draw (opponent could get threefold repetition)
    if (showReasons) Bot.logger.debug("points from termination:" + Float.toString(points));
    if (points < -80) return points; // Return if game ends; position doesn't matter

    // Prevent move repetition
    if (!board.moves.isEmpty()) {
      String lastMove = board.moves.getLast();
      int count = board.numMoves.get(lastMove);
      points -= 5 * count;
    }
    if (showReasons) Bot.logger.debug("points from repetition:" + Float.toString(points));

    // Per-piece points
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        Piece piece = board.getPiece(new int[] {i, j});
        if (piece != null) {
          if (piece.isWhite() == evalForWhite) {
            points += piece.value(); // Get points for piece value
            // Get points from PSQT
            if (piece.isWhite()) points += piece.pieceSquareScore()[i][j];
            else points += piece.pieceSquareScore()[7-i][7-j];

          } else {
            points -= piece.value(); // Get points for piece value
            // Get points from PSQT
            if (piece.isWhite()) points -= piece.pieceSquareScore()[i][j];
            else points -= piece.pieceSquareScore()[7-i][7-j];
          }
        }
      }
    }
    if (showReasons) Bot.logger.debug("points from material & PSQTs:" + Float.toString(points));

    // Prevent hanging pieces
    String[] oppNextMoves = board.nextPositions(whitesTurnNext, false);
    ArrayList<Piece> pieces = board.getPieces(evalForWhite);
    for (String move : oppNextMoves) {
      if (move == null) continue;
      for (Piece piece : pieces) {
        if (piece.getSquare() == null) continue;
        if (move.charAt(2) == piece.getSquare().charAt(0) && move.charAt(3) == piece.getSquare().charAt(1)) {
          points -= piece.value() * 2;
        }
      }
    }
    if (showReasons) Bot.logger.debug("points from hanging:" + Float.toString(points));

    // Checks
    if (board.check(evalForWhite, whitesTurnNext)) points -= 3;
    else if (board.check(!evalForWhite, whitesTurnNext)) points += 3;
    if (showReasons) Bot.logger.debug("points from checks:" + Float.toString(points));

    if (showReasons) Bot.logger.debug("points total:" + Float.toString(points));
    return points;
  }

  MoveWithPoints minimax(Board board, int depth, boolean maximizingPlayer, boolean whiteTurnNext, boolean evalForWhite, long endTime, float alpha, float beta) throws TimeLimitExceededException {
    if (depth == 0) {
      MoveWithPoints x = new MoveWithPoints();
      x.eval = evaluate(board, evalForWhite, whiteTurnNext, false);
      return x;
    }

    String[] moves = board.nextPositions(whiteTurnNext, false);

    if (maximizingPlayer) {
        MoveWithPoints max = new MoveWithPoints();
        max.eval = -Float.MAX_VALUE;
        //Bot.logger.info(moves.length);
        for (String move : moves) {
          if (move == null) continue;
          if (board.newBoardWithmove(move).check(evalForWhite, !whiteTurnNext)) continue;
          if (System.currentTimeMillis() > endTime) throw new TimeLimitExceededException();
          MoveWithPoints x = minimax(board.newBoardWithmove(move), depth - 1, false, !whiteTurnNext, evalForWhite, endTime, alpha, beta);
          if (x.eval > max.eval) {
            //Bot.logger.info(move);
            max.eval = x.eval;
            max.move = move;
          }
          alpha = Math.max(alpha, max.eval);
          if (alpha >= beta) break;
        }
        return max;
    } else {
        MoveWithPoints min = new MoveWithPoints();
        min.eval = Float.MAX_VALUE;
        for (String move : moves) {
          if (board.newBoardWithmove(move).check(!evalForWhite, !whiteTurnNext)) continue;
          if (System.currentTimeMillis() > endTime) throw new TimeLimitExceededException();
            MoveWithPoints x = minimax(board.newBoardWithmove(move), depth - 1, true, !whiteTurnNext, evalForWhite, endTime, alpha, beta);
            if (x.eval < min.eval) {
              min.eval = x.eval;
              min.move = move;
            }
            beta = Math.min(beta, min.eval);
            if (alpha >= beta) break;
        }
        return min;
    }
  }

  public String findMove(Board board, boolean white, Integer depth, Integer time, Bot bot, String syzygyPath, boolean firstMove) {
    if (firstMove) {
      String[] nextMoves = board.nextPositions(white, false);
      for (String move: nextMoves) {
        if (!board.check(white, white)) {
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
    if (depth != null) {
      try {
        return minimax(board, depth, true, white, white, Long.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE).move;
      } catch (TimeLimitExceededException e) {
        return null;
      }
    } else {
      Bot.logger.info("Time based");
      long endTime = System.currentTimeMillis()+time-1000;
      MoveWithPoints best = new MoveWithPoints();
      try {
        best = minimax(board, 1, true, white, white, Long.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE);
      } catch (TimeLimitExceededException e) {
        Bot.logger.info("Error in timelimit");
      }
      MoveWithPoints x;
      //best.eval = -Float.MAX_VALUE;
      // Bot.logger.info(best.move);
      for (depth = 2; System.currentTimeMillis() < endTime; depth++) {
        //Bot.logger.info("depth " + Integer.toString(depth));
        try {
          x = minimax(board, depth, true, white, white, endTime, -Float.MAX_VALUE, Float.MAX_VALUE);
          if (x.eval > best.eval) best = x;
        } catch (TimeLimitExceededException e) {
          Bot.logger.info("Time limit exceeded");
          break;
        }
      }
      Bot.logger.info("score cp " + Integer.toString((int)best.eval*100) + " depth " + Integer.toString(depth-1));
      Bot.logger.debug(board.newBoardWithmove(best.move).check(white, !white));
      evaluate(board.newBoardWithmove(best.move), white, white, true);
      return best.move;
    }
  }
}

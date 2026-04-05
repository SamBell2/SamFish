package chess.syzygy;

import chess.Board;
import chess.Bot;
import chess.pieces.*;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Syzygy {
    Board board;
    File tables;
    RandomAccessFile rtbw;
    RandomAccessFile rtbz;
    public boolean newPos(Board board) {
        if (board.getPieces().size() <= this.size()) {
            this.board = board;
            ArrayList<Piece> whitePieces = new ArrayList<Piece>();
            ArrayList<Piece> blackPieces = new ArrayList<Piece>();
            for (Piece piece : board.getPieces()) {
                if (piece.isWhite()) {
                    whitePieces.add(piece);
                } else {
                    blackPieces.add(piece);
                }
            }
            StringBuilder sb = new StringBuilder();
            ArrayList<Piece> whitePiecesOrdered = new ArrayList<Piece>();
            ArrayList<Piece> blackPiecesOrdered = new ArrayList<Piece>();
            int blackPoints = 0; int whitePoints = 0;
            for (Piece piece : whitePieces) whitePoints += piece.value();
            for (Piece piece : blackPieces) blackPoints += piece.value();
            while (whitePieces.size() > 0) {
                int maxValue = 0;
                Piece maxPiece = null;
                for (Piece piece : whitePieces) {
                    if (piece == null) continue;
                    if (piece.value() == 0) {
                        //sb.append(piece.toString());
                        whitePiecesOrdered.add(piece);
                        whitePieces.set(whitePieces.indexOf(piece), null);
                    } else if (piece.value() > maxValue) {
                        maxPiece = piece;
                        maxValue = piece.value();
                    }
                }
                while (whitePieces.contains(null)) whitePieces.remove(null);
                if (maxPiece != null) {
                    //sb.append(maxPiece.toString());
                    whitePiecesOrdered.add(maxPiece);
                    whitePieces.remove(maxPiece);
                }
            }
            //sb.append("v");
            while (blackPieces.size() > 0) {
                int maxValue = 0;
                Piece maxPiece = null;
                for (Piece piece : blackPieces) {
                    if (piece == null) continue;
                    if (piece.value() == 0) {
                        //sb.append(piece.toString().toUpperCase());
                        blackPiecesOrdered.add(piece);
                        blackPieces.set(blackPieces.indexOf(piece), null);
                    } else if (piece.value() > maxValue) {
                        maxPiece = piece;
                        maxValue = piece.value();
                    }
                }
                while (blackPieces.contains(null)) blackPieces.remove(null);
                if (maxPiece != null) {
                    //sb.append(maxPiece.toString().toUpperCase());
                    blackPiecesOrdered.add(maxPiece);
                    blackPieces.remove(maxPiece);
                }
            }
            Bot.logger.info(Integer.toString(whitePiecesOrdered.size()));
            Bot.logger.info(Integer.toString(blackPiecesOrdered.size()));
            if (whitePiecesOrdered.size() > blackPiecesOrdered.size()) {
                for (Piece piece : whitePiecesOrdered) {
                    sb.append(piece.toString());
                }
                sb.append("v");
                 for (Piece piece : blackPiecesOrdered) {
                    sb.append(piece.toString().toUpperCase());
                }
            } else if (whitePiecesOrdered.size() < blackPiecesOrdered.size()) {
                for (Piece piece : blackPiecesOrdered) {
                    sb.append(piece.toString().toUpperCase());
                }
                sb.append("v");
                 for (Piece piece : whitePiecesOrdered) {
                    sb.append(piece.toString());
                }
            } else if (whitePoints > blackPoints) {
                for (Piece piece : whitePiecesOrdered) {
                    sb.append(piece.toString());
                }
                sb.append("v");
                 for (Piece piece : blackPiecesOrdered) {
                    sb.append(piece.toString().toUpperCase());
                }
            } else {
                for (Piece piece : blackPiecesOrdered) {
                    sb.append(piece.toString().toUpperCase());
                }
                sb.append("v");
                 for (Piece piece : whitePiecesOrdered) {
                    sb.append(piece.toString());
                }
            }
            Bot.logger.info(sb);
            try {
                rtbw = new RandomAccessFile(tables.getAbsolutePath() + "/" + sb.toString()+".rtbw", "r");
                rtbz = new RandomAccessFile(tables.getAbsolutePath() + "/" + sb.toString()+".rtbz", "r");
                /*rtbw = new RandomAccessFile(tables.getAbsolutePath() + "/" + "KBPvKR"+".rtbw", "r");
                rtbz = new RandomAccessFile(tables.getAbsolutePath() + "/" + "KBPvKR"+".rtbz", "r");*/
            } catch (IOException e) {
                Bot.logger.fatal("Something went wrong");
                Bot.logger.fatal(e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }
    public void newPath(String path) {
        Bot.logger.debug(path);
        tables = new File(path);
    }
    public String bestmove(boolean white) {
        Bot.logger.debug(board);
        //System.err.println(rtbw.getAbsolutePath());
        HashMap<Integer, ArrayList<String>> moves = new HashMap<Integer, ArrayList<String>>();
        moves.put(-2, new ArrayList<String>());
        moves.put(-1, new ArrayList<String>());
        moves.put(0, new ArrayList<String>());
        moves.put(1, new ArrayList<String>());
        moves.put(2, new ArrayList<String>());
        for (String move : board.nextPositions(white, false)) {
            //Bot.logger.info(WDLProbe.probe(board.newBoardWithmove(move).genFEN(), white));
            /* if (WDLProbe.probe(board.newBoardWithmove(move).genFEN(), white) >= currentMax) {
                posibleMoves.add(move);
                Bot.logger.info("move " + move);
                currentMax = WDLProbe.probe(board.newBoardWithmove(move).genFEN(), white);
            } */
           if (move == null) continue;
           if (board.newBoardWithmove(move).check(white)) continue;
           if (board.newBoardWithmove(move).whiteWon(!white, false) == 0 && white) return move;
           if (board.newBoardWithmove(move).whiteWon(!white, false) == 1 && !white) return move;
           if ((board.newBoardWithmove(move).threefold() || board.newBoardWithmove(move).whiteWon(!white, false) == -3) && (WDLProbe.probe(board.genFEN(), white, board.fullMoves, board.halfMoves) > 0)) {
            Bot.logger.info("Possible threefold");
            continue;
           }
           Bot.logger.info(board.newBoardWithmove(move).threefold());
           Bot.logger.info("Halfmoves:");
           Bot.logger.info(board.newBoardWithmove(move).halfMoves);
            moves.get(WDLProbe.probe(board.newBoardWithmove(move).genFEN(), white, board.newBoardWithmove(move).fullMoves, board.newBoardWithmove(move).halfMoves)).add(move);
        }
        /* for (String move : moves.get(0)) Bot.logger.info(move);
        System.out.println();
        for (String move : moves.get(2)) Bot.logger.info(move); */
        //return Integer.toString(value);
        int currentMax = -3;
        for (int i : moves.keySet()) {
            if (moves.get(i).size() != 0 && i > currentMax) {
                currentMax = i;
            }
        }
        Bot.logger.info(currentMax);
        ArrayList<String> top = moves.get(currentMax);
        /* if (currentMax > 0) {
            int bestScore = 10000;
            String bestMove = top.get(0);
            for (String move : top) {
                if (DTZProbe.probe(board.newBoardWithmove(move).genFEN(), white, board.newBoardWithmove(move).fullMoves, board.newBoardWithmove(move).halfMoves) < bestScore) {
                    bestScore = DTZProbe.probe(board.newBoardWithmove(move).genFEN(), white, board.newBoardWithmove(move).fullMoves, board.newBoardWithmove(move).halfMoves);
                    bestMove = move;
                }
            }
            return bestMove;
        } else  */if (currentMax < 0) {
            int bestScore = -10000;
            String bestMove = top.get(0);
            Bot.logger.info(bestMove);
            for (String move : top) {
                if (move == null) continue;
                if (DTZProbe.probe(board.newBoardWithmove(move).genFEN(), white, board.newBoardWithmove(move).fullMoves, board.newBoardWithmove(move).halfMoves) > bestScore) {
                    bestScore = DTZProbe.probe(board.newBoardWithmove(move).genFEN(), white, board.newBoardWithmove(move).fullMoves, board.newBoardWithmove(move).halfMoves);
                    bestMove = move;
                    Bot.logger.info("Updated bestMove to " + bestMove);
                }
            }
            Bot.logger.info(bestMove);
            return bestMove;
        } else {
            int bestScore = 10000;
            String bestMove = top.get(0);
            for (String move : top) {
                if (DTZProbe.probe(board.newBoardWithmove(move).genFEN(), white, board.newBoardWithmove(move).fullMoves, board.newBoardWithmove(move).halfMoves) < bestScore) {
                    bestScore = DTZProbe.probe(board.newBoardWithmove(move).genFEN(), white, board.newBoardWithmove(move).fullMoves, board.newBoardWithmove(move).halfMoves);
                    bestMove = move;
                }
            }
            Bot.logger.info(currentMax);
            Bot.logger.info(bestScore);
            return bestMove;
        }
    }
    private int size() {
        File[] files = this.tables.listFiles();
        int maxLength = 0;
        int size = 0;
        for (File file : files) {
            if (file.getName().length() > maxLength) {
                maxLength = file.getName().length();
                Bot.logger.info(maxLength);
                Bot.logger.info(file.getName());
                size = maxLength - 6;
            }
        }
        Bot.logger.info("Siz");
        Bot.logger.info(size);
        return size;
    }
}

package chess;

import chess.pieces.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Board {
  Piece[][] board =
      new Piece[][] {
        {null, null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null, null}
      };
  static final String textBoard =
      """
        +---+---+---+---+---+---+---+---+
      8 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
        +---+---+---+---+---+---+---+---+
      7 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
        +---+---+---+---+---+---+---+---+
      6 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
        +---+---+---+---+---+---+---+---+
      5 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
        +---+---+---+---+---+---+---+---+
      4 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
        +---+---+---+---+---+---+---+---+
      3 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
        +---+---+---+---+---+---+---+---+
      2 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
        +---+---+---+---+---+---+---+---+
      1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
        +---+---+---+---+---+---+---+---+
          a   b   c   d   e   f   g   h\
      """;
  String repr;
  ArrayList<String> moves = new ArrayList<String>();
  boolean printFormatting;
  String FEN;
  ArrayList<String> FENs;
  public int fullMoves;
  public int halfMoves;

  public Board(boolean formatting) {
    fullMoves = 0; halfMoves = 0;
    printFormatting = formatting;
    FENs = new ArrayList<String>();
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (i == 0) {
          if (j == 0 || j == 7) {
            board[i][j] = new Rook(Main.columnLetters[j] + (8 - i), false);
          } else if (j == 1 || j == 6) {
            board[i][j] = new Knight(Main.columnLetters[j] + (8 - i), false);
          } else if (j == 2 || j == 5) {
            board[i][j] = new Bishop(Main.columnLetters[j] + (8 - i), false);
          } else if (j == 3) {
            board[i][j] = new Queen(Main.columnLetters[j] + (8 - i), false);
          } else {
            board[i][j] = new King(Main.columnLetters[j] + (8 - i), false);
          }
        } else if (i == 1) {
          board[i][j] = new Pawn(Main.columnLetters[j] + (8 - i), false);
        } else if (i == 6) {
          board[i][j] = new Pawn(Main.columnLetters[j] + (8 - i), true);
        } else if (i == 7) {
          if (j == 0 || j == 7) {
            board[i][j] = new Rook(Main.columnLetters[j] + (8 - i), true);
          } else if (j == 1 || j == 6) {
            board[i][j] = new Knight(Main.columnLetters[j] + (8 - i), true);
          } else if (j == 2 || j == 5) {
            board[i][j] = new Bishop(Main.columnLetters[j] + (8 - i), true);
          } else if (j == 3) {
            board[i][j] = new Queen(Main.columnLetters[j] + (8 - i), true);
          } else {
            board[i][j] = new King(Main.columnLetters[j] + (8 - i), true);
          }
        }
      }
    }
    FENs.add(genFEN());
  }

  @Override
  public String toString() {
    /*repr = "";
    repr += textBoard;
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (board[i][j] == null) {
          repr = repr.replaceFirst("0", " ");
        } else if (printFormatting) {
          if (board[i][j].isWhite()) {
            repr = repr.replaceFirst("0", "\u001B[1m" + board[i][j].toString() + "\u001B[22;39m");
          } else {
            repr =
                repr.replaceFirst("0", "\u001B[1;31m" + board[i][j].toString() + "\u001B[22;39m");
          }
        } else {
          repr = repr.replaceFirst("0", board[i][j].toString());
        }
      }
    }*/
    StringBuilder repr = new StringBuilder(); 
    for (int i = 0; i < 8; i ++) {
      repr.append(8-i);
      repr.append(" ");
      for (int j = 0; j < 8; j ++) {
        if (board[i][j] == null) {
          repr.append(".");
        } else {
          repr.append(board[i][j].toString());
        }
        repr.append(" ");
      }
      repr.append("\n");
    }
    repr.append("  ");
    for (int i = 0; i < 8; i ++) {
      repr.append(Main.columnLetters[i]);
      repr.append(" ");
    }
    return repr.toString();
  }

  public void move(String move) {
    if (move == null) return;
    //if (getPiece(new char[]{move.charAt(0), move.charAt(1)}.toString()) != null && getPiece(new char[]{move.charAt(0), move.charAt(1)}.toString()).isWhite()) fullMoves ++;
    moves.add(move);
    if ((move.equals("e1g1") || move.equals("e1c1") || move.equals("e8g8") || move.equals("e8c8")) && getPiece(((Character)move.charAt(0)).toString() + ((Character)move.charAt(1))).value() == 0) {
      //Bot.logger.debug(((Character)move.charAt(0)).toString() + ((Character)move.charAt(1)));
      Piece king;
      Piece rook;
      switch (move) {
        case "e1g1":
          king = board[7][4];
          if (king == null) break;
          king.newPos("g1");
          board[7][6] = king;
          board[7][4] = null;
          rook = board[7][7];
          if (rook == null) break;
          rook.newPos("f1");
          board[7][5] = rook;
          board[7][7] = null;
          break;
        case "e1c1":
          king = board[7][4];
          if (king == null) break;
          king.newPos("c1");
          board[7][2] = king;
          board[7][4] = null;
          rook = board[7][0];
          if (rook == null) break;
          rook.newPos("d1");
          board[7][3] = rook;
          board[7][0] = null;
          break;
        case "e8g8":
          king = board[0][4];
          if (king == null) break;
          king.newPos("g8");
          board[0][6] = king;
          board[0][4] = null;
          rook = board[0][7];
          if (rook == null) break;
          rook.newPos("f8");
          board[0][5] = rook;
          board[0][7] = null;
          break;
        case "e8c8":
          king = board[0][4];
          if (king == null) break;
          king.newPos("c8");
          board[0][2] = king;
          board[0][4] = null;
          rook = board[0][0];
          if (rook == null) break;
          rook.newPos("d8");
          board[0][3] = rook;
          board[0][0] = null;
          break;

        default:
          break;
      }
    } else {
      Piece piece =
          board[8 - ((int) move.charAt(1) - '0')][
              Arrays.binarySearch(Main.columnLetters, "" + move.charAt(0))];
      if (piece == null) return;
        if (piece.value() == 1 || board[8 - ((int) move.charAt(3) - '0')][
                Arrays.binarySearch(Main.columnLetters, "" + move.charAt(2))] != null) {
          halfMoves = 0;
        } else halfMoves ++;
      if (move.length() == 5) {
        board[8 - ((int) move.charAt(1) - '0')][
                Arrays.binarySearch(Main.columnLetters, "" + move.charAt(0))] =
            null;
        switch (move.charAt(4)) {
          case 'q':
            board[8 - ((int) move.charAt(3) - '0')][
                    Arrays.binarySearch(Main.columnLetters, "" + move.charAt(2))] =
                new Queen((move.charAt(2) + "") + (move.charAt(3) + ""), piece.isWhite());
            break;
          case 'r':
            board[8 - ((int) move.charAt(3) - '0')][
                    Arrays.binarySearch(Main.columnLetters, "" + move.charAt(2))] =
                new Rook((move.charAt(2) + "") + (move.charAt(3) + ""), piece.isWhite());
            break;
          case 'b':
            board[8 - ((int) move.charAt(3) - '0')][
                    Arrays.binarySearch(Main.columnLetters, "" + move.charAt(2))] =
                new Bishop((move.charAt(2) + "") + (move.charAt(3) + ""), piece.isWhite());
            break;
          case 'n':
            board[8 - ((int) move.charAt(3) - '0')][
                    Arrays.binarySearch(Main.columnLetters, "" + move.charAt(2))] =
                new Knight((move.charAt(2) + "") + (move.charAt(3) + ""), piece.isWhite());
            break;
          default:
            board[8 - ((int) move.charAt(3) - '0')][
                    Arrays.binarySearch(Main.columnLetters, "" + move.charAt(2))] =
                new Pawn((move.charAt(2) + "") + (move.charAt(3) + ""), piece.isWhite());
            break;
        }
        FENs.add(genFEN());
        return;
      }
      /*System.out.println(piece);
      System.out.println(board[8 - ((int) move.charAt(1) - '0')][
              Arrays.binarySearch(Main.columnLetters, "" + move.charAt(0))]);
      System.out.println(8 - ((int) move.charAt(1) - '0'));
      System.out.println(Arrays.binarySearch(Main.columnLetters, "" + move.charAt(0)));
      System.out.println(move);
      System.out.println(board[8 - ((int) move.charAt(1) - '0')]);
      for (Piece line : board[8 - ((int) move.charAt(1) - '0')]) {
          System.out.println(line);
      }
      System.out.println(kingSquare(false));
      System.out.println(this);*/
      piece.newPos((move.charAt(2) + "") + (move.charAt(3) + ""));
      if (piece.value() == 1
          && board[8 - ((int) move.charAt(3) - '0')][
                  Arrays.binarySearch(Main.columnLetters, "" + move.charAt(2))]
              == null
          && move.charAt(0) != move.charAt(2)) {
        // logger.debug("En passant");
        String takePos = new String(new char[] {move.charAt(2), move.charAt(1)});
        // logger.debug("En passant");
        int[] takeIndex = posToIndex(takePos);
        // logger.debug("En passant");
        // logger.debug(Integer.toString(takeIndex[0]));
        // logger.debug(Integer.toString(takeIndex[1]));
        // logger.debug(takePos);
        // logger.debug(Integer.toString(Arrays.binarySearch(Main.columnLetters,
        // ""+takePos.charAt(0))));
        board[takeIndex[0]][takeIndex[1]] = null;
        // logger.debug("En passant");
        FENs.add(genFEN());
        return;
      }
      board[8 - ((int) move.charAt(3) - '0')][
              Arrays.binarySearch(Main.columnLetters, "" + move.charAt(2))] =
          piece;
      board[8 - ((int) move.charAt(1) - '0')][
              Arrays.binarySearch(Main.columnLetters, "" + move.charAt(0))] =
          null; // */
    }
    FENs.add(genFEN());
  }

  public Piece[][] getBoard() {
    return board;
  }

  public int[] posToIndex(String pos) {
    int[] index = {
      8 - ((int) pos.charAt(1) - '0'), Arrays.binarySearch(Main.columnLetters, "" + pos.charAt(0))
    };
    return index;
  }

  public String indexToPos(int[] index) {
    return Main.columnLetters[index[1]] + (8 - index[0]);
  }

  public Piece getPiece(int[] index) {
    return board[index[0]][index[1]];
  }

  public Piece getPiece(String pos) {
    // Bot.logger.info(pos);
    int[] index = posToIndex(pos);
    //Bot.logger.info(index[0]);
    //Bot.logger.info(index[1]);
    return board[index[0]][index[1]];
  }

  public Board newBoardWithmove(String move) {
    Board newBoard = new Board(printFormatting);
    if (FEN != null) {
      newBoard.getFEN(FEN);
    }
    for (String oldMove : moves) {
      newBoard.move(oldMove);
    }
    // System.out.println(move);
    newBoard.move(move);
    return newBoard;
  }

  public String[] nextPositions(boolean whitesTurn, boolean ignoreKingMoves) {
    ArrayList<String> newBoards = new ArrayList<String>();
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (board[i][j] != null && (board[i][j].isWhite() == whitesTurn)) {
          if (ignoreKingMoves && board[i][j].value() == 0) continue;
          for (String move : board[i][j].GetMoves(this)) {
            newBoards.add(move);
          }
        }
      }
    }
    String[] toReturn = newBoards.toArray(new String[moves.size()]);
    return toReturn;
  }

  public int whiteWon(boolean whitesTurn, boolean showReasons) { // returns 0 (white won) 1(black won) -2(draw) -3(nearly draw) -1 (none)
    if (threefold()) {
      return -2;
    }
    if (moves.size() > 8
        && (moves.get(moves.size() - 1).equals(moves.get(moves.size() - 5)))
        && (moves.get(moves.size() - 2).equals(moves.get(moves.size() - 6)))
        && (moves.get(moves.size() - 3).equals(moves.get(moves.size() - 7)))
        && (moves.get(moves.size() - 4).equals(moves.get(moves.size() - 8)))) {
      return -3;
    }
    boolean escape = false;
    for (String move : nextPositions(whitesTurn, false)) {
      Board nextBoard = newBoardWithmove(move);
      boolean takenThisMove = false;
      for (String nextMove : nextBoard.nextPositions(!whitesTurn, false)) {
        try {
          if (new String(new char[] {nextMove.charAt(2), nextMove.charAt(3)})
              .equals(nextBoard.kingSquare(whitesTurn))) {
            takenThisMove = true;
          }
        } catch (Exception e) {
          // System.out.println("error");
          // logger.warning(e.getMessage());
        }
      }
      if (!takenThisMove) {
        //if (showReasons) Bot.logger.info(move);
        if (showReasons) System.out.println(move);
        escape = true;
      }
    }
    //if (showReasons) Bot.logger.info(escape ? "can escape" : "can't escape");
    if (showReasons) System.out.println(escape ? "can escape" : "can't escape");
    boolean check = false;
    for (String move : nextPositions(!whitesTurn, false)) {
      try {
        if (new String(new char[] {move.charAt(2), move.charAt(3)}).equals(kingSquare(whitesTurn)))
          check = true;
      } catch (Exception e) {
        // System.out.println(move);
      }
    }
    if (whitesTurn) {
      if (!escape && check) return 1;
      if (!escape) return -2;
    } else {
      if (!escape && check) return 0;
      if (!escape) return -2;
    }
    return -1;
  }

  public boolean check(boolean whitesTurn) {
    String kingPos = kingSquare(whitesTurn);
    for (String move : nextPositions(!whitesTurn, false)) {
      // logger.info(move + " " + kingPos);
      if (move != null
          && move.charAt(2) == kingPos.charAt(0)
          && move.charAt(3) == kingPos.charAt(1)) {
        // logger.info("check");
        return true;
      }
    }
    return false;
  }
  public boolean threefold() {
    if (Collections.frequency(FENs, genFEN()) >= 3) return true;
    return false;
  }

  public String kingSquare(boolean white) {
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (board[i][j] != null && board[i][j].value() == 0 && board[i][j].isWhite() == white) {
          return indexToPos(new int[] {i, j});
        }
      }
    }
    return null;
  }

  public String genFEN() {
    String FEN = "";
    for (int i = 0; i < 8; i++) {
      int count = 0;
      for (int j = 0; j < 8; j++) {
        if (board[i][j] == null) {
          count++;
        } else {
          if (count > 0) {
            FEN += Integer.toString(count);
            count = 0;
          }
          FEN += board[i][j];
        }
      }
      if (count > 0) {
        FEN += Integer.toString(count);
      }
      if (i != 7) FEN += "/";
    }
    return FEN;
  }

  public void getFEN(String FEN) {
    this.FEN = FEN;
    board =
        new Piece[][] {
          {null, null, null, null, null, null, null, null},
          {null, null, null, null, null, null, null, null},
          {null, null, null, null, null, null, null, null},
          {null, null, null, null, null, null, null, null},
          {null, null, null, null, null, null, null, null},
          {null, null, null, null, null, null, null, null},
          {null, null, null, null, null, null, null, null},
          {null, null, null, null, null, null, null, null}
        };
    String[] unravelled = FEN.split("/");
    int i = 0;
    int j = 0;
    for (String section : unravelled) {
      if (section.equals("")) continue;
      for (char character : section.toCharArray()) {
        if (character == '1'
            || character == '2'
            || character == '3'
            || character == '4'
            || character == '5'
            || character == '6'
            || character == '7'
            || character == '8') {
          j += (character - '0');
        } else {
          switch (character) {
            case 'p':
              board[i][j] = new Pawn(Main.columnLetters[j] + (8 - i), false);
              break;
            case 'b':
              board[i][j] = new Bishop(Main.columnLetters[j] + (8 - i), false);
              break;
            case 'n':
              // Piece[] line = board[i];
              // logger.debug(line[j].toString());
              board[i][j] = new Knight(Main.columnLetters[j] + (8 - i), false);
              break;
            case 'r':
              board[i][j] = new Rook(Main.columnLetters[j] + (8 - i), false);
              break;
            case 'q':
              board[i][j] = new Queen(Main.columnLetters[j] + (8 - i), false);
              break;
            case 'k':
              board[i][j] = new King(Main.columnLetters[j] + (8 - i), false);
              break;
            case 'P':
              board[i][j] = new Pawn(Main.columnLetters[j] + (8 - i), true);
              break;
            case 'B':
              board[i][j] = new Bishop(Main.columnLetters[j] + (8 - i), true);
              break;
            case 'N':
              board[i][j] = new Knight(Main.columnLetters[j] + (8 - i), true);
              break;
            case 'R':
              board[i][j] = new Rook(Main.columnLetters[j] + (8 - i), true);
              break;
            case 'Q':
              board[i][j] = new Queen(Main.columnLetters[j] + (8 - i), true);
              break;
            case 'K':
              board[i][j] = new King(Main.columnLetters[j] + (8 - i), true);
              break;
            default:
              break;
          }
          j++;
        }
      }
      j = 0;
      i++;
    }
  }
  public ArrayList<Piece> getPieces () {
    ArrayList<Piece> pieces = new ArrayList<Piece>();
    for (int i = 0; i < 8; i ++) {
      for (int j = 0; j < 8; j ++) {
        if (board[i][j] != null) {
          pieces.add(board[i][j]);
        }
      }
    }
    return pieces;
  }
  public static boolean equivalent(Board b1, Board b2) {
    for (int i = 0; i < 8; i ++) {
      for (int j = 0; j < 8; j ++) {
        Piece b1Piece = b1.getPiece(new int[]{i, j});
        Piece b2Piece = b2.getPiece(new int[]{i, j});
        if (b1Piece == null ^ b2Piece == null) return false;
        if (b1Piece == null) continue;
        if (b1Piece.getClass() != b2Piece.getClass() || b1Piece.isWhite() != b2Piece.isWhite()) return false;
      }
    }
    return true;
  }
}

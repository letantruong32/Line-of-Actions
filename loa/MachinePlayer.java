/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import static loa.Piece.*;
import java.util.Random;
import java.util.ArrayList;

/** An automated Player.
 *  @author Truong Le
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        // FIXME

        if (depth == 0) {
            return heuristicEstimate(board);
        }

//        if (board.gameOver()) {
//            System.out.println(board.getMoves().size());
//            System.out.println("game over");
//            return 0;
//        }

        /**_foundMove = null; */
        int bestScore = 0;
        if (sense == 1) {
            bestScore = -WINNING_VALUE;
        } else {
            bestScore = WINNING_VALUE;
        }

        int score;

        for (Move move: board.legalMoves()) {
            board.makeMove(move);
            score = findMove(board, depth - 1, false, sense, alpha, beta);

            if (sense == 1 && score > bestScore) {
                bestScore = score;
            } else if (sense == -1 && score < bestScore) {
                bestScore = score;
            }


            if (sense == 1) {
                alpha = Math.max(alpha, score);
            } else if (sense == - 1) {
                beta = Math.min(beta, score);
            }


            if (saveMove && bestScore == score) {
                if (move != null) {
                    _foundMove = move;
                }
            }

            board.retract();

            if (alpha >= beta) {
                break;
            }
        }
        //System.out.println("BEST SCORE: " + bestScore);
        return bestScore;
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 2;
    }

    /** heuristicEstimate
     * White is more advantageous if:
     * 1. It has more captureMoves
     * 2. It has more density (largest reg size)
     * 3. It has less regSize
     * 4. It has less numberPieces
     *
     * @param board
     * @return board state value
     */
    private int heuristicEstimate(Board board) {
        int staticScore = 1;

        Random r = new Random();

//        int numWPSecret = board.numSecret(WP);
//        int numBPSecret = board.numSecret(BP);
//        if (numWPSecret > numBPSecret) {
//            staticScore += numWPSecret * r.nextInt(20);
//            System.out.println("Tr");
//        } else if (numWPSecret < numBPSecret) {
//            staticScore -= (numBPSecret * r.nextInt(20));
//            System.out.println("Fa");
//        } else {
//            staticScore += 1;
//        }


        int regSizeW = board.getRegionSizes(WP).size();
        int regSizeB = board.getRegionSizes(BP).size();

        if (regSizeW == 1 && board.turn() == WP) {
            //System.out.println(board.toString());
            //System.out.println("True");
            return WINNING_VALUE + r.nextInt(25);
        } else if (regSizeB == 1 && board.turn() == BP) {
            //System.out.println("False");
            return -WINNING_VALUE - r.nextInt(25);
        }

        if (regSizeB > regSizeW) {
            if (board.turn() == WP) {
                staticScore += (regSizeW * r.nextInt(25));
            }
        } else if (regSizeW > regSizeB) {
            if (board.turn() == BP) {
                staticScore -= (regSizeB * r.nextInt(25));
            }
        }



//        int densityWP = board.density(WP);
//        int densityBP = board.density(BP);
//        if (densityWP > densityBP) {
//            if (staticScore < 0) {
//                staticScore *= -1;
//                staticScore += densityWP * r.nextInt(35);
//            }
//        } else if (densityBP > densityWP) {
//            if (staticScore > 0) {
//                staticScore *= -1;
//                staticScore += densityBP * r.nextInt(35);
//            }
//        }


//        int numWP = board.numPieces(WP);
//        int numBP = board.numPieces(BP);
//        int totalPieces = numWP + numBP;
//        if (numWP > 10 && numBP > 10) {
//            staticScore +=  1;
//        }
//        if ((numWP < totalPieces / 2.5) || (numWP < 6 && numBP > 6)) {
//            if (staticScore <= 0) {
//                staticScore *= -1;
//                staticScore += numWP * r.nextInt(30);
//                System.out.println("TRTRTRTR");
//            }
//        } else if ((numBP < totalPieces / 2.5) || (numBP < 6 && numWP > 6)) {
//            if (staticScore >= 0) {
//                staticScore *= -1;
//                staticScore -= numBP * r.nextInt(30);
//                System.out.println("FFFF");
//            }
//        }



//        Board b1 = new Board(board);
//        ArrayList<Move> m1 = b1.getMoves();
//        b1.retract();
//
//        int regW1 = b1.getRegionSizes(WP).size();
//        int regB1 = b1.getRegionSizes(BP).size();
//        System.out.println("WHITE PREV: " + regW1);
//
//
//        int regW = board.getRegionSizes(WP).size();
//        int regB = board.getRegionSizes(BP).size();
//        System.out.println("WHITE: " + regW);
//
//        if (board.turn() == WP) {
//            if (regW > regW1) {
//                staticScore += regW * r.nextInt(20);
//                System.out.println("WHITE");
//            }
//        } else if (board.turn() == BP) {
//            if (regB > regB1) {
//                staticScore -= regB * r.nextInt(20);
//                System.out.println("BLACK");
//            }
//        }





        return staticScore;
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

}

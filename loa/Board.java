/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Truong Le
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move.
     * FIXME*/
    void initialize(Piece[][] contents, Piece side) {
        for (int r = contents.length - 1; r >= 0; r--) {
            for (int c = 0; c < contents[r].length; c++) {
                set(sq(c, r), contents[r][c]);
            }
        }
        _turn = side;
        _moveLimit = DEFAULT_MOVE_LIMIT;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD.
     * FIXME*/
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }

        _moves.clear();
        _moves.addAll(board._moves);

        //System.out.println("COPY MOVES: " + board._moves);

        _turn = board._turn;

        for (int i = 0; i < _board.length; i++) {
            _board[i] = board._board[i];
        }
        //System.out.println("THIS TO STR: " + this.toString());

        _moveLimit = board._moveLimit;
        _winnerKnown = board._winnerKnown;
        _winner = board._winner;

        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        _whiteRegionSizes.addAll(board._whiteRegionSizes);
        _blackRegionSizes.addAll(board._blackRegionSizes);
        //System.out.println("THIS: " + this.toString());
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Newly-created getter to get curr _BOARD.
     * @return current state of the board
     * */
    Piece[] getBoard() {
        return _board;
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null.
     *  FIXME */
    void set(Square sq, Piece v, Piece next) {
        _board[sq.index()] = v;

        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** Assuming isLegal(MOVE), make MOVE. Assumes MOVE.isCapture()
     *  is false.
     *  FIXME*/
    void makeMove(Move move) {
        assert isLegal(move);
        _subsetsInitialized = false;

        Square toSq = move.getTo();
        if (_board[toSq.index()] != EMP) {
            move = move.captureMove();
            toSq = move.getTo();
            set(toSq, EMP);
        }

        Square fromSq = move.getFrom();
        set(toSq, _board[fromSq.index()]);
        set(fromSq, EMP);

        _moves.add(move);


        _turn = _turn.opposite();
    }

    /** Get All moves. */
    public ArrayList<Move> getMoves() {
        return _moves;
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0.
     *  FIXME*/
    void retract() {
        assert movesMade() > 0;
        Move prevMove = _moves.remove(_moves.size() - 1);

        Square fromSq = prevMove.getFrom();
        Square toSq = prevMove.getTo();


        if (prevMove.isCapture()) {
            set(toSq, _board[toSq.index()].opposite());
            set(fromSq, _board[toSq.index()].opposite());
        } else {
            set(fromSq, _board[toSq.index()]);
            set(toSq, EMP);
        }

        _turn = _turn.opposite();
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move.
     *  FIXME*/
    boolean isLegal(Square from, Square to) {
        int direction = from.direction(to);
        int distance = from.distance(to);
        if (from.moveDest(direction, distance) != null) {
            if (get(from) == get(to)) {
                return false;
            }
            if (distance != countLOA(from, to)) {
                return false;
            }
            if (blocked(from, to)) {
                return false;
            }
            return true;
        }
        return true;
    }

    /** Return Num of Squares not EMP in the LOA
     *  Helper method for isLegal.
     * @param from the initial Square position
     * @param to the target Square position
     */
    public int countLOA(Square from, Square to) {
        int dir = from.direction(to);
        int oppositeDir = dir < 4 ? (dir + 4) : (dir - 4);
        int count = 1;

        for (int i = 1; i < 8; i++) {
            Square temp1 = from.moveDest(dir, i);
            Square temp2 = from.moveDest(oppositeDir, i);
            if (temp1 != null && _board[temp1.index()] != EMP) {
                count++;
            }
            if (temp2 != null && _board[temp2.index()] != EMP) {
                count++;
            }
        }
        return count;
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Return and remove the last move made. */
    Move lastLegalMove() {
        return _moves.remove(_moves.size() - 1);
    }

    /** Return a sequence of all legal moves from this position.
     * FIXME*/
    List<Move> legalMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        Move temp = null;
        for (int c = 0; c < BOARD_SIZE; c += 1) {
            for (int r = 0; r < BOARD_SIZE; r += 1) {
                Square from = sq(c, r);
                if (_board[from.index()] == _turn) {
                    for (int dir = 0; dir < 8; dir += 1) {
                        for (Square to = from.moveDest(dir, 1); to != null;
                            to = to.moveDest(dir, 1)) {
                            if (isLegal(from, to)) {
                                temp = Move.mv(from, to);
                                if (_board[to.index()] != EMP
                                        && _turn != _board[to.index()]) {
                                    temp = temp.captureMove();
                                }
                                moves.add(temp);
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    /** Return a sequence of all legal moves from this position.
     * FIXME*/
    List<Move> legalMovesPiece(Piece s) {
        ArrayList<Move> moves = new ArrayList<>();
        Move temp = null;
        for (int c = 0; c < BOARD_SIZE; c += 1) {
            for (int r = 0; r < BOARD_SIZE; r += 1) {
                Square from = sq(c, r);
                if (_board[from.index()] == s) {
                    for (int dir = 0; dir < 8; dir += 1) {
                        for (Square to = from.moveDest(dir, 1); to != null;
                             to = to.moveDest(dir, 1)) {
                            if (isLegal(from, to)) {
                                temp = Move.mv(from, to);
                                if (_board[to.index()] != EMP
                                        && s != _board[to.index()]) {
                                    temp = temp.captureMove();
                                }
                                moves.add(temp);
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP.
     *  FIXME */
    Piece winner() {

        if (!_winnerKnown) {
            if (!piecesContiguous(_turn) && !piecesContiguous(_turn.opposite())) {
                _winner = null;
                _winnerKnown = false;
            }
            if (piecesContiguous(_turn) && piecesContiguous(_turn.opposite())) {
                _winner = _turn.opposite();
                _winnerKnown = true;
            }
            if (piecesContiguous(_turn) && !piecesContiguous(_turn.opposite())) {
                _winner = _turn;
                _winnerKnown = true;
            }
            if (!piecesContiguous(_turn) && piecesContiguous(_turn.opposite())) {
                _winner = _turn.opposite();
                _winnerKnown = true;
            }
        }

        if (_moveLimit == movesMade() && _winner == null) {
            _winner = EMP;
            _winnerKnown = true;
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square.
     *  FIXME*/
    private boolean blocked(Square from, Square to) {
        int direction = from.direction(to);
        int distance = from.distance(to);

        for (int i = 1; i < distance; i++) {
            Square temp = from.moveDest(direction, i);
            if (temp != null && _board[temp.index()] != EMP
                    && (_board[temp.index()] != _board[from.index()])) {
                return true;
            }
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted.
     *  FIXME: CHANGE TO STATIC func() name and STATIC _BOARD */
    public int numContig(Square sq, boolean[][] visited, Piece p) {
        int count = 1;
        visited[sq.col()][sq.row()] = true;
        Square[] adj = adjacent(sq);

        for (int i = 0; i < adj.length; i++) {
            if (_board[adj[i].index()] == p
                    && !visited[adj[i].col()][adj[i].row()]) {
                visited[adj[i].col()][adj[i].row()] = true;
                count += numContig(adj[i], visited, p);
            }
        }
        return count;
    }

    /** Set the values of _whiteRegionSizes and _blackRegionSizes.
     * FIXME: from public to private*/
    public void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();

        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];

        for (int c = 0; c < BOARD_SIZE; c += 1) {
            for (int r = 0; r < BOARD_SIZE; r += 1) {
                Square sq = sq(c, r);
                if (!visited[c][r] && _board[sq.index()] == WP) {
                    int ncWhite = numContig(sq, visited, _board[sq.index()]);
                    _whiteRegionSizes.add(ncWhite);
                } else if (!visited[c][r] && _board[sq.index()] == BP) {
                    int ncBlack = numContig(sq, visited, _board[sq.index()]);
                    _blackRegionSizes.add(ncBlack);
                }
            }
        }

        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }

    /** Return total number of Pieces S in current Board. */
    public int numPieces(Piece s) {
        int total = 0;
        if (s == WP) {
            for (int x: _whiteRegionSizes) {
                total += x;
            }
        } else {
            for (int y: _blackRegionSizes) {
                total += y;
            }
        }
        return total;
    }

    /** Return total number of Capture Moves of Pieces S in current Board.*/
    public int numCapture(Piece s) {
        int total = 0;
        for (Move move: legalMovesPiece(s)) {
            if (move.isCapture()) {
                total += 1;
            }
        }
        return total;
    }

    /** Return total number of Pieces S in secret Area. */
    public int numSecret(Piece s) {
        int count = 0;
        for (int m = 2; m < 6; m++) {
            for (int n = 2; n < 6; n++) {
                Square sq = sq(n, m);
                if (_board[sq.index()] == s) {
                    count++;
                }
            }
        }
        return count;
    }

    /** Return the density (largest reg size) of Pieces S in current Board. */
    public int density(Piece s) {
        return s == WP ? Collections.max(_whiteRegionSizes) :
                Collections.max(_blackRegionSizes);
    }

    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** List of the sizes of continguous clusters of pieces, by color.
     * FIXME: from public to private */
    public final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();
}

package com.example.meshlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F5)
                ) {
                    ChessGame()
                }
            }
        }
    }
}

@Composable
fun ChessGame() {
    var board by remember { mutableStateOf(initializeBoard()) }
    var selectedSquare by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isWhiteTurn by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isWhiteTurn) "White's Turn" else "Black's Turn",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            for (row in 0..7) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0..7) {
                        ChessSquare(
                            piece = board[row][col],
                            isLight = (row + col) % 2 == 0,
                            isSelected = selectedSquare == Pair(row, col),
                            onClick = {
                                handleSquareClick(
                                    row, col, board, selectedSquare,
                                    isWhiteTurn,
                                    onBoardUpdate = { board = it },
                                    onSelectedUpdate = { selectedSquare = it },
                                    onTurnUpdate = { isWhiteTurn = it }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                board = initializeBoard()
                selectedSquare = null
                isWhiteTurn = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Anurag", fontSize = 18.sp)
        }
    }
}

@Composable
fun ChessSquare(
    piece: ChessPiece?,
    isLight: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when {
                    isSelected -> Color.Yellow
                    isLight -> Color(0xFFF0D9B5)
                    else -> Color(0xFFB58863)
                }
            )
            .border(0.5.dp, Color.Black)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getPieceSymbol(piece),
            fontSize = 36.sp,
            textAlign = TextAlign.Center
        )
    }
}

fun handleSquareClick(
    row: Int,
    col: Int,
    board: Array<Array<ChessPiece?>>,
    selectedSquare: Pair<Int, Int>?,
    isWhiteTurn: Boolean,
    onBoardUpdate: (Array<Array<ChessPiece?>>) -> Unit,
    onSelectedUpdate: (Pair<Int, Int>?) -> Unit,
    onTurnUpdate: (Boolean) -> Unit
) {
    if (selectedSquare == null) {
        val piece = board[row][col]
        if (piece != null && piece.isWhite == isWhiteTurn) {
            onSelectedUpdate(Pair(row, col))
        }
    } else {
        val (fromRow, fromCol) = selectedSquare

        if (isValidMove(fromRow, fromCol, row, col, board)) {
            val newBoard = board.map { it.clone() }.toTypedArray()
            newBoard[row][col] = newBoard[fromRow][fromCol]
            newBoard[fromRow][fromCol] = null

            onBoardUpdate(newBoard)
            onTurnUpdate(!isWhiteTurn)
        }

        onSelectedUpdate(null)
    }
}

fun initializeBoard(): Array<Array<ChessPiece?>> {
    val board = Array(8) { Array<ChessPiece?>(8) { null } }

    // Pawns
    for (i in 0..7) {
        board[1][i] = ChessPiece(PieceType.PAWN, false)
        board[6][i] = ChessPiece(PieceType.PAWN, true)
    }

    // Rooks
    board[0][0] = ChessPiece(PieceType.ROOK, false)
    board[0][7] = ChessPiece(PieceType.ROOK, false)
    board[7][0] = ChessPiece(PieceType.ROOK, true)
    board[7][7] = ChessPiece(PieceType.ROOK, true)

    // Knights
    board[0][1] = ChessPiece(PieceType.KNIGHT, false)
    board[0][6] = ChessPiece(PieceType.KNIGHT, false)
    board[7][1] = ChessPiece(PieceType.KNIGHT, true)
    board[7][6] = ChessPiece(PieceType.KNIGHT, true)

    // Bishops
    board[0][2] = ChessPiece(PieceType.BISHOP, false)
    board[0][5] = ChessPiece(PieceType.BISHOP, false)
    board[7][2] = ChessPiece(PieceType.BISHOP, true)
    board[7][5] = ChessPiece(PieceType.BISHOP, true)

    // Queens
    board[0][3] = ChessPiece(PieceType.QUEEN, false)
    board[7][3] = ChessPiece(PieceType.QUEEN, true)

    // Kings
    board[0][4] = ChessPiece(PieceType.KING, false)
    board[7][4] = ChessPiece(PieceType.KING, true)

    return board
}

fun getPieceSymbol(piece: ChessPiece?): String {
    return when (piece?.type) {
        PieceType.KING -> if (piece.isWhite) "♔" else "♚"
        PieceType.QUEEN -> if (piece.isWhite) "♕" else "♛"
        PieceType.ROOK -> if (piece.isWhite) "♖" else "♜"
        PieceType.BISHOP -> if (piece.isWhite) "♗" else "♝"
        PieceType.KNIGHT -> if (piece.isWhite) "♘" else "♞"
        PieceType.PAWN -> if (piece.isWhite) "♙" else "♟"
        null -> ""
    }
}

fun isValidMove(
    fromRow: Int,
    fromCol: Int,
    toRow: Int,
    toCol: Int,
    board: Array<Array<ChessPiece?>>
): Boolean {
    val piece = board[fromRow][fromCol] ?: return false
    val targetPiece = board[toRow][toCol]

    if (targetPiece != null && targetPiece.isWhite == piece.isWhite) return false

    val rowDiff = toRow - fromRow
    val colDiff = toCol - fromCol

    return when (piece.type) {
        PieceType.PAWN -> {
            val dir = if (piece.isWhite) -1 else 1
            val startRow = if (piece.isWhite) 6 else 1

            when {
                colDiff == 0 && rowDiff == dir && targetPiece == null -> true
                colDiff == 0 && rowDiff == 2 * dir && fromRow == startRow &&
                        targetPiece == null && board[fromRow + dir][fromCol] == null -> true
                abs(colDiff) == 1 && rowDiff == dir && targetPiece != null -> true
                else -> false
            }
        }
        PieceType.ROOK -> {
            (rowDiff == 0 || colDiff == 0) && isPathClear(fromRow, fromCol, toRow, toCol, board)
        }
        PieceType.KNIGHT -> {
            (abs(rowDiff) == 2 && abs(colDiff) == 1) ||
                    (abs(rowDiff) == 1 && abs(colDiff) == 2)
        }
        PieceType.BISHOP -> {
            abs(rowDiff) == abs(colDiff) && isPathClear(fromRow, fromCol, toRow, toCol, board)
        }
        PieceType.QUEEN -> {
            (rowDiff == 0 || colDiff == 0 || abs(rowDiff) == abs(colDiff)) &&
                    isPathClear(fromRow, fromCol, toRow, toCol, board)
        }
        PieceType.KING -> {
            abs(rowDiff) <= 1 && abs(colDiff) <= 1
        }
    }
}

fun isPathClear(
    fromRow: Int,
    fromCol: Int,
    toRow: Int,
    toCol: Int,
    board: Array<Array<ChessPiece?>>
): Boolean {
    val rowStep = when {
        toRow > fromRow -> 1
        toRow < fromRow -> -1
        else -> 0
    }
    val colStep = when {
        toCol > fromCol -> 1
        toCol < fromCol -> -1
        else -> 0
    }

    var currentRow = fromRow + rowStep
    var currentCol = fromCol + colStep

    while (currentRow != toRow || currentCol != toCol) {
        if (board[currentRow][currentCol] != null) return false
        currentRow += rowStep
        currentCol += colStep
    }

    return true
}

data class ChessPiece(val type: PieceType, val isWhite: Boolean)

enum class PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
}
/*
 * MIT License
 *
 * Copyright (c) 2026 Silvere Martin-Michiellot, Antigravity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.game.eternity2.solver;

import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.PiecePrimitive;
import org.game.eternity2.io.PuzzleLoaderWriter;

import java.util.*;

/**
 * Monte Carlo Tree Search Solver for Eternity II.
 * Uses UCT (Upper Confidence Bound for Trees) to balance exploration and
 * exploitation.
 * Optimized version using primitive models.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class MCTSSolver implements EternitySolverInterface {

    private static final double EXPLORATION_CONSTANT = Math.sqrt(2);
    private final Random random = new Random();
    private final long[] allPieces;

    public MCTSSolver() {
        this.allPieces = PuzzleLoaderWriter.generateEternity2Pieces();
    }

    @Override
    public BoardPrimitive computeTessellation(BoardPrimitive startingBoard) {
        if (startingBoard == null) {
            startingBoard = new BoardPrimitive(16, 16);
        }
        BoardPrimitive currentBoard = new BoardPrimitive(startingBoard);
        long endTime = System.currentTimeMillis() + 1000;

        while (System.currentTimeMillis() < endTime) {
            int[] nextEmpty = findNextEmpty(currentBoard);
            if (nextEmpty == null) {
                break;
            }
            MCTSNode root = new MCTSNode(null, new BoardPrimitive(currentBoard));
            long stepEnd = Math.min(endTime, System.currentTimeMillis() + 150);
            while (System.currentTimeMillis() < stepEnd) {
                MCTSNode selectedNode = select(root);
                if (!selectedNode.isTerminal()) {
                    expand(selectedNode);
                    if (!selectedNode.children.isEmpty()) {
                        selectedNode = selectedNode.children.get(random.nextInt(selectedNode.children.size()));
                    }
                }
                int score = simulate(selectedNode);
                backpropagate(selectedNode, score);
            }
            MCTSNode bestChild = root.getBestChild();
            if (bestChild != null && bestChild.state != null) {
                currentBoard = bestChild.state;
            } else {
                break;
            }
        }

        return currentBoard;
    }

    private MCTSNode select(MCTSNode node) {
        while (!node.children.isEmpty()) {
            node = node.children.stream()
                    .max(Comparator.comparingDouble(this::calculateUCT))
                    .orElse(node.children.get(0));
        }
        return node;
    }

    private double calculateUCT(MCTSNode node) {
        if (node.visits == 0)
            return Double.MAX_VALUE;
        return (node.score / node.visits)
                + EXPLORATION_CONSTANT * Math.sqrt(Math.log(node.parent.visits) / node.visits);
    }

    private void expand(MCTSNode node) {
        // Find next empty spot on board
        int[] nextPos = findNextEmpty(node.state);
        if (nextPos == null) {
            node.terminal = true;
            return;
        }

        int x = nextPos[0];
        int y = nextPos[1];

        List<Long> unusedTiles = getUnusedTiles(node.state);
        int[] constraints = node.state.getConstraints(x, y);

        for (long tile : unusedTiles) {
            for (int r = 0; r < 4; r++) {
                if (PiecePrimitive.matches(tile, constraints[0], constraints[1], constraints[2], constraints[3])) {
                    BoardPrimitive newState = new BoardPrimitive(node.state);
                    newState.placePiece(x, y, tile);
                    MCTSNode child = new MCTSNode(node, newState);
                    node.children.add(child);
                }
                tile = PiecePrimitive.rotateCW(tile);
            }
        }
        if (node.children.isEmpty()) {
            node.terminal = true;
        }
    }

    private int simulate(MCTSNode node) {
        BoardPrimitive simulationState = new BoardPrimitive(node.state);
        List<Long> available = getUnusedTiles(simulationState);
        Collections.shuffle(available, random);

        int[] nextPos;
        while ((nextPos = findNextEmpty(simulationState)) != null) {
            int x = nextPos[0];
            int y = nextPos[1];
            int[] constraints = simulationState.getConstraints(x, y);
            boolean placed = false;
            for (int i = 0; i < available.size(); i++) {
                long tile = available.get(i);
                for (int r = 0; r < 4; r++) {
                    if (PiecePrimitive.matches(tile, constraints[0], constraints[1], constraints[2], constraints[3])) {
                        simulationState.placePiece(x, y, tile);
                        available.remove(i);
                        placed = true;
                        break;
                    }
                    tile = PiecePrimitive.rotateCW(tile);
                }
                if (placed)
                    break;
            }
            if (!placed) {
                break;
            }
        }
        return simulationState.computeScore();
    }

    private void backpropagate(MCTSNode node, int score) {
        while (node != null) {
            node.visits++;
            node.score += score;
            node = node.parent;
        }
    }

    private List<Long> getUnusedTiles(BoardPrimitive board) {
        Set<Integer> usedIds = new HashSet<>();
        for (long cell : board.getCells()) {
            if (cell != 0) {
                usedIds.add(PiecePrimitive.getId(cell));
            }
        }
        List<Long> unused = new ArrayList<>();
        for (long p : allPieces) {
            if (!usedIds.contains(PiecePrimitive.getId(p))) {
                unused.add(p);
            }
        }
        return unused;
    }

    private int[] findNextEmpty(BoardPrimitive board) {
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (board.isEmpty(x, y))
                    return new int[] { x, y };
            }
        }
        return null;
    }

    private static class MCTSNode {
        MCTSNode parent;
        List<MCTSNode> children = new ArrayList<>();
        BoardPrimitive state;

        int visits = 0;
        double score = 0;
        boolean terminal = false;

        public MCTSNode(MCTSNode parent, BoardPrimitive state) {
            this.parent = parent;
            this.state = state;
        }

        public MCTSNode getBestChild() {
            return children.stream().max(Comparator.comparingInt(n -> n.visits)).orElse(null);
        }

        public boolean isTerminal() {
            return terminal;
        }
    }
}

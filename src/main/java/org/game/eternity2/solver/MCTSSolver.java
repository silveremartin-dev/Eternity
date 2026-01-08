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
        MCTSNode root = new MCTSNode(null, new BoardPrimitive(startingBoard));

        long endTime = System.currentTimeMillis() + 5000; // Run for 5 seconds per move/step (demo)

        while (System.currentTimeMillis() < endTime) {
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

        // Return best child
        MCTSNode bestChild = root.getBestChild();
        return bestChild != null ? bestChild.state : startingBoard;
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
    }

    private int simulate(MCTSNode node) {
        BoardPrimitive simulationState = new BoardPrimitive(node.state);
        int addedScore = 0;
        // Random rollout (simplified)
        for (int i = 0; i < 50; i++) {
            int[] nextPos = findNextEmpty(simulationState);
            if (nextPos == null) {
                addedScore += 100; // Bonus for full fill
                break;
            }
            // In a real simulation, we'd try to place random valid pieces
            addedScore++;
        }
        return (int) (node.state.computeScore() + addedScore);
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

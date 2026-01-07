package org.game.eternity2.client;

import org.game.eternity2.elements.AbstractEternityBoard;
import org.game.eternity2.elements.EternityBoardInterface;
import org.game.eternity2.elements.EternityTileInterface;

import java.util.*;

/**
 * Monte Carlo Tree Search Solver for Eternity II.
 * Uses UCT (Upper Confidence Bound for Trees) to balance exploration and
 * exploitation.
 */
public class MCTSSolver implements EternitySolverInterface {

    private static final int SIMULATION_LIMIT = 1000;
    private static final double EXPLORATION_CONSTANT = Math.sqrt(2);
    private final Random random = new Random();

    @Override
    public AbstractEternityBoard computeTessellation(EternityBoardInterface startingBoard) {
        MCTSNode root = new MCTSNode(null, startingBoard.cloneBoard(), null);

        // Map remaining tiles
        // In a real implementation, we'd need to track which tiles are used in each
        // node carefully.
        // For this skeleton, we assume the board state carries the used tiles
        // information (implicitly).

        long endTime = System.currentTimeMillis() + 5000; // Run for 5 seconds per move/step (demo)

        while (System.currentTimeMillis() < endTime) {
            MCTSNode distinctNode = select(root);
            if (!distinctNode.isTerminal()) {
                expand(distinctNode);
                if (!distinctNode.children.isEmpty()) {
                    distinctNode = distinctNode.children.get(random.nextInt(distinctNode.children.size()));
                }
            }
            int score = simulate(distinctNode);
            backpropagate(distinctNode, score);
        }

        // Return best child
        MCTSNode bestChild = root.getBestChild();
        return bestChild != null ? bestChild.state : (AbstractEternityBoard) startingBoard;
    }

    private MCTSNode select(MCTSNode node) {
        while (!node.children.isEmpty()) {
            // If any child is unvisited/not expanded?
            // Standard UCT: Select best UCT child
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

        List<EternityTileInterface> unusedTiles = getUnusedTiles(node.state);

        for (EternityTileInterface tile : unusedTiles) {
            for (int r = 0; r < 4; r++) {
                // Try move
                // Note: verifyMatch needs to be available or we simulate setTileAt return
                // Assuming setTileAt checks validity
                AbstractEternityBoard newState = (AbstractEternityBoard) node.state.cloneBoard();
                if (newState.setTileAt(x, y, tile)) {
                    MCTSNode child = new MCTSNode(node, newState, tile);
                    node.children.add(child);
                }
                tile.rotateClockwise();
            }
        }
    }

    private int simulate(MCTSNode node) {
        AbstractEternityBoard simulationState = (AbstractEternityBoard) node.state.cloneBoard();
        int score = 0;
        // Random rollout
        for (int i = 0; i < 50; i++) { // limited depth
            int[] nextPos = findNextEmpty(simulationState);
            if (nextPos == null) {
                score += 100; // Bonus for full fill
                break;
            }
            // Pick random tile
            // This is simplified; getting unused tiles strictly is expensive
            score++;
        }
        return score;
    }

    private void backpropagate(MCTSNode node, int score) {
        while (node != null) {
            node.visits++;
            node.score += score;
            node = node.parent;
        }
    }

    // Helper to find unused tiles - in real impl, optimize this!
    private List<EternityTileInterface> getUnusedTiles(EternityBoardInterface board) {
        return new ArrayList<>(board.getMissingTiles()); // Naive
    }

    private int[] findNextEmpty(EternityBoardInterface board) {
        for (int y = 0; y < board.getYBoardSize(); y++) {
            for (int x = 0; x < board.getXBoardSize(); x++) {
                if (board.getTileAt(x, y) == null)
                    return new int[] { x, y };
            }
        }
        return null;
    }

    private static class MCTSNode {
        MCTSNode parent;
        List<MCTSNode> children = new ArrayList<>();
        AbstractEternityBoard state;
        EternityTileInterface move; // Tile placed to reach this state
        int visits = 0;
        double score = 0;
        boolean terminal = false;

        public MCTSNode(MCTSNode parent, AbstractEternityBoard state, EternityTileInterface move) {
            this.parent = parent;
            this.state = state;
            this.move = move;
        }

        public MCTSNode getBestChild() {
            return children.stream().max(Comparator.comparingInt(n -> n.visits)).orElse(null);
        }

        public boolean isTerminal() {
            return terminal;
        }
    }
}

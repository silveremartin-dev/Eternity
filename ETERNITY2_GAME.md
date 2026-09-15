# Eternity II - The Mathematical Puzzle Challenge

**Authors:** Silvère Martin-Michiellot, Antigravity (Google DeepMind)

---

## 🧩 The Game & Historical Context

**Eternity II** is an edge-matching combinatorial puzzle released commercially in 2007 as the successor to the original Eternity puzzle. The objective is to place 256 square pieces into a $16 \times 16$ grid such that all adjacent tile edges share matching patterns.

### The Unclaimed Prize
A $2,000,000 prize was offered for the first verified complete solution before December 31, 2010. The prize expired unclaimed, and the puzzle remains one of the preeminent open benchmark problems in combinatorial optimization.

---

## 📐 Rules & Invariants

### 1. The Grid
- **Grid Dimensions:** $16 \times 16$ (256 cells).
- **Pieces:** 256 unique square tiles.
- **Edges per Tile:** 4 edges (Top, Right, Bottom, Left), each with a specific pattern/color integer ID ($0 \le id \le 22$).

### 2. Matching Rules
- **Inner Edges:** Adjacent edges of neighboring tiles must share identical pattern IDs.
- **Border Edges:** The outer perimeter of the grid must match the neutral border pattern (Pattern ID = 0).
- **Orientation:** Tiles can be rotated in 4 discrete orientations ($0^\circ, 90^\circ, 180^\circ, 270^\circ$). Tiles cannot be flipped (non-chiral).
- **Max Theoretical Score:** For a board of size $W \times H$:
  $$\text{Max Score} = (W - 1) \cdot H + W \cdot (H - 1) + 2W + 2H$$
  For $16 \times 16$, the maximum score is $15 \cdot 16 + 16 \cdot 15 + 32 + 32 = 480 + 64 = 544$ matched edges.

### 3. Piece Topology
1. **Corner Pieces (4):** Two adjacent border edges (Pattern 0). Placed strictly in the 4 corners: $(0,0), (15,0), (0,15), (15,15)$.
2. **Edge Pieces (56):** Exactly one border edge (Pattern 0). Placed strictly along the outer perimeter.
3. **Inner Pieces (196):** Zero border edges. Placed in the $14 \times 14$ interior grid.

---

## 🔢 Combinatorial Complexity

- **Unconstrained Permutations:** $256! \approx 8.5 \times 10^{506}$
- **Rotational Variations:** $4^{256} \approx 1.3 \times 10^{154}$
- **Total Search Space:** $\approx 1.1 \times 10^{661}$ configurations.

Unlike puzzles with fast local constraint propagation (such as Sudoku), Eternity II exhibits slow constraint decay: a candidate board may appear valid for 240+ pieces before hitting an irrecoverable deadlock, requiring deep backtracking.

---

## 💻 Algorithmic Solutions Implemented

1. **Iterative MCV Backtracking (`EternitySolverEngine`):**
   - Place pieces in spots with the fewest valid candidates.
   - `NeighborIndex` and `GlobalPruner` achieve $O(1)$ constraint evaluation.
   - Fixed clues locking (`isFixed[]`) ensures official hints are respected.
2. **Monte Carlo Tree Search (`MCTSSolver`):**
   - Balances exploration and exploitation via UCT.
   - Stochastic rollout on remaining tile pools.
3. **Hardware GPU Acceleration (`GPUEternitySolver` / TornadoVM):**
   - Batch parallel verification on GPU compute cores.
4. **Stochastic Refinement (`StochasticRefinement`):**
   - Simulated annealing using unit rotations and pairwise tile swaps.

---

© 2026 Silvère Martin-Michiellot & Antigravity

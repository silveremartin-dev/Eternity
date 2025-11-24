# Eternity II - The Puzzle Challenge

## 🧩 The Game

**Eternity II** is an edge-matching puzzle released in 2007. It is the successor to the original Eternity puzzle. The challenge was to place 256 square pieces into a 16x16 grid, matching the patterns on their edges.

### The Prize
A prize of **$2 million** was offered for the first complete solution. The deadline passed on December 31, 2010, without a winner. To this day, a complete solution remains one of the "holy grails" of computational puzzles.

## 📐 Mechanics & Rules

### 1. The Grid
- **Size**: 16x16 grid (256 cells).
- **Pieces**: 256 unique square tiles.
- **Edges**: Each tile has 4 edges, each with a specific pattern/color.

### 2. Matching Rules
- **Inner Edges**: Adjacent edges of neighboring tiles must match perfectly (same pattern/color).
- **Border Edges**: The outer edges of the grid (border) must match the specific "border pattern" (usually grey).
- **Orientation**: Tiles can be rotated 0°, 90°, 180°, or 270°. They cannot be flipped.

### 3. The Pieces
There are three types of pieces:
1.  **Corner Pieces** (4): Two adjacent grey sides. Must be placed in the 4 corners.
2.  **Edge Pieces** (56): One grey side. Must be placed on the perimeter.
3.  **Inner Pieces** (196): No grey sides. Placed in the 14x14 inner square.

## 🔢 Complexity

The combinatorial explosion of Eternity II is staggering, making brute-force impossible.

- **Total Permutations**: If we just shuffle 256 pieces: $256! \approx 8.5 \times 10^{506}$.
- **Rotations**: Each piece has 4 orientations. $4^{256} \approx 1.3 \times 10^{154}$.
- **Constraints**: The edge-matching constraints drastically reduce the search space, but it remains astronomically large.

### Why is it so hard?
Unlike Sudoku or N-Queens, local constraints propagate very slowly. You can fill 90% of the board and realize the last few pieces don't fit, requiring deep backtracking.

## 💻 Computational Approach

Our project aims to solve (or approximate) this puzzle using a high-performance distributed architecture:

1.  **Backtracking with Pruning**: The core algorithm tries to place pieces one by one.
2.  **Heuristics**:
    - **Most Constrained First**: Place pieces in spots with the fewest valid options.
    - **Look-ahead**: Verify if placing a piece makes a future spot impossible.
3.  **Parallelism**:
    - **Distributed Solving**: Splitting the search tree across multiple servers/workers.
    - **GPU Acceleration**: Using `EternityKernel` to check thousands of candidates in parallel.
4.  **Data Structures**:
    - **Constraint Cache**: O(1) lookup to find which pieces match a specific edge pattern.
    - **Bitmasks**: Representing edge patterns as integers for fast comparison.

## 🏆 Current Best Known Solutions
While the full 256-piece puzzle is unsolved, the community tracks "best partial solutions" (maximum number of matching edges or placed pieces).

*This project serves as a playground for advanced Java concurrency (Virtual Threads), distributed systems (Redis/K8s), and GPU computing (TornadoVM).*

# Multidimensional Spatial Indexing & Query Processing (R*-Tree)

A Java multidimensional spatial indexing engine implementing an on-disk
**R\*-Tree**, alongside baseline linear scan implementations for spatial range
and skyline operations.

---

## Overview

This project provides a spatial indexing framework tailored for
multidimensional datasets. It supports both dynamic operations (insertion,
deletion) and STR bulk-loading, and compares indexing efficiency against
sequential scans for spatial range queries and multi-criteria skyline queries.

Index structures utilize block-level persistence, managing disk blocks via an
`IndexFileHandler` with node serialization.

---

## Key Features

### 1. R*-Tree Spatial Index

- **Optimized Tree Construction:**
  - Implements the complete R\*-Tree split heuristic minimizing boundary
    margins, area, and overlap.
  - Forced reinsertion policy (`REINSERT_P = 0.3`) upon node overflow to
    dynamically rebalance and avoid premature splits.
  - Subtree selection targeting minimal overlap enlargement for leaf nodes
    and minimum area enlargement for internal nodes.
- **Bulk Loading (STR Algorithm):**
  - Bottom-up packing using the **Sort-Tile-Recursive (STR)** algorithm.
  - Recursively tiles and clusters data points and internal nodes across
    dimensions.
- **Dynamic Deletion:**
  - Point deletion with hierarchical tree adjustment and underflow
    detection.

### 2. Query Processing Algorithms

- **Range Queries:**
  - **R\*-Tree Hierarchical Range Search:** Recursively prunes bounding
    boxes that do not intersect query bounds.
  - **Linear Scan Range Filter:** Baseline sequential scan filtering across
    arbitrary dimension constraints.
- **Skyline Queries (Pareto Optima / Non-Dominated Points):**
  - **Branch-and-Bound Skyline (BBS):** Progressive best-first search
    traversing the R\*-Tree via a priority queue ordered by coordinate sums
    (min-sum priority) and pruning dominated MBRs on the fly.
  - **Block Nested Loop (BNL):** In-memory sequential comparison baseline
    evaluating pairwise Pareto dominance.

---

## Architecture & Component Overview

| Component                  | Responsibility                                       |
| :------------------------- | :--------------------------------------------------- |
| `RStarTree.java`           | Core R\*-Tree indexing engine (insert, split, STR).  |
| `Node.java`                | Node representation managing block IDs and entries.  |
| `Entry.java`               | Associative record holding an MBR and block pointer. |
| `Rectangle.java`           | N-dimensional MBR supporting geometry and volume.    |
| `Point.java`               | Multidimensional point entity with distance metrics. |
| `QueryDimensions.java`     | Dimensional metadata abstraction mapping columns.    |
| `RStarTreeRangeQuery.java` | Spatial range evaluation using R\*-Tree index.       |
| `RangeQuery.java`          | Sequential unindexed range query baseline.           |
| `Skyline_BBS_Query.java`   | BBS skyline algorithm using R\*-Tree pruning.        |
| `Skyline_BNL_Query.java`   | Block Nested Loop skyline computation baseline.      |

---

## Data Format

Dataset files (e.g. CSV) expect lines formatted according to the
`DataFileHandler` configuration:

```text
<id>,<name>,<coord_1>,<coord_2>,...,<coord_n>,<postcode>,<housenumber>
```

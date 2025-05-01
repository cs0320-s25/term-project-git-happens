/**
 * A React component that renders a table from a 2D array of strings.
 * Supports keyboard navigation using Ctrl+T to activate and WASD keys for movement.
 *
 * @param {TableProps} props - The properties for the Table component.
 * @param {string[][]} props.data - The 2D array of strings representing the table data.
 * @param {boolean} props.hasHeader - A boolean indicating whether the table has a header row.
 *
 * @returns {JSX.Element} A React table component with keyboard navigation support.
 * @remarks
 * The table supports keyboard navigation using Ctrl+T to activate navigation mode.
 * In navigation mode, users can use the WASD keys to move between cells and ESC to exit navigation mode.
 * The component uses React refs to manage focus and keyboard events efficiently.
 */
import React, { useState, useRef, useEffect, KeyboardEvent } from "react";

interface TableProps {
  data: String[][];
  hasHeader: boolean;
}

export function Table(props: TableProps) {
  // Current focused cell position
  const [focusPosition, setFocusPosition] = useState<{
    row: number;
    col: number;
  }>({ row: 0, col: 0 });
  const [navigationActive, setNavigationActive] = useState(false);

  // Ref for the table element
  const tableRef = useRef<HTMLTableElement>(null);

  // Refs to access the table cells
  const cellRefs = useRef<HTMLElement[][]>([]);

  // Initialize the cell refs array based on data dimensions
  useEffect(() => {
    cellRefs.current = Array(props.data.length)
      .fill(0)
      .map(() => Array(props.data[0]?.length || 0).fill(null));
  }, [props.data]);

  // Setup global key listener for Ctrl+T
  useEffect(() => {
    const handleGlobalKeyDown = (e: globalThis.KeyboardEvent) => {
      // Check for Ctrl+T
      if (e.ctrlKey && e.key.toLowerCase() === "t" && !navigationActive) {
        e.preventDefault(); // Prevent browser's new tab behavior

        // Activate navigation and focus first cell immediately
        setNavigationActive(true);

        // Make sure to wait for state update before focusing
        setTimeout(() => {
          if (cellRefs.current[0]?.[0]) {
            cellRefs.current[0][0].focus();
            setFocusPosition({ row: 0, col: 0 });
          }
        }, 0);
      }
    };

    // Add the event listener to the document
    document.addEventListener("keydown", handleGlobalKeyDown);

    // Cleanup
    return () => {
      document.removeEventListener("keydown", handleGlobalKeyDown);
    };
  }, [navigationActive]);

  // Handle WASD navigation within the table
  const handleKeyDown = (
    e: React.KeyboardEvent<HTMLElement>,
    rowIndex: number,
    colIndex: number
  ) => {
    // For Tab key, always let the default browser behavior work
    if (e.key === "Tab") {
      e.preventDefault();
      setNavigationActive(false);
      // Remove focus from the table entirely
      const fileInputBox = document.getElementById("filepath input");
      if (fileInputBox) {
        fileInputBox.focus();
      }
      return;
    }

    // Only process WASD navigation if navigation mode is active
    if (!navigationActive) return;

    let newRow = rowIndex;
    let newCol = colIndex;
    let handled = false;

    switch (e.key.toLowerCase()) {
      case "w": // Up
        e.preventDefault();
        newRow = Math.max(0, rowIndex - 1);
        handled = true;
        break;
      case "s": // Down
        e.preventDefault();
        newRow = Math.min(props.data.length - 1, rowIndex + 1);
        handled = true;
        break;
      case "a": // Left
        e.preventDefault();
        newCol = Math.max(0, colIndex - 1);
        handled = true;
        break;
      case "d": // Right
        e.preventDefault();
        newCol = Math.min(props.data[0].length - 1, colIndex + 1);
        handled = true;
        break;
      case "escape": // Exit table navigation
        e.preventDefault();
        setNavigationActive(false);
        // Remove focus from the table entirely
        const fileInputBox = document.getElementById("filepath input");
        if (fileInputBox) {
          fileInputBox.focus();
        }
        return;
      default:
        return;
    }

    if (handled) {
      // Update focus position
      setFocusPosition({ row: newRow, col: newCol });

      // Focus the new cell
      const targetCell = cellRefs.current[newRow]?.[newCol];
      if (targetCell) {
        targetCell.focus();
      }
    }
  };

  // Global keydown handler for when navigation is inactive
  useEffect(() => {
    const handleInactiveKeypress = (e: globalThis.KeyboardEvent) => {
      // If navigation is inactive, prevent WASD from entering table navigation
      if (!navigationActive) {
        if (["w", "a", "s", "d"].includes(e.key.toLowerCase())) {
          // Do not activate table navigation
          return;
        }
      }
    };

    document.addEventListener("keydown", handleInactiveKeypress);
    return () => {
      document.removeEventListener("keydown", handleInactiveKeypress);
    };
  }, [navigationActive]);

  return (
    <div>
      {/* Visual indicator that table is in navigation mode */}
      {navigationActive && (
        <div
          aria-live="polite"
          aria-label="keyboard table navigation instructions"
          role="status"
          style={{ marginBottom: "8px", fontSize: "1rem" }}
        >
          Table navigation active. Use W to move up, A to move left, S to move
          down, D to move right. Press ESC before switching to another dataset.
        </div>
      )}

      <table
        className="table"
        aria-label="table"
        role="table"
        aria-description="Table showing data"
        ref={tableRef}
      >
        <tbody>
          {props.data.map((row, rowIndex) => (
            <tr key={String(rowIndex)}>
              {row.map((cell, colIndex) => {
                const isFocused =
                  navigationActive &&
                  rowIndex === focusPosition.row &&
                  colIndex === focusPosition.col;

                return props.hasHeader && rowIndex === 0 ? (
                  <th
                    aria-label={"Header: " + cell}
                    key={"header" + String(colIndex)}
                    role="columnheader"
                    tabIndex={-1}
                    ref={(el) => {
                      if (el) {
                        if (!cellRefs.current[rowIndex]) {
                          cellRefs.current[rowIndex] = [];
                        }
                        cellRefs.current[rowIndex][colIndex] = el;
                      }
                    }}
                    onKeyDown={(e) => handleKeyDown(e, rowIndex, colIndex)}
                    onFocus={() => {
                      if (navigationActive) {
                        setFocusPosition({ row: rowIndex, col: colIndex });
                      }
                    }}
                  >
                    {cell}
                  </th>
                ) : (
                  <td
                    aria-label={"Row: " + rowIndex + ", Value: " + cell}
                    key={String(rowIndex) + String(colIndex)}
                    role="cell"
                    tabIndex={-1}
                    ref={(el) => {
                      if (el) {
                        if (!cellRefs.current[rowIndex]) {
                          cellRefs.current[rowIndex] = [];
                        }
                        cellRefs.current[rowIndex][colIndex] = el;
                      }
                    }}
                    onKeyDown={(e) => handleKeyDown(e, rowIndex, colIndex)}
                    onFocus={() => {
                      if (navigationActive) {
                        setFocusPosition({ row: rowIndex, col: colIndex });
                      }
                    }}
                  >
                    {cell}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

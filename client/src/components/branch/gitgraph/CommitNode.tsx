import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../styles/branch.css";
import "../../../styles/main.css";
import type { CommitData, BranchData } from "../../App";

/**
 * Props for the CommitNode component.
 */
interface CommitNodeProps {
  /** Object containing commit data, coordinates, and external parent status. */
  commit: {
    commit: CommitData;
    x: number;
    y: number;
    hasExternalParent: boolean;
  };
  /** Function to toggle commit selection on click. */
  onToggleSelect: (
    commit: CommitNodeProps["commit"],
    clickX: number,
    clickY: number
  ) => void;
}

/**
 * Represents a commit node in the graph, showing the commit hash and message.
 * The commit node is clickable, with keyboard navigation support (Arrow keys, Enter, and Space).
 *
 * @param {CommitNodeProps} props - The props for the CommitNode component.
 * @param {boolean} isFocused - Indicates if the node is currently focused for keyboard navigation.
 * @param {(dir: "up" | "down" | "left" | "right") => void} onArrowNavigate - A function for handling arrow key navigation.
 * @returns {JSX.Element} The commit node element with interactive functionality.
 */
export function CommitNode({
  commit,
  onToggleSelect,
  isFocused,
  onArrowNavigate,
}: CommitNodeProps & {
  /** Indicates if the commit node is focused. */
  isFocused: boolean;
  /** A function to navigate between commit nodes using arrow keys. */
  onArrowNavigate: (dir: "up" | "down" | "left" | "right") => void;
}) {
  const ref = useRef<SVGGElement>(null);

  // Focus the commit node element when it's focused.
  useEffect(() => {
    if (isFocused && ref.current) {
      ref.current.focus();
    }
  }, [isFocused]);

  // Handle commit selection on click, recording the click's position.
  const handleSelect = (clickX: number, clickY: number) => {
    onToggleSelect(commit, clickX, clickY);
  };

  // Create the ARIA label for the commit node, displaying commit hash and message.
  const ariaLabel = `Commit ${commit.commit.commit_hash.slice(
    0,
    7
  )}, message: ${commit.commit.message}`;

  return (
    <g
      ref={ref}
      className="commit-node"
      tabIndex={0}
      role="button"
      aria-label={ariaLabel}
      onKeyDown={(e) => {
        // Handle keyboard navigation using arrow keys.
        if (e.key === "ArrowUp") onArrowNavigate("up");
        else if (e.key === "ArrowDown") onArrowNavigate("down");
        else if (e.key === "ArrowLeft") onArrowNavigate("left");
        else if (e.key === "ArrowRight") onArrowNavigate("right");
        else if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          onToggleSelect(commit, commit.x, commit.y);
        }
      }}
      onClick={(e) => {
        // Handle commit node click, calculating click position relative to SVG.
        const svgRect = e.target.ownerSVGElement?.getBoundingClientRect();
        const clickX = e.clientX - (svgRect?.left || 0);
        const clickY = e.clientY - (svgRect?.top || 0);
        handleSelect(clickX, clickY);
      }}
    >
      <circle
        cx={commit.x}
        cy={commit.y}
        r={10}
        stroke={commit.hasExternalParent ? "#333" : "none"}
        strokeWidth={commit.hasExternalParent ? 2 : 0}
      />
      <text
        x={commit.x + 15}
        y={commit.y + 4}
        fontSize={12}
        fontFamily="monospace"
      >
        {commit.commit.message.slice(0, 20)}...
      </text>
    </g>
  );
}

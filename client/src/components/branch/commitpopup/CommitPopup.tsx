import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import type { CommitData, BranchData } from "../../App";

interface CommitPopupProps {
  commit: CommitData;
  x: number;
  y: number;
  maxWidth: number;
  maxHeight: number;
  onClose: (hash: string) => void;
}

export function CommitPopup(props: CommitPopupProps) {
  const { commit, x, y, maxWidth, maxHeight, onClose } = props;

  return (
    <div
      style={{
        position: "absolute",
        left: x + 10, // Still position to the right of the node
        top: y - maxHeight / 2, // Center vertically on the node
        background: "gray",
        border: "1px solid #ccc",
        borderRadius: "8px",
        padding: "10px",
        zIndex: 1000,
        boxShadow: "0 4px 10px rgba(0, 0, 0, 0.2)",
        maxWidth: `${maxWidth}px`,
        maxHeight: `${maxHeight}px`,
        overflow: "auto",
      }}
    >
      <h4 style={{ margin: "0 0 6px" }}>Commit: {commit.commit_hash}</h4>
      <div style={{ fontSize: "12px", marginBottom: "8px" }}>
        {commit.message}
      </div>
      <pre style={{ fontSize: "11px", whiteSpace: "pre-wrap" }}>
        {commit.contents.join("\n")}
      </pre>
      <button onClick={() => onClose(commit.commit_hash)}>Close</button>
    </div>
  );
}

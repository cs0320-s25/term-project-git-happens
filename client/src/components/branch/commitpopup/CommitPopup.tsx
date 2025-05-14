import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import type { CommitData, BranchData } from "../../App";
import "../../../styles/branch.css";

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
      <div className="commit-contents">
        {commit.contents.map((file, fileIdx) => (
          <div
            key={fileIdx}
            style={{
              display: "flex",
            }}
          >
            <div className="file-contents">
              <div style={{ fontSize: "12px", marginBottom: "8px" }}>
                {file.fileName}
              </div>
              {file.fileContents.map((img, idx) => (
                <img
                  key={idx}
                  src={img.imgStr}
                  alt={img.imgName || `Ingredient ${idx + 1}`}
                  style={{
                    width: maxWidth - 10,
                    top: `${idx * -45}px`, // shift each layer down slightly
                    zIndex: 1000 - idx,
                    position: "relative",
                  }}
                />
              ))}
            </div>
          </div>
        ))}
      </div>
      <button onClick={() => onClose(commit.commit_hash)}>Close</button>
    </div>
  );
}

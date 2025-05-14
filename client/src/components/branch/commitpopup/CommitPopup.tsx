import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import type { CommitData, BranchData } from "../../App";
import "../../../styles/branch.css";

/**
 * Props for the CommitPopup component.
 */
interface CommitPopupProps {
  /** The commit data to display */
  commit: CommitData;
  /** X coordinate for popup positioning */
  x: number;
  /** Y coordinate for popup positioning */
  y: number;
  /** Maximum width for the popup */
  maxWidth: number;
  /** Maximum height for the popup */
  maxHeight: number;
  /** Function to call when closing the popup */
  onClose: (hash: string) => void;
}

/**
 * A popup component that displays detailed commit information including
 * the commit hash, message, and a list of files and their visual contents.
 *
 * @param props - CommitPopupProps
 * @returns JSX.Element
 */
export function CommitPopup(props: CommitPopupProps) {
  const { commit, x, y, maxWidth, maxHeight, onClose } = props;

  return (
    <div
      className="popup-container"
      role="dialog"
      aria-labelledby={`commit-title-${commit.commit_hash}`}
      aria-describedby={`commit-description-${commit.commit_hash}`}
      aria-modal="true"
      style={{
        position: "absolute",
        left: x + 10,
        top: y - maxHeight / 2,
        width: `${maxWidth}px`,
        maxHeight: `${maxHeight}px`,
      }}
    >
      {/* Commit Title */}
      <h4
        id={`commit-title-${commit.commit_hash}`}
        style={{ margin: "0 0 6px" }}
      >
        Commit: {commit.commit_hash}
      </h4>

      {/* Commit Message */}
      <div
        id={`commit-description-${commit.commit_hash}`}
        style={{ fontSize: "12px", marginBottom: "8px" }}
      >
        {commit.message}
      </div>

      {/* Display each file and its image contents */}
      <div className="commit-contents">
        {commit.contents.map((file, fileIdx) => {
          const imageHeight = 50;
          const overlap = 40;
          const visibleHeight =
            imageHeight +
            (file.fileContents.length - 1) * (imageHeight - overlap);

          return (
            <section key={fileIdx} aria-label={`File: ${file.fileName}`}>
              <header style={{ fontSize: "12px", marginTop: 5 }}>
                {file.fileName}
              </header>
              <div
                className="file-images"
                style={{
                  position: "relative",
                  height: `${visibleHeight}px`,
                  overflow: "visible",
                }}
              >
                {file.fileContents.map((img, idx) => (
                  <img
                    key={idx}
                    src={img.imgStr}
                    alt={img.imgName || `Ingredient ${idx + 1}`}
                    aria-label={`Image ${idx + 1} of ${
                      file.fileContents.length
                    }`}
                    style={{
                      width: maxWidth - 25,
                      height: imageHeight,
                      position: "absolute",
                      top: `${idx * (imageHeight - overlap)}px`,
                      left: 5,
                      zIndex: 1000 - idx,
                    }}
                  />
                ))}
              </div>
            </section>
          );
        })}
      </div>

      {/* Close Button */}
      <button
        onClick={() => onClose(commit.commit_hash)}
        aria-label="Close commit details"
      >
        Close
      </button>
    </div>
  );
}

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
      <h4
        id={`commit-title-${commit.commit_hash}`}
        style={{ margin: "0 0 6px" }}
      >
        Commit: {commit.commit_hash}
      </h4>

      <div
        id={`commit-description-${commit.commit_hash}`}
        style={{ fontSize: "12px", marginBottom: "8px" }}
      >
        {commit.message}
      </div>

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
                    style={{
                      width: maxWidth - 25,
                      height: imageHeight,
                      position: "absolute",
                      top: `${idx * (imageHeight - overlap)}px`,
                      left: 5,
                      zIndex: 1000 - idx,
                    }}
                    aria-label={`Image ${idx + 1} of ${
                      file.fileContents.length
                    }`}
                  />
                ))}
              </div>
            </section>
          );
        })}
      </div>

      <button
        onClick={() => onClose(commit.commit_hash)}
        aria-label="Close commit details"
      >
        Close
      </button>
    </div>
  );
}

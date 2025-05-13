import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../../styles/game.css";

interface BranchPopupProps {
  show: boolean;
  onClose: () => void;
  onSelect: (branchType: "fries" | "burger") => void;
}

export function BranchCreationPopup(props: BranchPopupProps) {
  if (!props.show) return null;

  return (
    <div className="branch-creation-popup">
      <h2>Choose Your Branch Type</h2>
      <p>
        Do you want your branch to be a <strong>fries</strong> branch or a{" "}
        <strong>burger</strong> branch?
      </p>
      <div className="flex flex-col gap-3">
        <button onClick={() => props.onSelect("fries")}>Fries Branch</button>
        <button onClick={() => props.onSelect("burger")}>Burger Branch</button>
        <button onClick={props.onClose}>Cancel</button>
      </div>
    </div>
  );
}

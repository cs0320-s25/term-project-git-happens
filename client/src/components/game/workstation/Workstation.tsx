import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../styles/game.css";
import { IngredientImage, BranchType } from "../Game";
import { Terminal } from "./terminal/Terminal";
import type { CommitData, BranchData } from "../../App";
import { plate } from "../../../assets/images";
import { BranchCreationPopup } from "../workstation/branchcreationpopup/BranchCreationPopup";

interface WorkstationProps {
  selectedWorkstation: 1 | 2 | 3 | null;
  setSelectedWorkstation: Dispatch<SetStateAction<1 | 2 | 3 | null>>;
  workstation1Items: IngredientImage[];
  setWorkstation1Items: Dispatch<SetStateAction<IngredientImage[]>>;
  workstation2Items: IngredientImage[];
  setWorkstation2Items: Dispatch<SetStateAction<IngredientImage[]>>;
  workstation3Items: IngredientImage[];
  setWorkstation3Items: Dispatch<SetStateAction<IngredientImage[]>>;
  plate1Items: IngredientImage[];
  setPlate1Items: Dispatch<SetStateAction<IngredientImage[]>>;
  plate2Items: IngredientImage[];
  setPlate2Items: Dispatch<SetStateAction<IngredientImage[]>>;
  plate3Items: IngredientImage[];
  setPlate3Items: Dispatch<SetStateAction<IngredientImage[]>>;
  branchData: {
    commits: CommitData[];
    branches: BranchData[];
  };
  setBranchData: Dispatch<
    SetStateAction<{
      commits: CommitData[];
      branches: BranchData[];
    }>
  >;
  currentBranch: string;
  setCurrentBranch: Dispatch<SetStateAction<string>>;
  branchTypes: BranchType[];
  setBranchTypes: Dispatch<SetStateAction<BranchType[]>>;
}

export function Workstation(props: WorkstationProps) {
  const [textInput, setTextInput] = useState<string>("");
  const [textDisplay, setTextDisplay] = useState<string>("");

  function handleTextInput() {
    setTextDisplay(textInput);
    setTextInput("");
  }

  function handleRemoveItem(workstation: 1 | 2 | 3, imgName: string) {
    const update = (items: IngredientImage[]) =>
      items.filter((item) => item.imgName !== imgName);

    if (workstation === 1) {
      props.setWorkstation1Items(update);
    } else if (workstation === 2) {
      props.setWorkstation2Items(update);
    } else if (workstation === 3) {
      props.setWorkstation3Items(update);
    }
  }

  const workstationRefs = [
    useRef<HTMLDivElement>(null),
    useRef<HTMLDivElement>(null),
    useRef<HTMLDivElement>(null),
  ];

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      const target = e.target as HTMLElement;

      const isTyping =
        target.tagName === "INPUT" ||
        target.tagName === "TEXTAREA" ||
        target.isContentEditable;

      if (isTyping) return;

      if (e.shiftKey) {
        switch (e.code) {
          case "Digit1":
            props.setSelectedWorkstation(1);
            workstationRefs[0].current?.focus();
            break;
          case "Digit2":
            props.setSelectedWorkstation(2);
            workstationRefs[1].current?.focus();
            break;
          case "Digit3":
            props.setSelectedWorkstation(3);
            workstationRefs[2].current?.focus();
            break;
        }
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [props.setSelectedWorkstation]);

  const [showPopup, setShowPopup] = useState(false);
  const [newBranch, setNewBranch] = useState<string>("");

  function handleBranchSelect(type: "fries" | "burger") {
    console.log("User selected:", type);
    props.setBranchTypes((prev) => [
      ...prev,
      { branchName: newBranch, branchType: type },
    ]);
    setShowPopup(false);
  }

  return (
    <div className="workstation-container">
      <p>Workstation</p>
      {showPopup ? (
        <BranchCreationPopup
          show={true}
          onClose={() => setShowPopup(false)}
          onSelect={(type) => handleBranchSelect(type)}
        />
      ) : (
        <div className="workstation-background">
          {[1, 2, 3].map((wsNum) => {
            const items =
              wsNum === 1
                ? props.workstation1Items
                : wsNum === 2
                ? props.workstation2Items
                : props.workstation3Items;

            return (
              <div
                key={wsNum}
                ref={workstationRefs[wsNum - 1]}
                className={`workstation-section workstation${wsNum} ${
                  props.selectedWorkstation === wsNum ? "selected" : ""
                }`}
                tabIndex={0}
                role="button"
                aria-label={`Select workstation ${wsNum}`}
                onClick={() => props.setSelectedWorkstation(wsNum as 1 | 2 | 3)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    props.setSelectedWorkstation(wsNum as 1 | 2 | 3);
                  }
                }}
              >
                {items.map((ing, i) => (
                  <img
                    key={ing.imgName}
                    src={ing.imgStr}
                    tabIndex={0}
                    role="button"
                    aria-label={`Remove ingredient ${ing.imgName}`}
                    onClick={(e) => {
                      e.stopPropagation();
                      handleRemoveItem(wsNum as 1 | 2 | 3, ing.imgName);
                    }}
                    onKeyDown={(e) => {
                      if (e.key === "Enter" || e.key === " ") {
                        e.preventDefault();
                        e.stopPropagation();
                        handleRemoveItem(wsNum as 1 | 2 | 3, ing.imgName);
                      }
                    }}
                    style={{
                      zIndex: items.length - i,
                      position: "relative",
                      cursor: "pointer",
                    }}
                  />
                ))}
                <img
                  src={plate}
                  className="workstation-plate"
                  alt={`Plate for workstation ${wsNum}`}
                />
              </div>
            );
          })}
        </div>
      )}

      <Terminal
        workstation1Items={props.workstation1Items}
        setWorkstation1Items={props.setWorkstation1Items}
        workstation2Items={props.workstation2Items}
        setWorkstation2Items={props.setWorkstation2Items}
        workstation3Items={props.workstation3Items}
        setWorkstation3Items={props.setWorkstation3Items}
        plate1Items={props.plate1Items}
        setPlate1Items={props.setPlate1Items}
        plate2Items={props.plate2Items}
        setPlate2Items={props.setPlate2Items}
        plate3Items={props.plate3Items}
        setPlate3Items={props.setPlate3Items}
        branchData={props.branchData}
        setBranchData={props.setBranchData}
        currentBranch={props.currentBranch}
        setCurrentBranch={props.setCurrentBranch}
        newBranch={newBranch}
        setNewBranch={setNewBranch}
      />

      <p>{textDisplay}</p>
    </div>
  );
}

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
  sessionID: string;
  userID: string;
  handleDragStartFromWorkstation: (
    e: React.DragEvent,
    img: IngredientImage,
    workstation: number
  ) => void;
  handleDropOnWorkstation: (e: React.DragEvent, workstationNum: number) => void;
  handleDragOver: (e: React.DragEvent) => void;
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

  const [selectedImage, setSelectedImage] = useState<{
    workstation: 1 | 2 | 3;
    imgName: string;
  } | null>(null);

  function moveSelectedImage(
    direction: "up" | "down",
    selectedImage: { workstation: 1 | 2 | 3; imgName: string } | null,
    workstationItems: IngredientImage[],
    setWorkstationItems: Dispatch<SetStateAction<IngredientImage[]>>
  ) {
    if (!selectedImage) return;

    setWorkstationItems((items) => {
      const index = items.findIndex(
        (img) => img.imgName === selectedImage.imgName
      );
      if (index === -1) return items; // Image not found

      // Determine the new index based on the direction
      const newIndex = direction === "up" ? index - 1 : index + 1;
      if (newIndex < 0 || newIndex >= items.length) return items; // Prevent out of bounds

      // Swap the items at the current index and the new index
      const newItems = [...items];
      [newItems[index], newItems[newIndex]] = [
        newItems[newIndex],
        newItems[index],
      ];

      return newItems;
    });
  }

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
      <p className="section-text">Workstation</p>
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
                onDrop={(e) => props.handleDropOnWorkstation(e, wsNum)}
                onDragOver={props.handleDragOver}
              >
                <div className="workstation-contents">
                  {items.map((ing, i) => (
                    <img
                      key={ing.imgName}
                      src={ing.imgStr}
                      tabIndex={0}
                      role="button"
                      className={
                        selectedImage?.workstation === wsNum &&
                        selectedImage?.imgName === ing.imgName
                          ? "ingredient-img selected"
                          : "ingredient-img"
                      }
                      aria-label={`Remove ingredient ${ing.imgName}`}
                      draggable
                      onDragStart={(e) =>
                        props.handleDragStartFromWorkstation(e, ing, wsNum)
                      }
                      onClick={(e) => {
                        e.stopPropagation();
                        const isAlreadySelected =
                          selectedImage?.workstation === wsNum &&
                          selectedImage?.imgName === ing.imgName;

                        if (isAlreadySelected) {
                          setSelectedImage(null); // Deselect
                        } else {
                          setSelectedImage({
                            workstation: wsNum as 1 | 2 | 3,
                            imgName: ing.imgName,
                          });
                        }
                      }}
                      onKeyDown={(e) => {
                        if (e.key === "Backspace" || e.key === "Delete") {
                          e.preventDefault();
                          e.stopPropagation();
                          handleRemoveItem(wsNum as 1 | 2 | 3, ing.imgName);
                        }
                        if (e.key === "Enter" || e.key === " ") {
                          const isAlreadySelected =
                            selectedImage?.workstation === wsNum &&
                            selectedImage?.imgName === ing.imgName;

                          if (isAlreadySelected) {
                            setSelectedImage(null); // Deselect
                          } else {
                            setSelectedImage({
                              workstation: wsNum as 1 | 2 | 3,
                              imgName: ing.imgName,
                            });
                          }
                        }
                        if (e.shiftKey) {
                          switch (e.key) {
                            case "ArrowUp":
                              if (selectedImage?.workstation === 1) {
                                moveSelectedImage(
                                  "up",
                                  selectedImage,
                                  props.workstation1Items,
                                  props.setWorkstation1Items
                                );
                              } else if (selectedImage?.workstation === 2) {
                                moveSelectedImage(
                                  "up",
                                  selectedImage,
                                  props.workstation2Items,
                                  props.setWorkstation2Items
                                );
                              } else if (selectedImage?.workstation === 3) {
                                moveSelectedImage(
                                  "up",
                                  selectedImage,
                                  props.workstation3Items,
                                  props.setWorkstation3Items
                                );
                              }
                              break;
                            case "ArrowDown":
                              if (selectedImage?.workstation === 1) {
                                moveSelectedImage(
                                  "down",
                                  selectedImage,
                                  props.workstation1Items,
                                  props.setWorkstation1Items
                                );
                              } else if (selectedImage?.workstation === 2) {
                                moveSelectedImage(
                                  "down",
                                  selectedImage,
                                  props.workstation2Items,
                                  props.setWorkstation2Items
                                );
                              } else if (selectedImage?.workstation === 3) {
                                moveSelectedImage(
                                  "down",
                                  selectedImage,
                                  props.workstation3Items,
                                  props.setWorkstation3Items
                                );
                              }
                              break;
                          }
                        }
                      }}
                      onBlur={() => {
                        if (
                          selectedImage?.workstation === wsNum &&
                          selectedImage?.imgName === ing.imgName
                        ) {
                          setSelectedImage(null);
                        }
                      }}
                      style={{
                        zIndex: items.length - i,
                        position: "relative",
                        cursor: "pointer",
                      }}
                    />
                  ))}
                </div>
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
        branchTypes={props.branchTypes}
        setBranchTypes={props.setBranchTypes}
        sessionID={props.sessionID}
        userID={props.userID}
      />

      <p>{textDisplay}</p>
    </div>
  );
}

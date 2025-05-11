import { Dispatch, SetStateAction, useState, useEffect } from "react";
import "../../../styles/game.css";
import { IngredientImage } from "../Game";
import { Terminal } from "./terminal/Terminal";
import type { CommitData, BranchData } from "../../App";
import { plate } from "../../../assets/images";

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

  return (
    <div className="workstation-container">
      <p>Workstation</p>
      <div className="workstation-background">
        <div
          className={`workstation-section workstation1 ${
            props.selectedWorkstation === 1 ? "selected" : ""
          }`}
          onClick={() => props.setSelectedWorkstation(1)}
        >
          {props.workstation1Items.map((ing, i) => (
            <img
              key={ing.imgName}
              src={ing.imgStr}
              onClick={(e) => {
                e.stopPropagation();
                handleRemoveItem(1, ing.imgName);
              }}
              style={{
                zIndex: props.workstation1Items.length - i,
                position: "relative",
                cursor: "pointer",
              }}
            />
          ))}
          <img src={plate} style={{ width: "100px", height: "100px:" }} />
        </div>
        <div
          className={`workstation-section workstation1 ${
            props.selectedWorkstation === 2 ? "selected" : ""
          }`}
          onClick={() => props.setSelectedWorkstation(2)}
        >
          {props.workstation2Items.map((ing, i) => (
            <img
              key={ing.imgName}
              src={ing.imgStr}
              onClick={(e) => {
                e.stopPropagation();
                handleRemoveItem(2, ing.imgName);
              }}
              style={{
                zIndex: props.workstation2Items.length - i,
                position: "relative",
                cursor: "pointer",
              }}
            />
          ))}
          <img src={plate} style={{ width: "100px", height: "100px:" }} />
        </div>
        <div
          className={`workstation-section workstation1 ${
            props.selectedWorkstation === 3 ? "selected" : ""
          }`}
          onClick={() => props.setSelectedWorkstation(3)}
        >
          {props.workstation3Items.map((ing, i) => (
            <img
              key={ing.imgName}
              src={ing.imgStr}
              onClick={(e) => {
                e.stopPropagation();
                handleRemoveItem(3, ing.imgName);
              }}
              style={{
                zIndex: props.workstation3Items.length - i,
                position: "relative",
                cursor: "pointer",
              }}
            />
          ))}
          <img src={plate} style={{ width: "100px", height: "100px:" }} />
        </div>
      </div>

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
      />

      <p>{textDisplay}</p>
    </div>
  );
}

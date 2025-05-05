import { Dispatch, SetStateAction, useState, useEffect } from "react";
import "../../../styles/game.css";
import { IngredientImage } from "../Game";
import { Terminal } from "./terminal/Terminal";
import type { CommitData, BranchData } from "../../App";

interface WorkstationProps {
  workstationItems: IngredientImage[];
  setWorkstationItems: Dispatch<SetStateAction<IngredientImage[]>>;
  plateItems: IngredientImage[];
  setPlateItems: Dispatch<SetStateAction<IngredientImage[]>>;
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

  return (
    <div className="workstation-container">
      <p>Workstation</p>
      <div className="workstation-ingredients">
        {props.workstationItems.map((ing, index) => (
          <div>
            <p>{ing.imgName}</p>
            <img key={ing.imgName} src={ing.imgStr} />
          </div>
        ))}
      </div>

      <Terminal
        workstationItems={props.workstationItems}
        setWorkstationItems={props.setWorkstationItems}
        plateItems={props.plateItems}
        setPlateItems={props.setPlateItems}
        branchData={props.branchData}
        setBranchData={props.setBranchData}
      />

      <p>{textDisplay}</p>
    </div>
  );
}

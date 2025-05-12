import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../styles/game.css";
import { Plate } from "./plate/Plate";
import { Level } from "./level/Level";
import { Ingredients } from "./ingredients/Ingredients";
import { Workstation } from "./workstation/Workstation";
import type { CommitData, BranchData } from "../App";
import { burger_top, burger_bottom } from "../../assets/images";

export interface IngredientImage {
  imgStr: string;
  imgName: string;
}

interface GameProps {
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
}

export function Game(props: GameProps) {
  const [workstation1Items, setWorkstation1Items] = useState<IngredientImage[]>(
    []
  );
  const [workstation2Items, setWorkstation2Items] = useState<IngredientImage[]>(
    []
  );
  const [workstation3Items, setWorkstation3Items] = useState<IngredientImage[]>(
    []
  );
  const [plate1Items, setPlate1Items] = useState<IngredientImage[]>([]);
  const [plate2Items, setPlate2Items] = useState<IngredientImage[]>([]);
  const [plate3Items, setPlate3Items] = useState<IngredientImage[]>([]);

  const [selectedWorkstation, setSelectedWorkstation] = useState<
    1 | 2 | 3 | null
  >(null);

  const levelData = [
    {
      instructions: "Instructions for level 1",
      orderItems: [
        { imgStr: burger_top, imgName: "1" },
        { imgStr: burger_bottom, imgName: "1" },
        { imgStr: burger_bottom, imgName: "1" },
        { imgStr: burger_bottom, imgName: "1" },
        { imgStr: burger_bottom, imgName: "1" },
        { imgStr: burger_bottom, imgName: "1" },
      ],
    },
    {
      instructions: "Instructions for level 2",
      orderItems: [
        { imgStr: burger_bottom, imgName: "1" },
        { imgStr: burger_bottom, imgName: "1" },
      ],
    },
    {
      instructions: "Instructions for level 3",
      orderItems: [
        { imgStr: burger_bottom, imgName: "1" },
        { imgStr: burger_bottom, imgName: "1" },
      ],
    },
    {
      instructions: "Instructions for level 4",
      orderItems: [
        { imgStr: burger_bottom, imgName: "1" },
        { imgStr: burger_bottom, imgName: "1" },
      ],
    },
    {
      instructions: "Instructions for level 5",
      orderItems: [
        { imgStr: burger_bottom, imgName: "1" },
        { imgStr: burger_bottom, imgName: "1" },
      ],
    },
    {
      instructions: "Instructions for level 6",
      orderItems: [
        { imgStr: burger_bottom, imgName: "1" },
        { imgStr: burger_bottom, imgName: "1" },
      ],
    },
  ];

  const [currentLevel, setCurrentLevel] = useState(1);
  const [orderItems, setOrderItems] = useState(levelData[0].orderItems);

  const handlePrev = () => {
    if (currentLevel > 1) {
      const newLevel = currentLevel - 1;
      setCurrentLevel(newLevel);
      setOrderItems(levelData[newLevel - 1].orderItems);
    }
  };

  const handleNext = () => {
    if (currentLevel < levelData.length) {
      const newLevel = currentLevel + 1;
      setCurrentLevel(newLevel);
      setOrderItems(levelData[newLevel - 1].orderItems);
    }
  };

  function getBranchIngredients(branch: string): IngredientImage[] {
    switch (branch) {
      case "fries":
        return [
          { imgStr: burger_bottom, imgName: "1" },
          { imgStr: burger_bottom, imgName: "1" },
        ];
        break;
      default:
        return [
          { imgStr: burger_top, imgName: "1" },
          { imgStr: burger_bottom, imgName: "1" },
          { imgStr: burger_bottom, imgName: "1" },
          { imgStr: burger_bottom, imgName: "1" },
          { imgStr: burger_bottom, imgName: "1" },
          { imgStr: burger_bottom, imgName: "1" },
        ];
    }
  }

  return (
    <div className="game-container">
      <div className="flex-row">
        <div className="level-container">
          <div className="level-buttons"></div>
          <Level
            level={currentLevel}
            instructions={levelData[currentLevel - 1].instructions}
            orderItems={orderItems}
            setOrderItems={setOrderItems}
            onPrev={handlePrev}
            onNext={handleNext}
            isFirst={currentLevel === 1}
            isLast={currentLevel === levelData.length}
          />
        </div>
        <div className="plate-ingredient-workstation-container">
          <Plate
            plate1Items={plate1Items}
            setPlate1Items={setPlate1Items}
            plate2Items={plate2Items}
            setPlate2Items={setPlate2Items}
            plate3Items={plate3Items}
            setPlate3Items={setPlate3Items}
          />
          <Workstation
            selectedWorkstation={selectedWorkstation}
            setSelectedWorkstation={setSelectedWorkstation}
            workstation1Items={workstation1Items}
            setWorkstation1Items={setWorkstation1Items}
            workstation2Items={workstation2Items}
            setWorkstation2Items={setWorkstation2Items}
            workstation3Items={workstation3Items}
            setWorkstation3Items={setWorkstation3Items}
            plate1Items={plate1Items}
            setPlate1Items={setPlate1Items}
            plate2Items={plate2Items}
            setPlate2Items={setPlate2Items}
            plate3Items={plate3Items}
            setPlate3Items={setPlate3Items}
            branchData={props.branchData}
            setBranchData={props.setBranchData}
            currentBranch={props.currentBranch}
            setCurrentBranch={props.setCurrentBranch}
          />
          <Ingredients
            ingredientsItems={getBranchIngredients(props.currentBranch)}
            workstation1Items={workstation1Items}
            setWorkstation1Items={setWorkstation1Items}
            workstation2Items={workstation2Items}
            setWorkstation2Items={setWorkstation2Items}
            workstation3Items={workstation3Items}
            setWorkstation3Items={setWorkstation3Items}
            selectedWorkstation={selectedWorkstation}
          />
        </div>
      </div>
    </div>
  );
}

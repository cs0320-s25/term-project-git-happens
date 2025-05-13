import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../styles/game.css";
import { Plate } from "./plate/Plate";
import { Level } from "./level/Level";
import { Ingredients } from "./ingredients/Ingredients";
import { Workstation } from "./workstation/Workstation";
import type { CommitData, BranchData } from "../App";
import {
  fancy_patty,
  fries,
  ketchup,
  lettuce,
  mayo,
  moldy_patty,
  mustard,
  onion,
  patty,
  pretzel_bottom,
  pretzel_top,
  sesame_bottom,
  sesame_top,
  tomato,
  plate,
  cheese,
} from "../../assets/images";

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
  sessionID: string;
  userID: string;
}

export interface BranchType {
  branchName: string;
  branchType: "fries" | "burger";
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
        { imgStr: sesame_top, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
      ],
      completed: true,
    },
    {
      instructions:
        "Instructions for level 2 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
      orderItems: [
        { imgStr: sesame_bottom, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
      ],
      completed: true,
    },
    {
      instructions: "Instructions for level 3",
      orderItems: [
        { imgStr: sesame_bottom, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
      ],
      completed: false,
    },
    {
      instructions: "Instructions for level 4",
      orderItems: [
        { imgStr: sesame_bottom, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
      ],
      completed: false,
    },
    {
      instructions: "Instructions for level 5",
      orderItems: [
        { imgStr: sesame_bottom, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
      ],
      completed: false,
    },
    {
      instructions: "Instructions for level 6",
      orderItems: [
        { imgStr: sesame_bottom, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
      ],
      completed: false,
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

  const [branchTypes, setBranchTypes] = useState<BranchType[]>([
    { branchName: "main", branchType: "burger" },
  ]);

  function getBranchIngredients(type: string): IngredientImage[] {
    switch (type) {
      case "fries":
        return [
          { imgStr: fries, imgName: "1" },
          { imgStr: ketchup, imgName: "1" },
          { imgStr: mayo, imgName: "1" },
          { imgStr: mustard, imgName: "1" },
        ];
      default:
        return [
          { imgStr: sesame_top, imgName: "1" },
          { imgStr: sesame_bottom, imgName: "1" },
          { imgStr: pretzel_top, imgName: "1" },
          { imgStr: pretzel_bottom, imgName: "1" },
          { imgStr: patty, imgName: "1" },
          { imgStr: lettuce, imgName: "1" },
          { imgStr: tomato, imgName: "1" },
          { imgStr: onion, imgName: "1" },
          { imgStr: cheese, imgName: "1" },
          { imgStr: ketchup, imgName: "1" },
          { imgStr: mayo, imgName: "1" },
          { imgStr: mustard, imgName: "1" },
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
            completed={levelData[currentLevel - 1].completed}
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
            branchTypes={branchTypes}
            setBranchTypes={setBranchTypes}
            sessionID={props.sessionID}
            userID={props.userID}
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

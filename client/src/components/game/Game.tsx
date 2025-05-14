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
      instructions:
        "Welcome to Git Happens! In this game, you will learn helpful Git commands and practices by taking, making, and sending out food orders from a kitchen. To start, try creating the burger order below! When you're done, git add -A to add the order to the serving tray, git commit -m “<WRITE A HELPFUL MESSAGE HERE>” to ready the order, and git push to serve.",
      orderItems: [
        { imgStr: sesame_top, imgName: "1" },
        { imgStr: tomato, imgName: "1" },
        { imgStr: lettuce, imgName: "1" },
        { imgStr: cheese, imgName: "1" },
        { imgStr: patty, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
      ],
      completed: false,
    },
    {
      instructions:
        "It looks like one of your coworkers served a moldy burger... Take it back to your workstations with git pull. If you encounter a merge conflict, try to resolve it by choosing the ingredients you want to keep. In this case, keep everything but the mold. Once you are done, add, commit, and push your burger when it looks like the order below!",
      orderItems: [
        { imgStr: sesame_top, imgName: "1" },
        { imgStr: lettuce, imgName: "1" },
        { imgStr: cheese, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
      ],
      completed: false,
    },
    {
      instructions:
        "This time, your customers want an order of fries... Try creating a new fry station branch using git branch “<BRANCH NAME HERE>”, and navigate to it using git checkout “<BRANCH NAME HERE>”. Once you have added your fries, try to git merge back into your main branch in order to assemble the complete order and serve.",
      orderItems: [
        { imgStr: ketchup, imgName: "1" },
        { imgStr: fries, imgName: "1" },
      ],
      completed: false,
    },
    {
      instructions:
        "This time, your customers want to order the moldy burger you pulled previously, which you can't make from your workstation. Try using git log to view the kitchen's previous commits, and git reset “<COMMIT ID HERE>” to return to that commit.",
      orderItems: [
        { imgStr: sesame_top, imgName: "1" },
        { imgStr: lettuce, imgName: "1" },
        { imgStr: cheese, imgName: "1" },
        { imgStr: moldy_patty, imgName: "1" },
        { imgStr: sesame_bottom, imgName: "1" },
      ],
      completed: false,
    },
    {
      instructions:
        "Uh oh, looks like the health inspector is coming around, so you should get rid of any plates containing moldy burger! Use git rm “<FILE NAME HERE>” to remove a plate from both your local (kitchen) and remote (dining room) repositories.",
      orderItems: [{ imgStr: sesame_bottom, imgName: "1" }],
      completed: false,
    },
    {
      instructions:
        "Try pulling and modifying the following order, but instead of pushing, save it with git stash.",
      orderItems: [
        { imgStr: fancy_patty, imgName: "1" },
        { imgStr: pretzel_bottom, imgName: "1" },
      ],
      completed: false,
    },
    {
      instructions:
        "Now that we've stashed our burger, we can take care of this order of fries using the commands that we've learned previously.",
      orderItems: [
        { imgStr: mustard, imgName: "1" },
        { imgStr: fries, imgName: "1" },
      ],
      completed: false,
    },
    {
      instructions:
        "Finally, let's finish our fancy half-made burger by viewing our stash with git stash list and retrieving our stashed commit with git stash pop in order to complete the following order.",
      orderItems: [
        { imgStr: pretzel_top, imgName: "1" },
        { imgStr: tomato, imgName: "1" },
        { imgStr: mayo, imgName: "1" },
        { imgStr: onion, imgName: "1" },
        { imgStr: cheese, imgName: "1" },
        { imgStr: fancy_patty, imgName: "1" },
        { imgStr: pretzel_bottom, imgName: "1" },
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

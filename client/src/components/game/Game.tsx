import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../styles/main.css";
import { Plate } from "./plate/Plate";
import { Order } from "./order/Order";
import { Ingredients } from "./ingredients/Ingredients";
import { Workstation } from "./workstation/Workstation";
import type { CommitData, BranchData } from "../App";

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
}

export function Game(props: GameProps) {
  const [workstationItems, setWorkstationItems] = useState<IngredientImage[]>(
    []
  );
  const [plateItems, setPlateItems] = useState<IngredientImage[]>([]);

  return (
    <div className="game">
      <p>Game</p>
      <div>
        <div className="plate-order-flex">
          <div className="order-ingredients-container">
            <Order />
            <Ingredients
              workstationItems={workstationItems}
              setWorkstationItems={setWorkstationItems}
            />
          </div>
          <Plate plateItems={plateItems} setPlateItems={setPlateItems} />
        </div>
      </div>
      <div>
        <Workstation
          workstationItems={workstationItems}
          setWorkstationItems={setWorkstationItems}
          plateItems={plateItems}
          setPlateItems={setPlateItems}
          branchData={props.branchData}
          setBranchData={props.setBranchData}
        />
      </div>
    </div>
  );
}

import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../styles/game.css";
import { Plate } from "./plate/Plate";
import { Order } from "./order/Order";
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

  const [orderItems, setOrderItems] = useState<IngredientImage[]>([
    { imgStr: burger_top, imgName: "1" },
    { imgStr: burger_bottom, imgName: "1" },
    { imgStr: burger_bottom, imgName: "1" },
    { imgStr: burger_bottom, imgName: "1" },
    { imgStr: burger_bottom, imgName: "1" },
    { imgStr: burger_bottom, imgName: "1" },
  ]);

  return (
    <div className="game-container">
      <div className="flex-row">
        <div className="instructions-order-container">
          <h1>Level 1</h1>
          <p>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce sed
            ornare neque. Aenean non metus sit amet nisi fermentum pharetra.
            Aliquam egestas porta enim, sit amet commodo dui tempor sed.
            Pellentesque et commodo arcu, ut vehicula quam. Nulla tristique
            lacus a felis scelerisque suscipit. Mauris auctor in tortor quis
            maximus. Vestibulum fermentum varius malesuada. Maecenas vestibulum
            sagittis lorem, non dictum massa malesuada elementum. Aliquam
            consequat elit nec turpis pharetra imperdiet. Cras quis elementum
            elit, non porttitor orci. Nam volutpat quam id arcu hendrerit
            porttitor. Aenean vitae leo ex. Nunc molestie non erat ac sodales.
          </p>
          <Order orderItems={orderItems} setOrderItems={setOrderItems} />
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
          <Ingredients
            workstation1Items={workstation1Items}
            setWorkstation1Items={setWorkstation1Items}
            workstation2Items={workstation2Items}
            setWorkstation2Items={setWorkstation2Items}
            workstation3Items={workstation3Items}
            setWorkstation3Items={setWorkstation3Items}
            selectedWorkstation={selectedWorkstation}
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
          />
        </div>
      </div>
    </div>
  );
}

import { useState } from "react";
import "../../styles/main.css";
import { Plate } from "./plate/Plate";
import { Order } from "./order/Order";
import { Ingredients } from "./ingredients/Ingredients";
import { Workstation } from "./workstation/Workstation";

export interface IngredientImage {
  imgStr: string;
  imgName: string;
}

export function Game() {
  const [workstationItems, setWorkstationItems] = useState<IngredientImage[]>(
    []
  );
  const [plateItems, setPlateItems] = useState<IngredientImage[]>([]);

  return (
    <div className="game">
      <p>Game</p>
      <div>
        <div className="plate-order-flex">
          <Plate plateItems={plateItems} setPlateItems={setPlateItems} />
          <div className="order-ingredients-container">
            <Order />
            <Ingredients
              workstationItems={workstationItems}
              setWorkstationItems={setWorkstationItems}
            />
          </div>
        </div>
      </div>
      <div>
        <Workstation
          workstationItems={workstationItems}
          setWorkstationItems={setWorkstationItems}
          plateItems={plateItems}
          setPlateItems={setPlateItems}
        />
      </div>
    </div>
  );
}

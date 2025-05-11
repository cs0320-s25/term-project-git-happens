import { Dispatch, SetStateAction, useState, useEffect } from "react";
import "../../../styles/game.css";
import { burger_top, burger_bottom } from "../../../assets/images";
import { IngredientImage } from "../Game";

interface IngredientsProps {
  workstation1Items: IngredientImage[];
  setWorkstation1Items: Dispatch<SetStateAction<IngredientImage[]>>;
  workstation2Items: IngredientImage[];
  setWorkstation2Items: Dispatch<SetStateAction<IngredientImage[]>>;
  workstation3Items: IngredientImage[];
  setWorkstation3Items: Dispatch<SetStateAction<IngredientImage[]>>;
  selectedWorkstation: 1 | 2 | 3 | null;
}

export function Ingredients(props: IngredientsProps) {
  function generateIngredientName(baseName: string): string {
    let count = 0;
    if (props.selectedWorkstation === 1) {
      count = props.workstation1Items.length;
    } else if (props.selectedWorkstation === 2) {
      count = props.workstation2Items.length;
    } else if (props.selectedWorkstation === 3) {
      count = props.workstation3Items.length;
    }
    return `${baseName}_${count + 1}`;
  }

  function handleAddIngredient(ing: IngredientImage): void {
    switch (props.selectedWorkstation) {
      case 1:
        props.setWorkstation1Items((prev) => [...prev, ing]);
        break;
      case 2:
        props.setWorkstation2Items((prev) => [...prev, ing]);
        break;
      case 3:
        props.setWorkstation3Items((prev) => [...prev, ing]);
        break;
      default:
    }
  }

  return (
    <div className="ingredients-container">
      <div className="ingredients-scroll">
        <img
          src={burger_top}
          onClick={() =>
            handleAddIngredient({
              imgStr: burger_top,
              imgName: generateIngredientName("burger_top"),
            })
          }
        ></img>
        <img
          src={burger_bottom}
          onClick={() =>
            handleAddIngredient({
              imgStr: burger_bottom,
              imgName: generateIngredientName("burger_bottom"),
            })
          }
        ></img>
        <img
          src={burger_bottom}
          onClick={() =>
            handleAddIngredient({
              imgStr: burger_bottom,
              imgName: generateIngredientName("burger_bottom"),
            })
          }
        ></img>
        <img
          src={burger_bottom}
          onClick={() =>
            handleAddIngredient({
              imgStr: burger_bottom,
              imgName: generateIngredientName("burger_bottom"),
            })
          }
        ></img>
      </div>
    </div>
  );
}

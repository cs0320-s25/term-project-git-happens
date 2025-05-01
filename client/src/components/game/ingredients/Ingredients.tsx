import { Dispatch, SetStateAction, useState, useEffect } from "react";
import "../../../styles/game.css";
import { burger_top, burger_bottom } from "../../../assets/images";
import { IngredientImage } from "../Game";

interface IngredientsProps {
  workstationItems: IngredientImage[];
  setWorkstationItems: Dispatch<SetStateAction<IngredientImage[]>>;
}

export function Ingredients(props: IngredientsProps) {
  const handleAddIngredient = (ing: IngredientImage) => {
    props.setWorkstationItems((prev) => [...prev, ing]);
  };

  return (
    <div className="ingredients-container">
      <p>Ingredients</p>
      <div>
        <img
          src={burger_top}
          onClick={() =>
            handleAddIngredient({ imgStr: burger_top, imgName: "burger_top" })
          }
        ></img>
        <img
          src={burger_bottom}
          onClick={() =>
            handleAddIngredient({
              imgStr: burger_bottom,
              imgName: "burger_bottom",
            })
          }
        ></img>
      </div>
    </div>
  );
}

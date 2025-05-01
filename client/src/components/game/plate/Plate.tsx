import { Dispatch, SetStateAction, useState, useEffect } from "react";
import "../../../styles/game.css";
import { plate } from "../../../assets/images";
import { IngredientImage } from "../Game";

interface PlateProps {
  plateItems: IngredientImage[];
  setPlateItems: Dispatch<SetStateAction<IngredientImage[]>>;
}

export function Plate(props: PlateProps) {
  return (
    <div className="plate-container">
      <p>Plate</p>
      <div className="plate-ingredients">
        {props.plateItems.map((ing, index) => (
          <img key={ing.imgName} src={ing.imgStr} />
        ))}
      </div>
      <img src={plate} alt="plate" width="400px"></img>
    </div>
  );
}

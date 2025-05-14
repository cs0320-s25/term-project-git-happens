import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../styles/game.css";
import { IngredientImage } from "../Game";

interface IngredientsProps {
  ingredientsItems: IngredientImage[];
  workstation1Items: IngredientImage[];
  setWorkstation1Items: Dispatch<SetStateAction<IngredientImage[]>>;
  workstation2Items: IngredientImage[];
  setWorkstation2Items: Dispatch<SetStateAction<IngredientImage[]>>;
  workstation3Items: IngredientImage[];
  setWorkstation3Items: Dispatch<SetStateAction<IngredientImage[]>>;
  selectedWorkstation: 1 | 2 | 3 | null;
  handleDragStartFromIngredients: (
    e: React.DragEvent,
    img: IngredientImage
  ) => void;
  handleDropOnIngredients: (e: React.DragEvent) => void;
  handleDragOver: (e: React.DragEvent) => void;
}

export function Ingredients(props: IngredientsProps) {
  const ingredientRefs = useRef<(HTMLImageElement | null)[]>([]);

  useEffect(() => {
    ingredientRefs.current = ingredientRefs.current.slice(
      0,
      props.ingredientsItems.length
    );
  }, [props.ingredientsItems]);

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
        props.setWorkstation1Items((prev) => [ing, ...prev]);
        break;
      case 2:
        props.setWorkstation2Items((prev) => [ing, ...prev]);
        break;
      case 3:
        props.setWorkstation3Items((prev) => [ing, ...prev]);
        break;
      default:
    }
  }

  const ingredientsContainerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      const target = e.target as HTMLElement;
      const isTyping =
        target.tagName === "INPUT" ||
        target.tagName === "TEXTAREA" ||
        target.isContentEditable;

      if (isTyping) return;

      if (e.shiftKey && e.key === "I") {
        e.preventDefault();
        ingredientsContainerRef.current?.focus();
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  return (
    <div
      className="ingredients-container drop-zone"
      role="region"
      aria-label="Available ingredients"
    >
      <div
        className="ingredients-scroll"
        ref={ingredientsContainerRef}
        tabIndex={-1}
        onDrop={props.handleDropOnIngredients}
        onDragOver={props.handleDragOver}
        aria-label="Ingredient list. Use arrow keys to navigate, Enter or Space to add."
      >
        {props.ingredientsItems.map((ingredient, index) => (
          <img
            key={index}
            ref={(el) => (ingredientRefs.current[index] = el)}
            src={ingredient.imgStr}
            tabIndex={0}
            role="button"
            aria-label={`Add ${
              ingredient.imgStr
                .split("/")
                .pop()
                ?.split("?")[0]
                .replace(/\.\w+$/, "")
                .replace(/[-_]/g, " ") || "ingredient"
            }`}
            draggable
            onDragStart={(e) =>
              props.handleDragStartFromIngredients(e, ingredient)
            }
            onClick={() =>
              handleAddIngredient({
                imgStr: ingredient.imgStr,
                imgName: generateIngredientName(ingredient.imgStr),
              })
            }
            onKeyDown={(e) => {
              if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                handleAddIngredient({
                  imgStr: ingredient.imgStr,
                  imgName: generateIngredientName(ingredient.imgStr),
                });
              } else if (e.key === "ArrowRight") {
                e.preventDefault();
                const nextIndex = (index + 1) % props.ingredientsItems.length;
                ingredientRefs.current[nextIndex]?.focus();
              } else if (e.key === "ArrowLeft") {
                e.preventDefault();
                const prevIndex =
                  (index - 1 + props.ingredientsItems.length) %
                  props.ingredientsItems.length;
                ingredientRefs.current[prevIndex]?.focus();
              }
            }}
            className="ingredient-item"
          />
        ))}
      </div>
    </div>
  );
}

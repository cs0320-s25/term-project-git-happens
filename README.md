# term-project: Git Happens

# Project Details
- Github Repo: https://github.com/cs0320-s25/term-project-git-happens.git
- For our final group project, we chose to create an interactible game remeniscent of projects such as Flexbox Froggy which teaches the fundementals of basic Git commands and storage.
- Time: 

# Design Choices
- Level Design:
    - In order to convey the intricacies of Git storage, we decided to structure our levels as an ongoing set of todo's which include a specific 'win condition' to be completed, allowing the user to advance to the next level. This allows for more user freedom within the game, while also ensuring the user's progress is saved and built upon through each level instead of being reset as seen in games like Flexbox Froggy.
- Frontend Interface Design:
    - Visual Design:
        - We decided to visually portray various elements of commit storage in order to efficiently teach Git's various systems.
        - For example, we used burger ingredients to represent lines of code being assembled in a file (plate) in a kitchen workstation (branch), which can then be added and committed via a serving tray to be pushed (served).
        - We also created a commit tree window which can be viewed at any time, allowing the user to view the trajectory of their commits in relation to other branches.
    - Command Parsing:
        - We utilized various means of splitting and splicing user command inputs in order to replicate terminal responses to Git commands, such as checking for '' and "" quotation markers around commit messages, and ensuring inputted tags (such as branch -a, -r, -d, etc.) are appropriately addressed.
    - <ADD MORE>
- Backend Structures and Handler Design:
    - Firebase Storage Structure:
        - <ADD MORE>
    - Command Handlers:
        - <ADD MORE>
    - <ADD MORE>

# Errors/Bugs
- <ADD MORE>

# Tests
- <ADD MORE>

# How to
- Running the Program:
    - Run the program locally using a Terminal by entering the 'client' package and running "npm start"
    - Next, navigate to your browser and 'http://localhost:8000/' in order to view the program
- Navigating the Program:
    - Upon start, input a Session ID and User ID and press "Enter" in order to begin a game session.
    - Click on a plate in the 'Workstation' to select it, and then scroll through the list of ingredients below and click them to add to the plate. Additionally, click plated ingredients in order to remove them from the plate.
    - In the top right corner, click the hamburger menu in order to toggle view of the branch tree.
    - Above the ingredients list, click on the terminal command input box and begin typing in order to input commands, as you would with a terminal.
    - View level instructions in the top left corner, and navigate through levels using the left and right arrow buttons.
- Testing:
    - <ADD MORE>

# Collaboration
- <ADD MORE>

# Resources Used:
- <ADD MORE>
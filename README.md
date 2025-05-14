# term-project: Git Happens

# Project Details
- Github Repo: https://github.com/cs0320-s25/term-project-git-happens.git
- For our final group project, we chose to create an interactible game remeniscent of projects such as Flexbox Froggy which teaches the fundementals of basic Git commands and storage.

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
- Backend Structures and Handler Design:
    - Firebase Storage Structure:
        - Local Storage:
            - Our local storage structure mimics a local Git repository. Each user has their own local copy of the project, and we use Firebase to store their local branches, changes, staged and pushed commits, and heads of each branch. This allows for users to practice managing a shared repository while still having access to the same ocal tools they would have if working on a project in a code editor.
        - Remote Storage:
            - Our remote storage structure mimics a remote Git repository. Every user with access to a game session is able to push their personal commits to the shared remote repository, as well as see the changes others make on the remote. Similar to local storage, the remote storage section of our database stores the remote refs to local branches, pushed commits, and the heads of every branch.
    - Command Handlers:
        - Our command handlers recreate 12 foundational git commands: add, rm, commit, push, pull, branch, checkout, merge, reset, stash, status, and log. Additionally, we have handlers for creating and deleting a game session and a handler which checks the user's submitted code against level solutions.
    - GitDiffHelper:
        - This class is responsible for determining whether a user is able to pull from, push to, or merge with the remote repository or other branches. We have implemented a subsequence-based automatic merging method to better mirror Git's ability to resolve differences in files. This class is also the basis of how we determine level win conditions, checking the user's pushed work against our solutions.

# Errors/Bugs
- <ADD MORE>

# Tests
- Backend Tests:
    -GitAddHandlerTest
    -GitCommitHandlerTest
    -GitPushHandlerTest
    -GitPullHandlerTest
    -GitBranchHandlerTest
    -GitCheckoutHandlerTest
    -GitMergeHandlerTest
    -GitResetHandlerTest
    -GitStashHandlerTest
    -GitStatusHandlerTest
    -GitLogHandlerTest
    -GitDiffHelperTest

# How to
- Running the Program:
    - Run the program locally using a Terminal by entering the 'client' package and running "npm start"
    - Next, navigate to your browser and 'http://localhost:8000/' in order to view the program
- Navigating the Program:
    - Upon start, input a Session ID and User ID and press "Enter" in order to begin a game session.
    - Click on a plate in the 'Workstation' to select it, and then scroll through the list of ingredients below and click them to add to the plate.
        - Additionally, click plated ingredients in order to select them, and utilize the UP and DOWN arrow keys + SHIFT to move ingredients up and down within the plated order. Press BACKSPACE / DELETE to remove the item from the plate.
    - In the top right corner, click the hamburger menu in order to toggle view of the branch tree.
    - Above the ingredients list, click on the terminal command input box and begin typing in order to input commands, as you would with a terminal.
    - View level instructions in the top left corner, and navigate through levels using the left and right arrow buttons.
- Testing:
    - Our backend is tested using a mocked database for storage, and calls the various handlers in isolation and in combined tests, ensuring that the handlers each return appropriate responses for null parameters, errors from commands, and successful calls. Additionally, we tested our GitDiffHelper. We completed unit testing for each method in this class, checking for error and success responses.

# Resources Used:
-https://www.atlassian.com/git/glossary#commands
-https://git-scm.com/docs
-https://education.github.com/git-cheat-sheet-education.pdf
-https://git-scm.com/docs/git
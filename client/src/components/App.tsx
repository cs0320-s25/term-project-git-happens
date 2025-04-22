import "../styles/App.css";
import { Select } from "./select/Select";

/**
 * This is the highest level of Mock which builds the component APP;
 *
 * @return JSX of the entire mock
 *  Note: if the user is loggedIn, the main interactive screen will show,
 *  else it will stay at the screen prompting for log in
 */
function App() {
  return (
    // <div className="App">
    //   <div className="sign-in-page">
    //     <SignedOut>
    //       <div className="custom-login" aria-label="sign-in-page">
    //         <h1>Sign in to Mock</h1>
    //         <p className="notice">Only brown.edu email addresses are allowed</p>
    //         <SignInButton aria-label="sign-in-button"/>
    //       </div>
    //     </SignedOut>
    //   </div>

    //   <SignedIn>
    //     <div className="App-header">
    //       <h1 aria-label="Mock Header" className="mock-header">
    //         Mock
    //       </h1>
		//   <div className= "header-buttons" aria-label="header-buttons" id="header-buttons" tabIndex={11}>
		// 	<UserButton aria-label="user-button" />
		// 	<SignOutButton aria-label="sign-out-button"/>
		//   </div>
    //     </div>
    //     <Select />
    //   </SignedIn>
    // </div>
    <div className="App">
    <Select />
    </div>
  );
}

export default App;

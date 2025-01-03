import { useEffect, useState } from 'react';
import './App.css';



function App() {
  const [url, setUrl] = useState(null);

  useEffect(() => {
    //const response = fetch("http://localhost:8080/oauth");
    fetch('http://localhost:8080/oauth')
        .then(response => response.text())
        .then(data => {
          console.log(data)
          setUrl(data)
        });
  }, []);

  function handleLogin() {
    window.open(url, "_self")
  }

  function handleTestToken() {
    fetch('http://localhost:8080/oauth/read-cookie',{
      method: 'GET',
      credentials: 'include', // Ensure cookies are included
    })
        .then(response => response.text())
        .then(data => console.log(data))
  }

  function handleGetProfilen() {
    fetch('http://localhost:8080/profile',{
      method: 'GET',
      credentials: 'include', // Ensure cookies are included
    })
        .then(response => response.text())
        .then(data => console.log(data))
  }

  return (
    <div className="App">
      <header className="App-header">
        <button onClick={handleLogin} >Login with Google</button>
        <button onClick={handleTestToken} >Test token</button>
        <button onClick={handleGetProfilen} >Get profile</button>
      </header>
    </div>
  );
}

export default App;

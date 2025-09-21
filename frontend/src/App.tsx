import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import Home from './pages/Home/Home';
import Pokedex from './pages/Pokedex/Pokedex';
import Login from "./pages/Login/Login.tsx";
import Profile from "./pages/Profile/Profile.tsx";
import ProtectedRoute from "./components/ProtectedRoute/ProtectedRoute.tsx";
import Register from "./pages/Register/Register.tsx";

const App: React.FC = () => {
  return (
      <Router>
        <Routes>
            <Route path="/" element={<MainLayout> <Home/> </MainLayout>}/>
            <Route path="/pokedex" element={<MainLayout><Pokedex/></MainLayout>}/>

            <Route path="/profile" element={<MainLayout><ProtectedRoute><Profile/></ProtectedRoute></MainLayout>}/>

            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
        </Routes>
      </Router>
  );
};

export default App;
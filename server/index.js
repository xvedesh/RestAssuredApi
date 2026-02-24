// CommonJS syntax for importing modules
const express = require('express');
const jsonServer = require('json-server');
const jwt = require('jsonwebtoken');

const server = jsonServer.create();
const router = jsonServer.router('clients.json'); // Adjust the path to your JSON file as necessary
const middlewares = jsonServer.defaults();

// Secret key for JWT signing and encryption
const SECRET_KEY = '36bc4058ddcb5b7d71a2c9a1700bc1f467910e032c3891143eb0ae489f3c0289';

// Create a token from a payload
function createToken(payload) {
  return jwt.sign(payload, SECRET_KEY, { expiresIn: '1h' });
}

// Verify the token
function verifyToken(token) {
  return jwt.verify(token, SECRET_KEY);
}

// Middleware for checking if the user is authenticated
function isAuthenticated(req) {
  if (req.headers.authorization) {
    const token = req.headers.authorization.split(' ')[1];
    try {
      return verifyToken(token);
    } catch (e) {
      return false;
    }
  }
  return false;
}

server.use(middlewares);
server.use(express.json());

// Mock users database
const users = [
  { id: 1, username: 'user1', password: 'password1' },
  { id: 2, username: 'user2', password: 'password2' },
  // Add more mock users as needed
];
  
// Helper function to simulate user validation
function validateCredentials(username, password) {
  // In a real application, you'd hash the password and check against the database.
  // Here we're just doing a simple lookup for demonstration purposes.
  const user = users.find(u => u.username === username && u.password === password);
  return user;
}
  
// Login route
server.post('/auth/login', (req, res) => {
  const { username, password } = req.body;
  const user = validateCredentials(username, password);
  if (user) {
    // User is valid, create a token with the username as the subject or payload.
    const accessToken = createToken({ sub: user.username });
    res.status(200).jsonp({ accessToken });
  } else {
    // If credentials are not valid, return an error.
    res.status(401).jsonp({ message: 'Authentication failed' });
  }
});

// Protect routes
server.use((req, res, next) => {
  if (req.path !== '/auth/login' && !isAuthenticated(req)) {
    res.status(403).jsonp({ message: 'Not authorized' });
  } else {
    next();
  }
});

server.use(router);

const httpServer = server.listen(3000, () => {
  console.log('JSON Server is running with mock authentication');
});

process.on('SIGTERM', () => {
  console.log('SIGTERM received. Shutting down gracefully...');
  httpServer.close(() => {
    process.exit(0);
  });
});

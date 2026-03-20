// CommonJS syntax for importing modules
const express = require('express');
const jsonServer = require('json-server');
const jwt = require('jsonwebtoken');
const {
  connectProducer,
  disconnectProducer,
  publishClientEvent,
  publishEntityEvent,
  topics
} = require('./kafkaPublisher');

const server = jsonServer.create();
const router = jsonServer.router('clients.json'); // Adjust the path to your JSON file as necessary
const middlewares = jsonServer.defaults();

// Secret key for JWT signing and encryption
const SECRET_KEY = '36bc4058ddcb5b7d71a2c9a1700bc1f467910e032c3891143eb0ae489f3c0289';
const FRAMEWORK_PREFIX = '[AI-KAFKA-VALIDATOR]';

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

function getClientCollection() {
  return router.db.get('clients');
}

function getCollection(collectionName) {
  return router.db.get(collectionName);
}

function findClientById(clientId) {
  return findById('clients', clientId);
}

function findById(collectionName, entityId) {
  return getCollection(collectionName).value().find(item => String(item.id) === String(entityId));
}

function hasCollectionItems(collectionName, predicate) {
  return getCollection(collectionName).value().some(predicate);
}

function clientExists(clientId) {
  return Boolean(findClientById(clientId));
}

function accountExists(accountId) {
  return Boolean(findById('accounts', accountId));
}

function validateAccountRelationship(account) {
  if (!clientExists(account.clientId)) {
    return `Client not found for clientId=${account.clientId}`;
  }

  return null;
}

function validatePortfolioRelationship(portfolio) {
  if (!clientExists(portfolio.clientId)) {
    return `Client not found for clientId=${portfolio.clientId}`;
  }

  return null;
}

function validateTransactionRelationship(transaction) {
  if (!clientExists(transaction.clientId)) {
    return `Client not found for clientId=${transaction.clientId}`;
  }

  const account = findById('accounts', transaction.accountId);
  if (!account) {
    return `Account not found for accountId=${transaction.accountId}`;
  }

  if (String(account.clientId) !== String(transaction.clientId)) {
    return 'Transaction clientId does not match account owner';
  }

  return null;
}

function getDependencyViolationForClient(clientId) {
  if (hasCollectionItems('transactions', transaction => String(transaction.clientId) === String(clientId))) {
    return 'Cannot delete client with existing transactions';
  }

  if (hasCollectionItems('accounts', account => String(account.clientId) === String(clientId))) {
    return 'Cannot delete client with existing accounts';
  }

  if (hasCollectionItems('portfolios', portfolio => String(portfolio.clientId) === String(clientId))) {
    return 'Cannot delete client with existing portfolios';
  }

  return null;
}

function getDependencyViolationForAccount(accountId) {
  if (hasCollectionItems('transactions', transaction => String(transaction.accountId) === String(accountId))) {
    return 'Cannot delete account with existing transactions';
  }

  return null;
}

function validateRelationship(entityType, entity) {
  if (entityType === 'ACCOUNT') {
    return validateAccountRelationship(entity);
  }

  if (entityType === 'PORTFOLIO') {
    return validatePortfolioRelationship(entity);
  }

  if (entityType === 'TRANSACTION') {
    return validateTransactionRelationship(entity);
  }

  return null;
}

function mergeDeep(target, source) {
  const merged = { ...target };

  Object.keys(source || {}).forEach(key => {
    const sourceValue = source[key];
    const targetValue = merged[key];

    if (
      sourceValue &&
      typeof sourceValue === 'object' &&
      !Array.isArray(sourceValue) &&
      targetValue &&
      typeof targetValue === 'object' &&
      !Array.isArray(targetValue)
    ) {
      merged[key] = mergeDeep(targetValue, sourceValue);
    } else {
      merged[key] = sourceValue;
    }
  });

  return merged;
}

server.use(middlewares);
server.use(express.json());

server.get('/health', (_req, res) => {
  res.status(200).jsonp({
    status: 'UP',
    kafkaTopics: topics
  });
});

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

server.post('/clients', async (req, res) => {
  const client = req.body;
  const clientId = String(client.id);

  if (!client.id) {
    return res.status(400).jsonp({ message: 'Client id is required' });
  }

  if (findClientById(client.id)) {
    return res.status(409).jsonp({ message: `Client ${clientId} already exists` });
  }

  try {
    getClientCollection().push(client).write();
    await publishClientEvent('CLIENT_CREATED', clientId, client);
    return res.status(201).jsonp(client);
  } catch (error) {
    console.error(`${FRAMEWORK_PREFIX}[Kafka] Failed to publish CLIENT_CREATED for clientId=${clientId}`, error);
    return res.status(500).jsonp({ message: 'Failed to publish client created event' });
  }
});

server.put('/clients/:id', async (req, res) => {
  const clientId = String(req.params.id);
  const existingClient = findClientById(req.params.id);

  if (!existingClient) {
    return res.status(404).jsonp({ message: `Client ${clientId} not found` });
  }

  const updatedClient = {
    ...req.body,
    id: req.params.id
  };

  try {
    getClientCollection().find({ id: req.params.id }).assign(updatedClient).write();
    await publishClientEvent('CLIENT_UPDATED', clientId, updatedClient);
    return res.status(200).jsonp(updatedClient);
  } catch (error) {
    console.error(`${FRAMEWORK_PREFIX}[Kafka] Failed to publish CLIENT_UPDATED for clientId=${clientId}`, error);
    return res.status(500).jsonp({ message: 'Failed to publish client updated event' });
  }
});

server.patch('/clients/:id', async (req, res) => {
  const clientId = String(req.params.id);
  const existingClient = findClientById(req.params.id);

  if (!existingClient) {
    return res.status(404).jsonp({ message: `Client ${clientId} not found` });
  }

  const patchedClient = mergeDeep(existingClient, req.body);
  patchedClient.id = req.params.id;

  try {
    getClientCollection().find({ id: req.params.id }).assign(patchedClient).write();
    await publishClientEvent('CLIENT_PATCHED', clientId, patchedClient);
    return res.status(200).jsonp(patchedClient);
  } catch (error) {
    console.error(`${FRAMEWORK_PREFIX}[Kafka] Failed to publish CLIENT_PATCHED for clientId=${clientId}`, error);
    return res.status(500).jsonp({ message: 'Failed to publish client patched event' });
  }
});

server.delete('/clients/:id', async (req, res) => {
  const clientId = String(req.params.id);
  const existingClient = findClientById(req.params.id);

  if (!existingClient) {
    return res.status(404).jsonp({ message: `Client ${clientId} not found` });
  }

  const dependencyViolation = getDependencyViolationForClient(clientId);
  if (dependencyViolation) {
    return res.status(409).jsonp({ message: dependencyViolation });
  }

  try {
    getClientCollection().remove({ id: req.params.id }).write();
    await publishClientEvent('CLIENT_DELETED', clientId);
    return res.status(200).jsonp({ id: clientId, deleted: true });
  } catch (error) {
    console.error(`${FRAMEWORK_PREFIX}[Kafka] Failed to publish CLIENT_DELETED for clientId=${clientId}`, error);
    return res.status(500).jsonp({ message: 'Failed to publish client deleted event' });
  }
});

function registerEntityRoutes(config) {
  const {
    basePath,
    collectionName,
    topic,
    entityType,
    createdEventType,
    updatedEventType,
    patchedEventType,
    deletedEventType
  } = config;

  server.post(basePath, async (req, res) => {
    const entity = req.body;
    const entityId = String(entity.id);

    if (!entity.id) {
      return res.status(400).jsonp({ message: `${entityType} id is required` });
    }

    if (findById(collectionName, entity.id)) {
      return res.status(409).jsonp({ message: `${entityType} ${entityId} already exists` });
    }

    const relationshipViolation = validateRelationship(entityType, entity);
    if (relationshipViolation) {
      return res.status(400).jsonp({ message: relationshipViolation });
    }

    try {
      getCollection(collectionName).push(entity).write();
      await publishEntityEvent(topic, createdEventType, entityType, entityId, entity);
      return res.status(201).jsonp(entity);
    } catch (error) {
      console.error(`${FRAMEWORK_PREFIX}[Kafka] Failed to publish ${createdEventType} for ${entityType} entityId=${entityId}`, error);
      return res.status(500).jsonp({ message: `Failed to publish ${entityType} created event` });
    }
  });

  server.put(`${basePath}/:id`, async (req, res) => {
    const entityId = String(req.params.id);
    const existingEntity = findById(collectionName, req.params.id);

    if (!existingEntity) {
      return res.status(404).jsonp({ message: `${entityType} ${entityId} not found` });
    }

    const updatedEntity = {
      ...req.body,
      id: req.params.id
    };

    const relationshipViolation = validateRelationship(entityType, updatedEntity);
    if (relationshipViolation) {
      return res.status(400).jsonp({ message: relationshipViolation });
    }

    try {
      getCollection(collectionName).find({ id: req.params.id }).assign(updatedEntity).write();
      await publishEntityEvent(topic, updatedEventType, entityType, entityId, updatedEntity);
      return res.status(200).jsonp(updatedEntity);
    } catch (error) {
      console.error(`${FRAMEWORK_PREFIX}[Kafka] Failed to publish ${updatedEventType} for ${entityType} entityId=${entityId}`, error);
      return res.status(500).jsonp({ message: `Failed to publish ${entityType} updated event` });
    }
  });

  server.patch(`${basePath}/:id`, async (req, res) => {
    const entityId = String(req.params.id);
    const existingEntity = findById(collectionName, req.params.id);

    if (!existingEntity) {
      return res.status(404).jsonp({ message: `${entityType} ${entityId} not found` });
    }

    const patchedEntity = mergeDeep(existingEntity, req.body);
    patchedEntity.id = req.params.id;

    const relationshipViolation = validateRelationship(entityType, patchedEntity);
    if (relationshipViolation) {
      return res.status(400).jsonp({ message: relationshipViolation });
    }

    try {
      getCollection(collectionName).find({ id: req.params.id }).assign(patchedEntity).write();
      await publishEntityEvent(topic, patchedEventType, entityType, entityId, patchedEntity);
      return res.status(200).jsonp(patchedEntity);
    } catch (error) {
      console.error(`${FRAMEWORK_PREFIX}[Kafka] Failed to publish ${patchedEventType} for ${entityType} entityId=${entityId}`, error);
      return res.status(500).jsonp({ message: `Failed to publish ${entityType} patched event` });
    }
  });

  server.delete(`${basePath}/:id`, async (req, res) => {
    const entityId = String(req.params.id);
    const existingEntity = findById(collectionName, req.params.id);

    if (!existingEntity) {
      return res.status(404).jsonp({ message: `${entityType} ${entityId} not found` });
    }

    if (entityType === 'ACCOUNT') {
      const dependencyViolation = getDependencyViolationForAccount(entityId);
      if (dependencyViolation) {
        return res.status(409).jsonp({ message: dependencyViolation });
      }
    }

    try {
      getCollection(collectionName).remove({ id: req.params.id }).write();
      await publishEntityEvent(topic, deletedEventType, entityType, entityId);
      return res.status(200).jsonp({ id: entityId, deleted: true });
    } catch (error) {
      console.error(`${FRAMEWORK_PREFIX}[Kafka] Failed to publish ${deletedEventType} for ${entityType} entityId=${entityId}`, error);
      return res.status(500).jsonp({ message: `Failed to publish ${entityType} deleted event` });
    }
  });
}

registerEntityRoutes({
  basePath: '/accounts',
  collectionName: 'accounts',
  topic: topics.account,
  entityType: 'ACCOUNT',
  createdEventType: 'ACCOUNT_CREATED',
  updatedEventType: 'ACCOUNT_UPDATED',
  patchedEventType: 'ACCOUNT_PATCHED',
  deletedEventType: 'ACCOUNT_DELETED'
});

registerEntityRoutes({
  basePath: '/portfolios',
  collectionName: 'portfolios',
  topic: topics.portfolio,
  entityType: 'PORTFOLIO',
  createdEventType: 'PORTFOLIO_CREATED',
  updatedEventType: 'PORTFOLIO_UPDATED',
  patchedEventType: 'PORTFOLIO_PATCHED',
  deletedEventType: 'PORTFOLIO_DELETED'
});

registerEntityRoutes({
  basePath: '/transactions',
  collectionName: 'transactions',
  topic: topics.transaction,
  entityType: 'TRANSACTION',
  createdEventType: 'TRANSACTION_CREATED',
  updatedEventType: 'TRANSACTION_UPDATED',
  patchedEventType: 'TRANSACTION_PATCHED',
  deletedEventType: 'TRANSACTION_DELETED'
});

server.use(router);

connectProducer()
  .then(() => {
    const httpServer = server.listen(3000, () => {
      console.log(`${FRAMEWORK_PREFIX} AI Kafka Validator initialized. JSON Server is running with mock authentication`);
    });

    process.on('SIGTERM', async () => {
      console.log(`${FRAMEWORK_PREFIX} SIGTERM received. Shutting down gracefully...`);
      await disconnectProducer();
      httpServer.close(() => {
        process.exit(0);
      });
    });
  })
  .catch(error => {
    console.error(`${FRAMEWORK_PREFIX}[Kafka] Unable to start server because the Kafka producer could not connect`, error);
    process.exit(1);
  });

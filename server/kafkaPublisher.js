const { Kafka, logLevel } = require('kafkajs');

const bootstrapServers = (process.env.KAFKA_BOOTSTRAP_SERVERS || 'localhost:9092')
  .split(',')
  .map(server => server.trim())
  .filter(Boolean);
const clientTopic = process.env.KAFKA_CLIENT_EVENTS_TOPIC || 'client-events';
const accountTopic = process.env.KAFKA_ACCOUNT_EVENTS_TOPIC || 'account-events';
const portfolioTopic = process.env.KAFKA_PORTFOLIO_EVENTS_TOPIC || 'portfolio-events';
const transactionTopic = process.env.KAFKA_TRANSACTION_EVENTS_TOPIC || 'transaction-events';
const FRAMEWORK_PREFIX = '[AI-KAFKA-VALIDATOR]';

const kafka = new Kafka({
  clientId: 'ai-kafka-validator-mock-service',
  brokers: bootstrapServers,
  logLevel: logLevel.NOTHING
});

const producer = kafka.producer();
let connectPromise;

function buildEvent(eventType, clientId, payload) {
  const event = {
    eventType,
    entityType: 'CLIENT',
    clientId: String(clientId),
    timestamp: new Date().toISOString()
  };

  if (payload) {
    event.payload = payload;
  }

  return event;
}

async function connectProducer() {
  if (!connectPromise) {
    connectPromise = producer.connect()
      .then(() => {
        console.log(`${FRAMEWORK_PREFIX}[Kafka] Producer connected to ${bootstrapServers.join(', ')} and ready for topics ${[
          clientTopic,
          accountTopic,
          portfolioTopic,
          transactionTopic
        ].join(', ')}`);
      })
      .catch(error => {
        connectPromise = null;
        throw error;
      });
  }

  return connectPromise;
}

async function publishClientEvent(eventType, clientId, payload) {
  const event = buildEvent(eventType, clientId, payload);

  await connectProducer();
  await sendMessage(clientTopic, clientId, event);

  console.log(`${FRAMEWORK_PREFIX}[Kafka] Published ${eventType} for clientId=${clientId} to topic=${clientTopic}`);
}

function buildEntityEvent(eventType, entityType, entityId, payload) {
  const event = {
    eventType,
    entityType,
    entityId: String(entityId),
    timestamp: new Date().toISOString()
  };

  if (payload) {
    event.payload = payload;
  }

  return event;
}

async function sendMessage(topic, key, event) {
  await connectProducer();
  await producer.send({
    topic,
    messages: [
      {
        key: String(key),
        value: JSON.stringify(event)
      }
    ]
  });
}

async function publishEntityEvent(topic, eventType, entityType, entityId, payload) {
  const event = buildEntityEvent(eventType, entityType, entityId, payload);

  await sendMessage(topic, entityId, event);
  console.log(`${FRAMEWORK_PREFIX}[Kafka] Published ${eventType} for entityType=${entityType} entityId=${entityId} to topic=${topic}`);
}

async function disconnectProducer() {
  if (connectPromise) {
    await producer.disconnect();
    connectPromise = null;
    console.log(`${FRAMEWORK_PREFIX}[Kafka] Producer disconnected`);
  }
}

module.exports = {
  connectProducer,
  disconnectProducer,
  publishClientEvent,
  publishEntityEvent,
  topics: {
    client: clientTopic,
    account: accountTopic,
    portfolio: portfolioTopic,
    transaction: transactionTopic
  }
};

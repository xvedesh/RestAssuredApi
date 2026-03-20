const fs = require('fs');
const path = require('path');

const seedFilePath = path.join(__dirname, 'seed-data.json');
const runtimeFilePath = path.join(__dirname, 'clients.json');

fs.copyFileSync(seedFilePath, runtimeFilePath);
console.log('[Data] Runtime dataset restored from seed-data.json');

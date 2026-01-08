const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Tap = sequelize.define('Tap', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true
  },
  pairId: {
    type: DataTypes.STRING,
    allowNull: false,
    index: true
  },
  fromUserId: {
    type: DataTypes.UUID,
    allowNull: false,
    index: true
  },
  toUserId: {
    type: DataTypes.UUID,
    allowNull: false,
    index: true
  },
  timestamp: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    index: true
  },
  synced: {
    type: DataTypes.BOOLEAN,
    defaultValue: true
  },
  tapType: {
    type: DataTypes.STRING,
    allowNull: true,
    defaultValue: 'LOVING_MISS' // Default to loving miss
  },
  customEmoji: {
    type: DataTypes.STRING,
    allowNull: true,
    validate: {
      len: [0, 50] // Max 50 characters (allowing for multi-byte emojis)
    }
  },
  message: {
    type: DataTypes.STRING,
    allowNull: true,
    validate: {
      len: [0, 100] // Max 100 characters
    }
  }
}, {
  tableName: 'taps',
  indexes: [
    { fields: ['pairId', 'fromUserId', 'timestamp'] },
    { fields: ['pairId', 'timestamp'] },
    { fields: ['fromUserId', 'timestamp'] }
  ]
});

module.exports = Tap;

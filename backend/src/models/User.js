const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const User = sequelize.define('User', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true
  },
  deviceId: {
    type: DataTypes.STRING,
    allowNull: false,
    unique: true,
    index: true
  },
  fcmToken: {
    type: DataTypes.STRING,
    allowNull: true,
    index: true
  },
  pairId: {
    type: DataTypes.STRING,
    allowNull: true,
    index: true
  },
  userCode: {
    type: DataTypes.STRING,
    allowNull: true,
    unique: true,
    index: true,
    validate: {
      len: [6, 20] // 6-20 characters
    }
  },
  premium: {
    type: DataTypes.BOOLEAN,
    defaultValue: false
  },
  createdAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW
  },
  updatedAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW
  }
}, {
  tableName: 'users',
  indexes: [
    { fields: ['deviceId'] },
    { fields: ['pairId'] },
    { fields: ['onesignalPlayerId'] }
  ]
});

module.exports = User;

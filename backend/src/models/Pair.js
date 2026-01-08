const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Pair = sequelize.define('Pair', {
  id: {
    type: DataTypes.STRING,
    primaryKey: true,
    allowNull: false
  },
  user1Id: {
    type: DataTypes.UUID,
    allowNull: false,
    references: {
      model: 'users',
      key: 'id'
    }
  },
  user2Id: {
    type: DataTypes.UUID,
    allowNull: true,
    references: {
      model: 'users',
      key: 'id'
    }
  },
  lastTapAt: {
    type: DataTypes.DATE,
    allowNull: true
  },
  lastTapAtReverse: {
    type: DataTypes.DATE,
    allowNull: true
  },
  streak: {
    type: DataTypes.INTEGER,
    defaultValue: 0,
    allowNull: false
  },
  lastStreakDate: {
    type: DataTypes.DATE,
    allowNull: true
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
  tableName: 'pairs',
  indexes: [
    { fields: ['user1Id'] },
    { fields: ['user2Id'] }
  ]
});

module.exports = Pair;

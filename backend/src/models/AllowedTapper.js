const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const AllowedTapper = sequelize.define('AllowedTapper', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true
  },
  userId: {
    type: DataTypes.UUID,
    allowNull: false,
    index: true,
    references: {
      model: 'users',
      key: 'id'
    }
  },
  tapperUserId: {
    type: DataTypes.UUID,
    allowNull: false,
    index: true,
    references: {
      model: 'users',
      key: 'id'
    }
  },
  tapperUserCode: {
    type: DataTypes.STRING,
    allowNull: false,
    index: true
  },
  createdAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW
  }
}, {
  tableName: 'allowed_tappers',
  indexes: [
    { fields: ['userId', 'tapperUserId'], unique: true },
    { fields: ['userId'] },
    { fields: ['tapperUserId'] },
    { fields: ['tapperUserCode'] }
  ]
});

module.exports = AllowedTapper;

const { Sequelize } = require('sequelize');
require('dotenv').config();

// Support both DATABASE_URL and individual env vars
const getDatabaseUrl = () => {
  if (process.env.DATABASE_URL) {
    return process.env.DATABASE_URL;
  }
  
  const user = process.env.DB_USER || 'tapme_user';
  const password = process.env.DB_PASSWORD || 'password';
  const host = process.env.DB_HOST || 'localhost';
  const port = process.env.DB_PORT || '5432';
  const database = process.env.DB_NAME || 'tapme_dev';
  
  return `postgresql://${user}:${password}@${host}:${port}/${database}`;
};

const sequelize = new Sequelize(
  getDatabaseUrl(),
  {
    dialect: 'postgres',
    logging: process.env.NODE_ENV === 'development' ? console.log : false,
    pool: {
      max: 20,
      min: 0,
      acquire: 30000,
      idle: 10000
    },
    dialectOptions: {
      ssl: process.env.NODE_ENV === 'production' ? {
        require: true,
        rejectUnauthorized: false
      } : false
    }
  }
);

// Test connection
sequelize.authenticate()
  .then(() => console.log('✅ Database connected'))
  .catch(err => console.error('❌ Database connection error:', err));

module.exports = sequelize;

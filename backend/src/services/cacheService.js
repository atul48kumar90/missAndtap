const redis = require('redis');
require('dotenv').config();

let redisClient = null;

// Initialize Redis client
if (process.env.REDIS_ENABLED === 'true' && process.env.REDIS_URL) {
  redisClient = redis.createClient({
    url: process.env.REDIS_URL
  });

  redisClient.on('error', (err) => {
    console.error('Redis Client Error:', err);
  });

  redisClient.connect().then(() => {
    console.log('✅ Redis connected');
  }).catch(err => {
    console.error('❌ Redis connection failed:', err);
    redisClient = null;
  });
}

class CacheService {
  // Get cached value
  async get(key) {
    if (!redisClient) return null;
    
    try {
      const value = await redisClient.get(key);
      return value ? JSON.parse(value) : null;
    } catch (error) {
      console.error('Cache get error:', error);
      return null;
    }
  }

  // Set cached value with TTL
  async set(key, value, ttlSeconds = 300) {
    if (!redisClient) return false;
    
    try {
      await redisClient.setEx(key, ttlSeconds, JSON.stringify(value));
      return true;
    } catch (error) {
      console.error('Cache set error:', error);
      return false;
    }
  }

  // Delete cached value
  async del(key) {
    if (!redisClient) return false;
    
    try {
      await redisClient.del(key);
      return true;
    } catch (error) {
      console.error('Cache delete error:', error);
      return false;
    }
  }

  // Delete multiple keys with pattern
  async delPattern(pattern) {
    if (!redisClient) return false;
    
    try {
      const keys = await redisClient.keys(pattern);
      if (keys.length > 0) {
        await redisClient.del(keys);
      }
      return true;
    } catch (error) {
      console.error('Cache delete pattern error:', error);
      return false;
    }
  }

  // Cache pair data
  async getPair(pairId) {
    return this.get(`pair:${pairId}`);
  }

  async setPair(pairId, pairData, ttl = 300) {
    return this.set(`pair:${pairId}`, pairData, ttl);
  }

  async invalidatePair(pairId) {
    return this.del(`pair:${pairId}`);
  }

  // Cache user data
  async getUser(userId) {
    return this.get(`user:${userId}`);
  }

  async setUser(userId, userData, ttl = 600) {
    return this.set(`user:${userId}`, userData, ttl);
  }

  // Cache tap stats
  async getTapStats(pairId, userId) {
    return this.get(`stats:${pairId}:${userId}`);
  }

  async setTapStats(pairId, userId, stats, ttl = 60) {
    return this.set(`stats:${pairId}:${userId}`, stats, ttl);
  }

  // Check if Redis is available
  isAvailable() {
    return redisClient !== null;
  }
}

module.exports = new CacheService();

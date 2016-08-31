package com.eryikuaipin.admin.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * redis save shiro session class
 *
 * @author WangRan
 */
@Component
public class JedisShiroSessionRepository implements ShiroSessionRepository {

    private static final String REDIS_SHIRO_SESSION = "shiro-session:";
    private static final int SESSION_VAL_TIME_SPAN = 18000;
    private static final int DB_INDEX = 0;

    @Autowired
    private RedisTemplate<?, ?> redisTemplate;
    @Autowired
    private JedisManager jedisManager;

    @Override
    public void saveSession(Session session) {
    	if (session == null || session.getId() == null)
            throw new NullPointerException("session is empty");
        try {
            byte[] key = redisTemplate.getStringSerializer().serialize(buildRedisSessionKey(session.getId()));
            byte[] value = SerializeUtil.serialize(session);
            long sessionTimeOut = session.getTimeout() / 1000;
            Long expireTime = sessionTimeOut + SESSION_VAL_TIME_SPAN + (5 * 60);
            getJedisManager().saveValueByKey(DB_INDEX, key, value, expireTime.intValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSession(Serializable id) {
        if (id == null) {
            throw new NullPointerException("session id is empty");
        }
        try {
            getJedisManager().deleteByKey(DB_INDEX,
            		redisTemplate.getStringSerializer().serialize(buildRedisSessionKey(id)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Session getSession(Serializable id) {
        if (id == null)
            throw new NullPointerException("session id is empty");
        Session session = null;
        try {
        	
            byte[] value = getJedisManager().getValueByKey(DB_INDEX, redisTemplate.getStringSerializer().serialize(buildRedisSessionKey(id)));
            session = SerializeUtil.deserialize(value, Session.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return session;
    }

    @Override
    public Collection<Session> getActiveSessions() {
    	return null;
    }

    private String buildRedisSessionKey(Serializable sessionId) {
        return REDIS_SHIRO_SESSION + sessionId;
    }

    public JedisManager getJedisManager() {
        return jedisManager;
    }

    public void setJedisManager(JedisManager jedisManager) {
        this.jedisManager = jedisManager;
    }
}

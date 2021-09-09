package com.Roclh.Infotecs.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@JsonPropertyOrder({"key", "value", "remaininglifespan"})
public class KeyValue {
    private static final long defaultTtl = 10000;
    private String key;
    private String value;
    @JsonIgnore
    private long deathTime;


    /**
     * Main constructor of the KeyValue object
     *
     * @param key
     * @param value
     * @param remainingLifespan   The time that the object will exist
     */
    public KeyValue(String key, String value, long remainingLifespan) {
        this.key = key;
        this.value = value;
        this.deathTime = System.currentTimeMillis() + remainingLifespan;
    }

    /**
     * Json constructor of the KeyValue object
     *
     * @param json JSON-formatted String that contains object needed to convert
     */
    public KeyValue(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = mapper.readValue(json, new TypeReference<Map<String,Object>>(){});
            this.key = (String)jsonMap.get("key");
            this.value = (String)jsonMap.get("value");
            this.deathTime = System.currentTimeMillis()+(Integer)jsonMap.get("remainingLifespan");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that checks if this object should be already extinct
     *
     * @return True if the object should be extinct, else False
     */
    public boolean checkTime() {
        return System.currentTimeMillis() >= deathTime;
    }

    /**
     * Method that get remaining lifespan of the object for dumping
     *
     * @return remaining lifespan if its more than 0 ms, else 0;
     */
    public long getRemainingLifespan() {
        if (!checkTime()) {
            return deathTime - System.currentTimeMillis();
        } else {
            return 0;
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getDeathTime() {
        return deathTime;
    }

    public void setDeathTime(long ttl) {
        this.deathTime = System.currentTimeMillis() + ttl;
    }

    public void setDeathTime() {
        this.deathTime = System.currentTimeMillis() + defaultTtl;
    }

    public static long getDefaultTtl() {
        return defaultTtl;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}

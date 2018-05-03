package com.tencent.qcloud.roomservice.webrtc.pojo;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.concurrent.ConcurrentHashMap;

public class WebRTCRoom {
    private String roomID = "";
    private String roomInfo = "";
    private ConcurrentHashMap<String, Long> pushersMap = new ConcurrentHashMap<>();

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomInfo() {
        return roomInfo;
    }

    public void setRoomInfo(String roomInfo) {
        this.roomInfo = roomInfo;
    }


    @JsonIgnore
    public int getPushersCnt() {
        return pushersMap.size();
    }

    @JsonIgnore
    public ConcurrentHashMap<String, Long> getPushersMap() {
        return this.pushersMap;
    }

    public void addPusher(String userID) {
        this.pushersMap.put(userID, System.currentTimeMillis()/1000);
    }

    public void updatePusher(String userID) {
        this.pushersMap.put(userID, System.currentTimeMillis()/1000);
    }

    @JsonIgnore
    public boolean isPusher(String userID) {
        return this.pushersMap.containsKey(userID);
    }

    public void delPusher(String userID) {
        this.pushersMap.remove(userID);
    }
}

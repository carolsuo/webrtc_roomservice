package com.tencent.qcloud.roomservice.webrtc.logic;

import com.tencent.qcloud.roomservice.webrtc.common.Config;
import com.tencent.qcloud.roomservice.webrtc.pojo.WebRTCRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebRTCRoomMgr  implements InitializingBean {
    private ConcurrentHashMap<String, WebRTCRoom> webRTCRoomMap = new ConcurrentHashMap<>();
    private static Logger log = LoggerFactory.getLogger(WebRTCRoomMgr.class);

    private HeartTimer heartTimer = new HeartTimer();
    private Timer timer = null;

    @Override
    public void afterPropertiesSet() throws Exception {
// 开启心跳检查定时器
        if (timer == null) {
            timer = new Timer();
            timer.schedule(heartTimer, 5 * 1000, 5 * 1000);
        }
    }

    public class HeartTimer extends TimerTask {
        @Override
        public void run() {
            onTimer();
        }
    }

    private void onTimer() {
        // 遍历房间每个成员，检查pusher的时间戳是否超过timeout
        long currentTS = System.currentTimeMillis()/1000;
        int timeout = Config.WebRTCRoom.heartBeatTimeout;

        for (WebRTCRoom room : webRTCRoomMap.values()) {
            Iterator<Map.Entry<String, Long>> entries = room.getPushersMap().entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Long> entry = entries.next();
                if (currentTS - entry.getValue() > timeout) {
                    entries.remove();
                }
            }
        }
    }

    /**
     * 获取房间列表
     */
    public ArrayList<WebRTCRoom> getList(int cnt, int index) {
        ArrayList<WebRTCRoom> resultList = new ArrayList<>();
        int cursor = 0;
        int roomCnt = 0;

        //遍历
        for (WebRTCRoom value : webRTCRoomMap.values()) {
            if (roomCnt >= cnt)
                break;

            log.info("getRoomList roomID: " + value.getRoomID() + ", pushers count: " + value.getPushersCnt());

            if (value.getPushersCnt() != 0) {
                if (cursor >= index) {
                    resultList.add(value);
                    ++roomCnt;
                } else {
                    ++cursor;
                    continue;
                }
            } else {
                webRTCRoomMap.remove(value.getRoomID());
            }
        }

        return resultList;
    }


    /**
     * 创建房间
     */
    public void creatRoom(String roomID, String userID, String roomInfo) {
        WebRTCRoom webRTCRoom = new WebRTCRoom();
        webRTCRoom.setRoomID(roomID);
        webRTCRoom.setRoomInfo(roomInfo);
        webRTCRoom.addPusher(userID);
        log.info("creatRoom roomID: " + roomID + ", userID: " + userID);
        webRTCRoomMap.put(roomID, webRTCRoom);
    }

    /**
     * 房间是否存在
     */
    public boolean isRoomExist(String roomID) {
        return webRTCRoomMap.containsKey(roomID);
    }

    /**
     * 用户是否在房间中
     */
    public boolean isPusherExist(String roomID, String userID) {
        WebRTCRoom webRTCRoom = webRTCRoomMap.get(roomID);
        if (webRTCRoom != null) {
            return webRTCRoom.isPusher(userID);
        }
        return false;
    }


    /**
     * 心跳更新
     */
    public void updateTimeStamp(String roomID, String userID) {
        WebRTCRoom webRTCRoom = webRTCRoomMap.get(roomID);
        if (webRTCRoom != null && webRTCRoom.isPusher(userID)) {
            webRTCRoom.updatePusher(userID);
        }
    }

    /**
     * 获取房间推流者人数
     */
    public int getMemberCnt(String roomID) {
        int count = 0;
        WebRTCRoom webRTCRoom = webRTCRoomMap.get(roomID);
        if (webRTCRoom != null) {
            count = webRTCRoom.getPushersCnt();
        }
        return count;
    }

    /**
     * 删除推流者
     */
    public void delPusher(String roomID, String userID) {
        WebRTCRoom webRTCRoom = webRTCRoomMap.get(roomID);
        if (webRTCRoom != null) {
            webRTCRoom.delPusher(userID);
            log.info("delPusher roomID: " + roomID + ", userID: " + userID);
        }
    }

    public void addPusher(String roomID, String userID) {
        WebRTCRoom webRTCRoom = webRTCRoomMap.get(roomID);
        if (webRTCRoom != null) {
            webRTCRoom.addPusher(userID);
            log.info("addPusher roomID: " + roomID + ", userID: " + userID);
        }
    }
}

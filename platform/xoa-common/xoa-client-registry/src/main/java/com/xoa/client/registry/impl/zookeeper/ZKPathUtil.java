package com.xoa.client.registry.impl.zookeeper;

public class ZKPathUtil {
    public static final String ENABLE = "enable";
    public static final String DISABLE = "disable";
    public static final String ALARM_EMAIL = "alarmemail";

    /**
     * 给定serviceId，计算出其对应的path，相对路径
     * <p/>
     * 可用节点
     *
     * @param serviceId
     * @return
     */
    public static String serviceIdToEnablePath(String serviceId) {
        return "/" + serviceId + "/" + ENABLE;
    }

    /**
     * 给定serviceId，计算出其对应的path，相对路径
     * <p/>
     * 不可用节点
     *
     * @param serviceId
     * @return
     */
    public static String serviceIdToDisablePath(String serviceId) {
        return "/" + serviceId + "/" + DISABLE;
    }


    /**
     * 给定serviceId，计算出其对应的path，相对路径
     * <p/>
     * 报警短信
     *
     * @param serviceId
     * @return
     */
    public static String getAlarmEmailsByServiceId(String serviceId) {
        return "/" + serviceId + "/" + ALARM_EMAIL;
    }
}

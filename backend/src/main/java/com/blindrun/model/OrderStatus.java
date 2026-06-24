package com.blindrun.model;

public enum OrderStatus {
    PENDING("pending"),      // 待开始
    RUNNING("running"),      // 进行中
    COMPLETED("completed"),  // 已完成
    CANCELLED("cancelled");  // 已取消

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 判断是否为终态（不能再改变的状态）
     */
    public static boolean isFinalState(String status) {
        return COMPLETED.getValue().equals(status) || CANCELLED.getValue().equals(status);
    }

    /**
     * 判断状态转换是否合法
     */
    public static boolean isValidTransition(String fromStatus, String toStatus) {
        if (isFinalState(fromStatus)) {
            return false; // 终态不能转换到其他状态
        }
        
        // pending -> cancelled (合法)
        // pending -> running (合法)
        // running -> completed (合法)
        // running -> cancelled (合法)
        return true;
    }
}

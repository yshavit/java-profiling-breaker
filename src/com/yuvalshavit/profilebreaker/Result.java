package com.yuvalshavit.profilebreaker;

public class Result {
    public final int value;
    public final long setupTime;
    public final long spinTime;
    public final long workTime;

    public Result(int value, long setupTime, long workTime, long spinTime) {
        this.value = value;
        this.setupTime = setupTime;
        this.workTime = workTime;
        this.spinTime = spinTime;
    }

    @Override
    public String toString() {
        return String.format("setup: %.3f\twork: %.3f\tspin: %.3f\tvalue: %d",
            setupTime/1000000D,
            workTime/1000000D,
            spinTime/1000000D,
            value
        );
    }
}

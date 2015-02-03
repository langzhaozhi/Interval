package org.langzhaozhi.interval;

/**
 * 32 位无符号整数型闭区间[start,end]
 *
 * @param <T> 此区间的绑定对象,由应用做出解释
 */
public final class Interval<T> {
    public final T mAttachment;
    /**
     * 无符号32位的小值，外部要用 (mStart & 0xFFFFFFFFL) 转成long型取得此无符号整型值
     */
    public final long mStart;
    /**
     * 无符号32位的大值，外部要用 (mStart & 0xFFFFFFFFL) 转成long型取得此无符号整型值
     */
    public final long mEnd;

    public Interval(T aAttachment, int aStart, int aEnd) {
        if ((aStart & 0xFFFF_FFFFL) > (aEnd & 0xFFFF_FFFFL)) {
            throw new IllegalArgumentException();
        }
        this.mAttachment = aAttachment;
        this.mStart = aStart & 0xFFFF_FFFFL;
        this.mEnd = aEnd & 0xFFFF_FFFFL;
    }

    public boolean contains(int aPoint) {
        long point = aPoint & 0xFFFF_FFFFL;
        return this.mStart <= point && point <= this.mEnd;
    }

    @Override
    public String toString() {
        return "[0x" + Long.toHexString( this.mStart & 0xFFFFFFFFL ) + ",0x" + Long.toHexString( this.mEnd & 0xFFFFFFFFL ) + "]:" + this.mAttachment;
    }
}

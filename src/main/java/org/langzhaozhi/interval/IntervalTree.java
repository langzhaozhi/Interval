package org.langzhaozhi.interval;

/**
 * <p>32位无符号整型区间划分树,对区间的划分意味着任意两个区间不相交。</p>
 * </p>所要解决的根本问题是：</p>
 * </p>&#160;&#160;&#160;&#160;<b>任意给定一个整数，如何快速判断其属于哪个划分的区间?</b></p>
 *
 * </p>问题源于找到一种极度快速的方法查找一个IP地址的归属地，
 * 普通的方法当然是相对快速的时间复杂度为O(logN)的二分折半查找法，但实测结果表明还是慢了，
 * 在某些超大规模反复查询的领域不满足要求。
 * 本方法给出的整数区间树可以使时间复杂度降低到O(1)，采用类似IP方式把32位整数分成最高8位、
 * 次高8位、次低8位、最低8位形成只有四层的区间树，每次查询恒定只需4个单步就立即匹配到要的结果。</p>
 *
 * <p>不变对象，意味着一旦构造就不再改变，因此可以任意多线程并发访问。</p>
 * @param <T>
 */
public final class IntervalTree<T> {
    static final int [] BIT_MOVE_COUNT = {
        24, 16, 8, 0
    };
    /**
     * 虚根节点
     */
    private IntervalNode<T> mRootNode;

    IntervalTree(IntervalNode<T> aRootNode) {
        this.mRootNode = aRootNode;
    }

    /**
     * 获取一个点aPoint所属的区间,这是递归调用,已经改造称下面的 getInterval 的非递归调用过程
     * @param aPoint 一个点
     * @return 参数aPoint所在的区间
     */
    Interval<T> getIntervalRecursive(int aPoint) {
        IntervalNode<T> searchNode = this.mRootNode.search( aPoint, 0 );
        Interval<T> interval = searchNode != null ? searchNode.mInterval : null;
        return interval != null ? interval.contains( aPoint ) ? interval : null : null;
    }

    /**
     * 获取一个点aPoint所属的区间,每次查询都是恒常的四步就立即匹配出结果
     * @param aPoint 一个点
     * @return 参数aPoint所在的区间
     */
    public Interval<T> getInterval(int aPoint) {
        IntervalNode<T> parentNode = this.mRootNode;
        for (int depth = 0;; ++depth) {
            //依次取最高8位、次高8位、次低8位、最低8位无符号值：恒常四步就是指对各个8位进行比较的次数恒定为4
            int depthChar = ((aPoint >>> IntervalTree.BIT_MOVE_COUNT[ depth ]) & 0xFF);
            if (depthChar < parentNode.mMin) {
                IntervalNode<T> searchNode = parentNode.mChildrenNodes[ 0 ];
                while (searchNode.mChildrenNodes != null) {
                    searchNode = searchNode.mChildrenNodes[ 0 ];
                }
                return searchNode.mInterval.contains( aPoint ) ? searchNode.mInterval : null;
            }
            else if (depthChar > parentNode.mMax) {
                //类似递归的回溯
                IntervalNode<T> rootNode = this.mRootNode;
                while (parentNode != rootNode) {
                    parentNode = parentNode.mParentNode;
                    depthChar = ((aPoint >>> IntervalTree.BIT_MOVE_COUNT[ --depth ]) & 0xFF) + 1;
                    if (depthChar <= parentNode.mMax) {
                        IntervalNode<T> searchNode = parentNode.mChildrenNodes[ depthChar - parentNode.mMin ];
                        while (searchNode.mChildrenNodes != null) {
                            searchNode = searchNode.mChildrenNodes[ 0 ];
                        }
                        return searchNode.mInterval.contains( aPoint ) ? searchNode.mInterval : null;
                    }
                }
                return null;
            }
            else {
                int levalChildIndex = depthChar - parentNode.mMin;
                IntervalNode<T> searchNode = parentNode.mChildrenNodes[ levalChildIndex ];
                if (depth == 3) {
                    return searchNode.mOwnerChar == depthChar ? searchNode.mInterval : searchNode.mInterval.contains( aPoint ) ? searchNode.mInterval : null;//此searchNode为第4层数据叶子节点
                }
                else {
                    if (searchNode.mOwnerChar != depthChar) {
                        while (searchNode.mChildrenNodes != null) {
                            searchNode = searchNode.mChildrenNodes[ 0 ];
                        }
                        return searchNode.mInterval.contains( aPoint ) ? searchNode.mInterval : null;
                    }
                    else {
                        parentNode = searchNode;
                    }
                }
            }
        }
    }

    static final class IntervalNode<T> {
        int mOwnerChar;
        int mMin, mMax;//当min==max时就是一个点,这里用int是取无符号8位整形
        IntervalNode<T> mParentNode;
        IntervalNode<T> [] mChildrenNodes;

        Interval<T> mInterval;

        IntervalNode(int aOwnerChar, int aMin, int aMax) {
            if (aMin > aMax) {
                throw new Error();
            }
            this.mOwnerChar = aOwnerChar;
            //父节点
            this.mMin = aMin;
            this.mMax = aMax;
            @SuppressWarnings("unchecked")
            IntervalNode<T> [] childrenNodes = new IntervalNode [ aMax - aMin + 1 ];
            this.mChildrenNodes = childrenNodes;
        }

        IntervalNode(int aChar, Interval<T> aInterval) {
            //叶子节点
            this.mOwnerChar = aChar;
            this.mMin = aChar;
            this.mMax = aChar;
            this.mInterval = aInterval;
        }

        IntervalNode<T> search(int aPoint, int aLevel) {
            //依次取最高8位、次高8位、次低8位、最低8位无符号值
            int depthChar = ((aPoint >>> IntervalTree.BIT_MOVE_COUNT[ aLevel ]) & 0xFF);
            if (depthChar < this.mMin) {
                IntervalNode<T> searchNode = this.mChildrenNodes[ 0 ];
                while (searchNode.mChildrenNodes != null) {
                    searchNode = searchNode.mChildrenNodes[ 0 ];
                }
                return searchNode;
            }
            else if (depthChar > this.mMax) {
                return null;
            }
            else {
                int levalChildIndex = depthChar - this.mMin;
                IntervalNode<T> searchNode = this.mChildrenNodes[ levalChildIndex ];
                if (aLevel == 3) {
                    return searchNode;//此searchNode为第4层数据叶子节点
                }
                else {
                    if (searchNode.mOwnerChar != depthChar) {
                        while (searchNode.mChildrenNodes != null) {
                            searchNode = searchNode.mChildrenNodes[ 0 ];
                        }
                        return searchNode;
                    }
                    else {
                        IntervalNode<T> findNode = searchNode.search( aPoint, aLevel + 1 );
                        if (findNode == null) {
                            if (levalChildIndex == this.mChildrenNodes.length - 1) {
                                return null;
                            }
                            else {
                                searchNode = this.mChildrenNodes[ levalChildIndex + 1 ];
                                while (searchNode.mChildrenNodes != null) {
                                    searchNode = searchNode.mChildrenNodes[ 0 ];
                                }
                                return searchNode;
                            }
                        }
                        else {
                            return findNode;
                        }
                    }
                }
            }
        }
    }
}

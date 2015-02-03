package org.langzhaozhi.interval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.langzhaozhi.interval.IntervalTree.IntervalNode;

/**
 * 采用类似DAT的fetch构造过程
 */
public class IntervalTreeMaker {
    public static <T> IntervalTree<T> makeIntervalTree(Interval<T> [] aIntervalArray) {
        if (aIntervalArray == null || aIntervalArray.length == 0) {
            throw new IllegalArgumentException( "aIntervalArray not valid" );
        }
        //按无符号32位整数排序
        Arrays.parallelSort( aIntervalArray, (aOne, aTwo) -> {
            long v = (aOne.mStart & 0xFFFF_FFFFL) - (aTwo.mStart & 0xFFFF_FFFFL);
            return v < 0 ? -1 : v == 0 ? 0 : 1;
        } );
        //检查区间相交的情况，目前暂时不支持区间相交：只支持对整数区间的划分，划分意味着任意两个区间都不重叠
        for (int i = 1; i < aIntervalArray.length; ++i) {
            if ((aIntervalArray[ i ].mStart & 0xFFFF_FFFFL) <= (aIntervalArray[ i - 1 ].mEnd & 0xFFFF_FFFFL)) {
                throw new IllegalArgumentException( "暂不支持区间重叠相交的情况" );
            }
        }
        MakeContext<T> context = new MakeContext<T>( aIntervalArray );
        LinkedList<ProccessingNode<T>> queueFetch = new LinkedList<ProccessingNode<T>>();
        queueFetch.add( context.mProccessingRootNode );
        while (!queueFetch.isEmpty()) {
            ProccessingNode<T> nextParentNode = queueFetch.removeFirst();
            IntervalTreeMaker.fetch( context, queueFetch, nextParentNode );
        }
        IntervalNode<T> rootIntervalNode = context.mProccessingRootNode.toIntervalNode();
        return new IntervalTree<T>( rootIntervalNode );
    }

    private static <T> void fetch(MakeContext<T> aContext, LinkedList<ProccessingNode<T>> aQueue, ProccessingNode<T> aParentNode) {
        //根据字典序构造下层Trie结构,类似DAT构造过程, childrenNodeList 就是 aParentNode 的儿子
        Interval<T> [] valueArray = aContext.mIntervalArray;
        ArrayList<ProccessingNode<T>> childrenNodeList = null;
        for (int i = aParentNode.mLeft, size = aParentNode.mRight, preChar = -1, parentDepth = aParentNode.mDepth, childDepth = parentDepth + 1; i < size; ++i) {
            Interval<T> nextValue = valueArray[ i ];
            int childChar = ( int )((nextValue.mEnd >> IntervalTree.BIT_MOVE_COUNT[ parentDepth ]) & 0xFF);//从0,1,2,3共4层
            if (childChar != preChar) {
                if (childChar < preChar) {
                    throw new Error();
                }
                ProccessingNode<T> nextChildNode = new ProccessingNode<T>( childChar, childDepth, i );
                if (childrenNodeList == null) {
                    childrenNodeList = aContext.mCacheChildNodeList;
                }
                childrenNodeList.add( nextChildNode );
                if (childDepth == 4) {
                    //这个是数据节点
                    nextChildNode.mInterval = nextValue;
                }
                else {
                    //加入bfs遍历队列
                    aQueue.add( nextChildNode );
                }
                preChar = childChar;
            }
        }
        if (childrenNodeList != null) {
            @SuppressWarnings("unchecked")
            ProccessingNode<T> [] childrenNodes = childrenNodeList.toArray( new ProccessingNode [ childrenNodeList.size() ] );
            int lastChildIndex = childrenNodes.length - 1;
            for (int i = 0; i < lastChildIndex; ++i) {
                //标示各个儿子的mRight为下一个儿子的mLeft
                childrenNodes[ i ].mRight = childrenNodes[ i + 1 ].mLeft;
            }
            //设置最后一个儿子的mRight为父亲的mRight
            childrenNodes[ lastChildIndex ].mRight = aParentNode.mRight;
            //记录下父子关系
            aParentNode.mChildrenNodes = childrenNodes;
            //清理aContext.mCacheChildNodeList以备下次继续用
            childrenNodeList.clear();
        }
    }

    private static final class ProccessingNode<T> {
        private int mChar;
        private int mDepth;
        private int mLeft;
        private int mRight;
        private ProccessingNode<T> [] mChildrenNodes;
        private Interval<T> mInterval;

        ProccessingNode(int aTotalCount) {
            //虚根
            this.mChar = '\0';
            this.mDepth = 0;
            this.mLeft = 0;
            this.mRight = aTotalCount;
        }

        ProccessingNode(int aChar, int aDepth, int aLeft) {
            //子节点
            this.mChar = aChar;
            this.mDepth = aDepth;
            this.mLeft = aLeft;
        }

        IntervalNode<T> toIntervalNode() {
            if (this.mInterval == null) {
                ProccessingNode<T> [] processingChildrenNodes = this.mChildrenNodes;
                int lastProccessingChildIndex = processingChildrenNodes.length - 1;
                ProccessingNode<T> firstProcessingChildNode = processingChildrenNodes[ 0 ];
                ProccessingNode<T> lastProcessingChildNode = processingChildrenNodes[ lastProccessingChildIndex ];
                IntervalNode<T> thisIntervalNode = new IntervalNode<T>( this.mChar, firstProcessingChildNode.mChar, lastProcessingChildNode.mChar );
                for (int i = 0; i <= lastProccessingChildIndex; ++i) {
                    ProccessingNode<T> nextProcessingChildNode = processingChildrenNodes[ i ];
                    IntervalNode<T> nextIntervalChildNode = nextProcessingChildNode.toIntervalNode();
                    nextIntervalChildNode.mParentNode = thisIntervalNode;
                    int nextIntervalChildIndex = nextProcessingChildNode.mChar - firstProcessingChildNode.mChar;
                    thisIntervalNode.mChildrenNodes[ nextIntervalChildIndex ] = nextIntervalChildNode;
                    for (int j = nextIntervalChildIndex - 1; j >= 0 && thisIntervalNode.mChildrenNodes[ j ] == null; --j) {
                        //这里是非常关键的优化
                        //不仅在 nextIntervalChildIndex 位置上插入，而且把前面空隙部分填成此儿子以大大加快查找匹配的速度
                        thisIntervalNode.mChildrenNodes[ j ] = nextIntervalChildNode;
                    }
                }
                return thisIntervalNode;
            }
            else {
                return new IntervalNode<T>( this.mChar, this.mInterval );
            }
        }
    }

    /**
     * 构建上下文
     *
     * @param <T>
     */
    private static final class MakeContext<T> {
        Interval<T> [] mIntervalArray;
        ProccessingNode<T> mProccessingRootNode;
        ArrayList<ProccessingNode<T>> mCacheChildNodeList;

        MakeContext(Interval<T> [] aIntervalArray) {
            this.mIntervalArray = aIntervalArray;
            this.mProccessingRootNode = new ProccessingNode<T>( aIntervalArray.length );
            //避免每次创建用途的cache
            this.mCacheChildNodeList = new ArrayList<ProccessingNode<T>>( aIntervalArray.length );
        }
    }
}

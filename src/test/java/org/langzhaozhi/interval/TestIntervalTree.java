package org.langzhaozhi.interval;

public class TestIntervalTree {
    public static void main(String [] args) {
        TestIntervalTree.simpleTest();
        TestIntervalTree.simpleTest2();
        System.err.println( "====================================================" );
    }

    private static void simpleTest() {
        System.err.println( "================测试单个区间情况======================" );
        @SuppressWarnings("unchecked")
        Interval<String> [] singleInterval = new Interval [] {
            new Interval<String>( "所在区间[5,8]", 5, 8 )
        };
        IntervalTree<String> tree = IntervalTreeMaker.makeIntervalTree( singleInterval );
        System.err.println( "4-->" + tree.getInterval( 4 ) );//null
        System.err.println( "5-->" + tree.getInterval( 5 ) );//ok
        System.err.println( "6-->" + tree.getInterval( 6 ) );//ok
        System.err.println( "7-->" + tree.getInterval( 7 ) );//ok
        System.err.println( "8-->" + tree.getInterval( 8 ) );//ok
        System.err.println( "9-->" + tree.getInterval( 9 ) );//null
    }

    private static void simpleTest2() {
        System.err.println( "================测试两个区间情况======================" );
        @SuppressWarnings("unchecked")
        Interval<String> [] intervals = new Interval [] {
            new Interval<String>( "所在区间[5,8]", 5, 8 ), new Interval<String>( "所在区间[0xFF00_0000,0xFF00_FF00]", 0xFF00_0000, 0xFF00_FF00 )
        };
        IntervalTree<String> tree = IntervalTreeMaker.makeIntervalTree( intervals );
        System.err.println( "4-->" + tree.getInterval( 4 ) );//null
        System.err.println( "5-->" + tree.getInterval( 5 ) );//ok
        System.err.println( "6-->" + tree.getInterval( 6 ) );//ok
        System.err.println( "7-->" + tree.getInterval( 7 ) );//ok
        System.err.println( "8-->" + tree.getInterval( 8 ) );//ok
        System.err.println( "9-->" + tree.getInterval( 9 ) );//null

        System.err.println( "(0xFF00_0000-1)-->" + tree.getInterval( 0xFF00_0000 - 1 ) );//null
        boolean allOK = true;
        for (int i = 0xFF00_0000; i <= 0xFF00_FF00; ++i) {
            if (tree.getInterval( i ) != intervals[ 1 ]) {
                System.err.println( "Error: 0x" + Integer.toHexString( i ) );
                allOK = false;
            }
        }
        if (allOK) {
            System.err.println( "    区间[0xFF00_0000,0xFF00_FF00]内每个数查询都正确!" );
        }
        System.err.println( "(0xFF00_FF00+1)-->" + tree.getInterval( 0xFF00_FF00 + 1 ) );//null
    }
}

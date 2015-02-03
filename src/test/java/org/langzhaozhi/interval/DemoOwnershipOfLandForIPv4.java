package org.langzhaozhi.interval;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 演示IPv4 32位地址归属地极快查询的应用，采用整数区间树结构
 *
 * 资源文件为 resources目录下的 ipv4-global-utf8.txt, 是一个有122905个区间的全球地址库，是一个比较粗略的库，
 * 但至少每个IPv4地址可以粗略从中查询到其所属的归属地在哪里。这是UTF-8 格式的文本文件，每行是一个地址区间。
 *
 * 也实现了一个二分折半算法来进行比较速度，实际结果表明，区间树算法大约是二分折半算法的5倍速度，效果相当不错。
 */
public class DemoOwnershipOfLandForIPv4 {
    public static void main(String [] args) throws Throwable {
        Interval<String> [] ipIntervals = DemoOwnershipOfLandForIPv4.readInvervals();

        //区间树算法
        DemoOwnershipOfLandForIPv4.testFullIntervalTreeSpeed( ipIntervals );

        System.gc();
        for (int i = 0; i < 5; ++i) {//为公平起见先把内存释放
            Thread.sleep( 1000 );
            System.gc();
        }
        System.gc();

        //二分折半算法
        DemoOwnershipOfLandForIPv4.testFullBinarySearchSpeed( ipIntervals );
    }

    private static void testFullIntervalTreeSpeed(Interval<String> [] aIPIntervals) {
        IntervalTree<String> intervalTre = IntervalTreeMaker.makeIntervalTree( aIPIntervals );
        //比较ipv4-global-utf8.txt看结果是否匹配
        System.out.println( "打印看下区间树算法结果是否匹配...." );
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0x0000_0000 );//0.0.0.0
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0xFFFF_FFFF );//255.255.255.255
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0x7FFF_FFFF );//127.255.255.255
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0x7F00_0000 );//127.0.0.0
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0x7F00_0001 );//127.0.0.1
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0x7F00_0002 );//127.0.0.2
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0x8000_0000 );//128.0.0.0
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0x0701_0101 );//7.1.1.1
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0x0155_9fa0 );//1.85.159.160
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0x0315_ff00 );//3.21.255.0
        DemoOwnershipOfLandForIPv4.printIntervalResult( intervalTre, 0x0808_0808 );//8.8.8.8 google

        System.out.println();
        System.out.println( "开始进行所有32位全IP数据的区间树速度测试..." );
        long t1 = System.currentTimeMillis();
        int index = 0;
        do {
            intervalTre.getInterval( index++ );
        } while (index != 0);
        long t2 = System.currentTimeMillis();
        System.out.println( "区间树所有32位地址查询花费:" + (t2 - t1) + " ms, 平均每秒:" + (0xFFFF_FFFFL + 1L) * 1000.0 / (t2 - t1) + " 条/秒" );
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    private static void testFullBinarySearchSpeed(Interval<String> [] aIPIntervals) {
        //实现一个二分查找的速度比较
        int [] ipSplits = new int [ aIPIntervals.length ];
        for (int i = 0; i < aIPIntervals.length; ++i) {
            ipSplits[ i ] = ( int )aIPIntervals[ i ].mEnd;
        }

        //比较ipv4-global-utf8.txt看结果是否匹配
        System.out.println( "打印看下二分折半查找算法结果是否匹配...." );
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0x0000_0000 );//0.0.0.0
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0xFFFF_FFFF );//255.255.255.255
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0x7FFF_FFFF );//127.255.255.255
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0x7F00_0000 );//127.0.0.0
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0x7F00_0001 );//127.0.0.1
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0x7F00_0002 );//127.0.0.2
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0x8000_0000 );//128.0.0.0
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0x0701_0101 );//7.1.1.1
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0x0155_9fa0 );//1.85.159.160
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0x0315_ff00 );//3.21.255.0
        DemoOwnershipOfLandForIPv4.printBinaryResult( aIPIntervals, ipSplits, 0x0808_0808 );//8.8.8.8 google

        System.out.println();
        System.out.println( "开始进行所有32位全IP数据二分折半查找速度测试..." );

        long t1 = System.currentTimeMillis();
        int index = 0;
        do {
            DemoOwnershipOfLandForIPv4.doBinarySearch( aIPIntervals, ipSplits, index++ );
        } while (index != 0);
        long t2 = System.currentTimeMillis();
        System.out.println( "二分折半查找所有32位地址查询花费:" + (t2 - t1) + " ms, 平均每秒:" + (0xFFFF_FFFFL + 1L) * 1000.0 / (t2 - t1) + " 条/秒" );
    }

    private static Interval<String> doBinarySearch(Interval<String> [] aIPIntervals, int [] aIPSplits, int aTestIP) {
        long thisIPLong = aTestIP & 0xFFFF_FFFFL;//IP地址的大小比较必须转换成32位无符号型，因此都必须用long,否则必须转成两次高地位int比较
        int low = 0;
        int high = aIPSplits.length - 1;//转成索引闭区间[low, high]
        int mid = (low + high) >>> 1;//避免除以2
        long midIPLong = aIPSplits[ mid ] & 0xFFFF_FFFFL;
        while (low < high) {
            if (midIPLong == thisIPLong) {
                break;
            }
            else {
                if (midIPLong < thisIPLong) {
                    low = mid == high ? mid : mid + 1;
                }
                else {
                    high = mid == low ? mid : mid - 1;
                }
                mid = (low + high) >>> 1;//避免除以2
                midIPLong = aIPSplits[ mid ] & 0xFFFF_FFFFL;
            }
        }
        Interval<String> interval = midIPLong < thisIPLong ? aIPIntervals[ mid + 1 ] : aIPIntervals[ mid ];
        //对于有间断间隔的区间,因此必须进行contains(aTestIP)检查是否在区间内才保证逻辑完整性
        return interval.contains( aTestIP ) ? interval : null;
    }

    private static void printIntervalResult(IntervalTree<String> aIntervalTree, int aTestIP) {
        Interval<String> ipInterval = aIntervalTree.getInterval( aTestIP );
        System.out.println( DemoOwnershipOfLandForIPv4.ipInt2String( aTestIP ) + "--->所在地址区间为[" + DemoOwnershipOfLandForIPv4.ipInt2String( ( int )ipInterval.mStart ) + ", " + DemoOwnershipOfLandForIPv4.ipInt2String( ( int )ipInterval.mEnd ) + "],归属地:" + ipInterval.mAttachment );
    }

    private static void printBinaryResult(Interval<String> [] aIPIntervals, int [] aIPSplits, int aTestIP) {
        Interval<String> ipInterval = DemoOwnershipOfLandForIPv4.doBinarySearch( aIPIntervals, aIPSplits, aTestIP );
        System.out.println( DemoOwnershipOfLandForIPv4.ipInt2String( aTestIP ) + "--->所在地址区间为[" + DemoOwnershipOfLandForIPv4.ipInt2String( ( int )ipInterval.mStart ) + ", " + DemoOwnershipOfLandForIPv4.ipInt2String( ( int )ipInterval.mEnd ) + "],归属地:" + ipInterval.mAttachment );
    }

    public static Interval<String> [] readInvervals() throws Throwable {
        ArrayList<Interval<String>> pairArray = new ArrayList<Interval<String>>( 122905 );
        try (BufferedReader br = new BufferedReader( new InputStreamReader( new BufferedInputStream( DemoOwnershipOfLandForIPv4.class.getResourceAsStream( "/ipv4-global-utf8.txt" ) ), "UTF-8" ) )) {
            HashMap<String, String> textMap = new HashMap<String, String>();
            for (String nextLine = br.readLine(); nextLine != null; nextLine = br.readLine()) {
                //这个文件的各行已经按照IP地址由小到大排序好了的
                if ((nextLine = nextLine.trim()).length() != 0 && !nextLine.startsWith( "##" )) {
                    int commaIndex = nextLine.indexOf( "," );
                    int rightIndex = nextLine.indexOf( ']', commaIndex + 1 );
                    int startIP = DemoOwnershipOfLandForIPv4.ipString2Int( nextLine.substring( 1, commaIndex ) );
                    int endIP = DemoOwnershipOfLandForIPv4.ipString2Int( nextLine.substring( commaIndex + 1, rightIndex ) );
                    String ownershipOfLand = nextLine.substring( rightIndex + 2 );//归属地
                    String existText = textMap.putIfAbsent( ownershipOfLand, ownershipOfLand );
                    ownershipOfLand = existText != null ? existText : ownershipOfLand;//节省字符串文本内存空间占用,因为有很多文本相同

                    pairArray.add( new Interval<String>( ownershipOfLand, startIP, endIP ) );
                }
            }
        }
        @SuppressWarnings("unchecked")
        Interval<String> [] pairs = pairArray.toArray( new Interval [ pairArray.size() ] );
        return pairs;
    }

    public static int ipString2Int(String aStringIP) {
        String [] parts = aStringIP.split( "\\." );
        int a, b, c, d;
        a = Integer.parseInt( parts[ 0 ] );
        b = Integer.parseInt( parts[ 1 ] );
        c = Integer.parseInt( parts[ 2 ] );
        d = Integer.parseInt( parts[ 3 ] );
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    public static String ipInt2String(int aIntIP) {
        int a = (aIntIP >> 24) & 0xFF;
        int b = (aIntIP >> 16) & 0xFF;
        int c = (aIntIP >> 8) & 0xFF;
        int d = (aIntIP >> 0) & 0xFF;
        return a + "." + b + "." + c + "." + d;
    }
}

package SimHash;

import java.math.BigInteger;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/1/25 21:41
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Test3 {
    public static void main(String[] args){
        BigInteger intSimhash1 = new BigInteger("15862889684289025515");
        BigInteger intSimhash2 = new BigInteger("17880502300172121571");
        System.out.println(SimHash.hammingDistance(intSimhash1, intSimhash2));
    }
}

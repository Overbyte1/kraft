package rpc;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * 管理入站连接
 */
public class InboundChannelGroup {
    public static void main(String[] args) {
        BiMap<Integer, String> biMap = HashBiMap.create();
        biMap.put(1, "string");
        System.out.println(biMap.get(1));
        BiMap<String, Integer> biMap1  = biMap.inverse();
        System.out.println(biMap1.get("string"));

        biMap.put(2, "char");
        System.out.println(biMap1.get("char"));


    }
}

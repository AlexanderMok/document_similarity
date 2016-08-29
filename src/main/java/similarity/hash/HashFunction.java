package similarity.hash;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;


/**
 * @package com.kass.app.app_user.models.similarity.simhash
 * @project app-kasscloud
 * @description Murmurhash function Murmurhash哈希函数
 * @author 莫庆来
 * @date 2016年8月2日
 */
public class HashFunction {
	
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	
	/**
	 * 返回无符号murmur hash值  
	 * @param feature
	 * @return 无符号 hash 值,BigDecimal类型
	 * 
	 */
    public static BigDecimal hashUnsigned64(String feature) {  
        return readUnsignedLong(hash(feature));  
    }  
	
    
    /**
     * Long转换成无符号长整型（C中数据类型）
     * @param value
     * @return
     */
    public static BigDecimal readUnsignedLong(long value) {  
        if (value >= 0)  
            return new BigDecimal(value);  
        long lowValue = value & 0x7fffffffffffffffL;  
        return BigDecimal.valueOf(lowValue).add(BigDecimal.valueOf(Long.MAX_VALUE)).add(BigDecimal.ONE);  
    }  
    
    /**
     * convert String into utf-8 and then execute hash
     * @param feature
     * @return
     */
    private static Long hash(String feature) {  
        return hash(feature.getBytes(UTF_8));  
    }
	
	/**
	 * murmur hash algorithm
	 * @param key
	 * @return
	 */
	private static Long hash(byte[] key) {  
  
        ByteBuffer buf = ByteBuffer.wrap(key);  
        int seed = 0x1234ABCD;  
  
        ByteOrder byteOrder = buf.order();  
        buf.order(ByteOrder.LITTLE_ENDIAN);  
  
        long m = 0xc6a4a7935bd1e995L;  
        int r = 47;  
  
        long h = seed ^ (buf.remaining() * m);  
  
        long k;  
        while (buf.remaining() >= 8) {  
            k = buf.getLong();  
  
            k *= m;  
            k ^= k >>> r;  
            k *= m;  
  
            h ^= k;  
            h *= m;  
        }  
  
        if (buf.remaining() > 0) {  
            ByteBuffer finish = ByteBuffer.allocate(8).order(  
                    ByteOrder.LITTLE_ENDIAN);  
            // for big-endian version, do this first: 
            finish.position(8-buf.remaining());  
            finish.put(buf).rewind();  
            h ^= finish.getLong();  
            h *= m;  
        }  
  
        h ^= h >>> r;  
        h *= m;  
        h ^= h >>> r;  
  
        buf.order(byteOrder);  
        return h;  
    }  
}

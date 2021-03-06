package com.feeyo.redis.nio.buffer.bucket;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import sun.nio.ch.DirectBuffer;

public class TestBucket2 {
	
	public static void main(String[] args) {
		
		long minBufferSize = 1024 * 1024 * 30;
		long maxBufferSize = 1024 * 1024 * 100;
		int minChunkSize = 0;
		int increment = 16;
		int maxChunkSize = 128;
		
		final ConcurrentHashMap<Long, Integer> used = new ConcurrentHashMap<>(); 
		
		final ByteBufferBucketPool bufferPool = new ByteBufferBucketPool(
				minBufferSize, maxBufferSize, minChunkSize, increment, maxChunkSize);
		
		
		long t1 = System.currentTimeMillis();
		
		
		int size = 50;
		final CountDownLatch c = new CountDownLatch( size );
		
		for(int i= 0; i < size; i++) {
			Thread t = new Thread() {
				Random r = new Random();
				public void run() {
					for (int j = 0; j <= 300000; j++) {
						int chunkSize = r.nextInt( 128 );
						if ( chunkSize == 0 ) chunkSize = 1;
						ByteBuffer b = bufferPool.allocate( chunkSize );
						if ( b != null) {
							
							if ( b.position() > 0 ) {
								System.out.println("err.....");
							}
							
							byte[] src = new byte[]{'1', '2'};
							b.put( src );
							
							if ( b instanceof DirectBuffer ) {
								
								long startAddress = ((sun.nio.ch.DirectBuffer) b).address();
								Integer f = used.get( startAddress );
								if (f != null && f == 1) {
									System.out.println( "startAddress: " + startAddress + ", flag:" + f + ", is err" );
								} else {
									used.put( startAddress, 1);
								}
								
								
								
							} else {
								System.out.println("no direct.");
							}
							
						}
						
						if ( b instanceof DirectBuffer ) {
							long startAddress = ((sun.nio.ch.DirectBuffer) b).address();
							used.put(startAddress, 0);
						}
						bufferPool.recycle(b);
					}
					c.countDown();
				}
			};
			t.start();
		}

		try {
			c.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		long t2 = System.currentTimeMillis();
		System.out.println("t2-t1:" + (t2-t1));


		int count = 0;
		ByteBufferBucket[] buckets = bufferPool.buckets();
		for (ByteBufferBucket bucket : buckets) {
			count += bucket.getCount();
		}
		System.out.println( count );
		
	}

}

package jp.cssj.driver.ctip.v1;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: RequestConsumerOutputStream.java,v 1.1 2005/03/26 10:22:30
 *          harumanx Exp $
 */
public class V1RequestConsumerOutputStream extends OutputStream {
	private final V1RequestConsumer consumer;

	private final byte[] buff = new byte[1];

	public V1RequestConsumerOutputStream(V1RequestConsumer consumer) {
		this.consumer = consumer;
	}

	public void write(int b) throws IOException {
		this.buff[0] = (byte) b;
		this.consumer.write(this.buff, 0, 1);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		this.consumer.write(b, off, len);
	}

	public void write(byte[] b) throws IOException {
		this.consumer.write(b, 0, b.length);
	}

	public void close() throws IOException {
		// ignore
	}
}
package jp.cssj.driver.ctip.v2;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: RequestConsumerOutputStream.java,v 1.1 2005/03/26 10:22:30
 *          harumanx Exp $
 */
public class V2RequestConsumerOutputStream extends OutputStream {
	private final V2RequestConsumer consumer;

	private final byte[] buff = new byte[1];

	public V2RequestConsumerOutputStream(V2RequestConsumer consumer) {
		this.consumer = consumer;
	}

	public void write(int b) throws IOException {
		this.buff[0] = (byte) b;
		this.consumer.data(this.buff, 0, 1);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		this.consumer.data(b, off, len);
	}

	public void write(byte[] b) throws IOException {
		this.consumer.data(b, 0, b.length);
	}

	public void close() throws IOException {
		this.consumer.eof();
	}
}
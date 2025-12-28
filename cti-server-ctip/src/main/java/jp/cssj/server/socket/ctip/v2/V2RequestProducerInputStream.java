package jp.cssj.server.socket.ctip.v2;

import java.io.IOException;
import java.io.InputStream;

import jp.cssj.cti2.progress.Progressive;
import jp.cssj.driver.ctip.v2.V2ClientPackets;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: RequestProducerInputStream.java,v 1.1 2005/03/26 10:22:30
 *          harumanx Exp $
 */
public class V2RequestProducerInputStream extends InputStream implements Progressive {
	private final V2RequestProducer request;

	private final byte[] buff = new byte[1];

	private int progress = 0;

	public V2RequestProducerInputStream(V2RequestProducer producer) {
		this.request = producer;
	}

	public long getProgress() {
		return this.progress;
	}

	private boolean checkRequest() throws IOException {
		switch (this.request.getType()) {
		case V2ClientPackets.EOF:
			return false;

		case V2ClientPackets.DATA:
			return true;
		default:
			throw new IllegalStateException("不正なリクエストです: " + Integer.toHexString(this.request.getType()));
		}
	}

	public int read() throws IOException {
		if (!this.checkRequest()) {
			return -1;
		}
		int read = this.request.read(this.buff, 0, 1);
		if (read == 1) {
			++this.progress;
			return this.buff[0];
		}
		this.request.next();
		if (!this.checkRequest()) {
			return -1;
		}
		return this.read();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		if (!this.checkRequest()) {
			return -1;
		}
		int read = this.request.read(b, off, len);
		if (read != -1) {
			this.progress += read;
			return read;
		}
		this.request.next();
		if (!this.checkRequest()) {
			return -1;
		}
		return this.read(b, off, len);
	}

	public int read(byte[] b) throws IOException {
		if (!this.checkRequest()) {
			return -1;
		}
		int read = this.request.read(b, 0, b.length);
		if (read != -1) {
			this.progress += read;
			return read;
		}
		this.request.next();
		if (!this.checkRequest()) {
			return -1;
		}
		return this.read(b, 0, b.length);
	}
}
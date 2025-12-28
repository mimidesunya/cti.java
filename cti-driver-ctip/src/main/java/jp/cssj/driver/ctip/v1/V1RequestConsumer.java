package jp.cssj.driver.ctip.v1;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import jp.cssj.driver.ctip.common.ChannelIO;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: V1RequestConsumer.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class V1RequestConsumer {
	/**
	 * プロパティパケットです。 getName,getValueで名前と値を取得できます。
	 */
	public static final byte PROPERTY = 1;

	/**
	 * リソース開始パケットです。 getURI,getMimeType,getEncodingでURIとMIMEタイプとエンコーディングを取得できます。
	 */
	public static final byte RESOURCE = 2;

	/**
	 * 内容開始パケットです。 getURI,getMimeType,getEncodingでURIとMIMEタイプとエンコーディングを取得できます。
	 */
	public static final byte MAIN = 3;

	/**
	 * データパケットです。 readでデータを取得できます。
	 */
	public static final byte DATA = 4;

	/**
	 * 終了パケットです。
	 */
	public static final byte END = 5;

	private final String encoding;

	private final byte[] buff = new byte[V1Session.BUFFER_SIZE + 5];

	private final ChannelIO io;

	private int pos = 0;

	private V1Session session;

	V1RequestConsumer(ChannelIO io, String encoding) throws IOException {
		this.io = io;
		this.encoding = encoding;
	}

	/**
	 * プロパティを送ります。
	 * 
	 * @param name
	 *            プロパティ名。
	 * @param value
	 *            値。
	 * @throws IOException
	 */
	public void property(String name, String value) throws IOException {
		this.flush();
		if (name.equals("input.exclude")) {
			name = "ctip.exclude";
		} else if (name.equals("input.include")) {
			name = "ctip.include";
		}
		byte[] nameBytes = ChannelIO.toBytes(name, this.encoding);
		byte[] valueBytes = ChannelIO.toBytes(value, this.encoding);

		ByteBuffer src = ByteBuffer.allocate(nameBytes.length + valueBytes.length + 9);
		src.putInt(nameBytes.length + valueBytes.length + 5);
		src.put(PROPERTY);
		src.putShort((short) nameBytes.length);
		src.put(nameBytes);
		src.putShort((short) valueBytes.length);
		src.put(valueBytes);
		this.io.writeAll(src);
	}

	/**
	 * リソースの開始を通知します。
	 * 
	 * @param uri
	 *            仮想URI。
	 * @param mimeType
	 *            MIME型。
	 * @param encoding
	 *            キャラクタ・エンコーディング。
	 * @throws IOException
	 */
	public void resource(URI uri, String mimeType, String encoding) throws IOException {
		this.flush();
		byte[] uriBytes = ChannelIO.toBytes(uri.toString(), this.encoding);
		byte[] mimeTypeBytes = ChannelIO.toBytes(mimeType, this.encoding);
		byte[] encodingBytes = ChannelIO.toBytes(encoding, this.encoding);

		ByteBuffer src = ByteBuffer.allocate(uriBytes.length + mimeTypeBytes.length + encodingBytes.length + 11);
		src.putInt(uriBytes.length + mimeTypeBytes.length + encodingBytes.length + 7);
		src.put(RESOURCE);
		src.putShort((short) uriBytes.length);
		src.put(uriBytes);
		src.putShort((short) mimeTypeBytes.length);
		src.put(mimeTypeBytes);
		src.putShort((short) encodingBytes.length);
		src.put(encodingBytes);
		this.io.writeAll(src);
	}

	/**
	 * 本体の開始を通知します。
	 * 
	 * @param uri
	 *            仮想URI。
	 * @param mimeType
	 *            MIME型。
	 * @param encoding
	 *            キャラクタ・エンコーディング。
	 * @throws IOException
	 */
	public void main(URI uri, String mimeType, String encoding) throws IOException {
		this.flush();
		byte[] uriBytes = ChannelIO.toBytes(uri.toString(), this.encoding);
		byte[] mimeTypeBytes = ChannelIO.toBytes(mimeType, this.encoding);
		byte[] encodingBytes = ChannelIO.toBytes(encoding, this.encoding);

		ByteBuffer src = ByteBuffer.allocate(uriBytes.length + mimeTypeBytes.length + encodingBytes.length + 11);
		src.putInt(uriBytes.length + mimeTypeBytes.length + encodingBytes.length + 7);
		src.put(MAIN);
		src.putShort((short) uriBytes.length);
		src.put(uriBytes);
		src.putShort((short) mimeTypeBytes.length);
		src.put(mimeTypeBytes);
		src.putShort((short) encodingBytes.length);
		src.put(encodingBytes);
		this.io.writeAll(src);

	}

	protected void setCTIPSession(V1Session session) {
		this.session = session;
	}

	/**
	 * データパケットを送ります。
	 * 
	 * @param b
	 *            バイト列バッファ。
	 * @param off
	 *            データの開始位置。
	 * @param len
	 *            データの長さ。
	 * @throws IOException
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		for (int i = 0; i < len; ++i) {
			this.write(b[i + off]);
		}
	}

	private void write(int b) throws IOException {
		if (this.pos >= V1Session.BUFFER_SIZE) {
			this.flush();
		}
		this.buff[(this.pos++) + 5] = (byte) b;
	}

	private void flush() throws IOException {
		if (this.pos > 0) {
			int payload = this.pos + 1;
			ByteBuffer src = ByteBuffer.wrap(this.buff, 0, this.pos + 5);
			src.putInt(payload);
			src.put(DATA);

			src.position(0);

			for (;;) {
				SelectionKey key = this.io.rwselect();
				if (src.remaining() > 0 && key.isWritable()) {
					this.io.getChannel().write(src);
				}
				if (key.isReadable()) {
					this.session.buildNext();
				}
				if (src.remaining() <= 0) {
					break;
				}
			}

			this.pos = 0;
		}
	}

	/**
	 * 終了パケットを送ります。
	 * 
	 * @throws IOException
	 */
	public void end() throws IOException {
		this.flush();
		ByteBuffer src = ByteBuffer.allocate(4);
		src.putInt(0);
		this.io.writeAll(src);
	}
}
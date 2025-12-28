package jp.cssj.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 接続後、クライアントとのやり取りを行います 。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: ProtocolProcessor.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public interface ProtocolProcessor {
	/**
	 * クライアントとのやり取りを行います。
	 * 
	 * @param in
	 * @param out
	 * @param firstLine
	 *            クライアントから送られた最初の行。
	 * @throws IOException
	 */
	public void process(Socket socket, InputStream in, OutputStream out, String firstLine) throws IOException;

	/**
	 * クライアントにメッセージを送ります。
	 * 
	 * @param code
	 * @param args
	 * @param message
	 * @throws IOException
	 */
	public void message(short code, String[] args, String message) throws IOException;

	/**
	 * 通信を終了します。
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
}

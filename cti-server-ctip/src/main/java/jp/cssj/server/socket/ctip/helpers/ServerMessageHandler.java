package jp.cssj.server.socket.ctip.helpers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.cssj.cti2.message.MessageHandler;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: ServerMessageHandler.java,v 1.1 2007/02/11 13:23:25 harumanx
 *          Exp $
 */
public class ServerMessageHandler implements MessageHandler {
	private static final Logger LOG = Logger.getLogger(ServerMessageHandler.class.getName());

	private final ResponseConsumer consumer;

	public ServerMessageHandler(ResponseConsumer consumer) {
		this.consumer = consumer;
	}

	public void message(short code, String[] args, String mes) {
		try {
			this.consumer.message(code, args, mes);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "入出力エラー" + args, e);
		}
	}
}

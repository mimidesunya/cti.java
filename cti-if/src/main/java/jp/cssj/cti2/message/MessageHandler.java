package jp.cssj.cti2.message;

/**
 * メッセージを受け取るインターフェースです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: MessageHandler.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public interface MessageHandler {
	/**
	 * メッセージ受け取ります。
	 * 
	 * @param code
	 *            メッセージコード。
	 * @param args
	 *            メッセージに付随する値。
	 * @param mes
	 *            人間が読める形式のメッセージ。
	 */
	public void message(short code, String[] args, String mes);
}
package jp.cssj.driver.ctip.v2;

/**
 * クライアントからサーバーに送られるパケットの種類です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: V2ClientPackets.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public interface V2ClientPackets {
	/**
	 * プロパティパケットです。
	 */
	public static final byte PROPERTY = 0x01;

	/**
	 * 内容開始パケットです。
	 */
	public static final byte START_MAIN = 0x02;

	/**
	 * サーバー側でメインドキュメントの取得を要求するパケットです。
	 */
	public static final byte SERVER_MAIN = 0x03;

	/**
	 * クライアント側でリソースを解決するモードに切り替えるパケットです。
	 */
	public static final byte CLIENT_RESOURCE = 0x04;

	/**
	 * 複数の結果を結合するモードに切り替えるパケットです。
	 */
	public static final byte CONTINUOUS = 0x05;

	/**
	 * データパケットです。
	 */
	public static final byte DATA = 0x11;

	/**
	 * リソース開始パケットです。
	 */
	public static final byte START_RESOURCE = 0x21;

	/**
	 * 存在しないリソースを示すパケットです。
	 */
	public static final byte MISSING_RESOURCE = 0x22;

	/**
	 * データの終了を示すパケットです。
	 */
	public static final byte EOF = 0x31;

	/**
	 * 処理中断パケットです。
	 */
	public static final byte ABORT = 0x32;

	/**
	 * 結果の結合パケットです。
	 */
	public static final byte JOIN = 0x33;

	/**
	 * 状態リセットパケットです。
	 */
	public static final byte RESET = 0x41;

	/**
	 * 通信終了パケットです。
	 */
	public static final byte CLOSE = 0x42;

	/**
	 * サーバー情報パケットです。
	 */
	public static final byte SERVER_INFO = 0x51;
}

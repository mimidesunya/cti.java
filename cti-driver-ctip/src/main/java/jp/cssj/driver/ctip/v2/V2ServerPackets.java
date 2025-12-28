package jp.cssj.driver.ctip.v2;

/**
 * サーバーからクライアントに送られるパケットの種類です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: V2ServerPackets.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public interface V2ServerPackets {
	/**
	 * データを開始します。
	 */
	public static final byte START_DATA = 0x01;

	/**
	 * データパケットです。
	 */
	public static final byte BLOCK_DATA = 0x11;

	/**
	 * 断片追加パケットです。
	 */
	public static final byte ADD_BLOCK = 0x12;

	/**
	 * 断片挿入パケットです。
	 */
	public static final byte INSERT_BLOCK = 0x13;

	/**
	 * エラーメッセージパケットです。
	 */
	public static final byte MESSAGE = 0x14;

	/**
	 * メインドキュメントの長さを通知するパケットです。
	 */
	public static final byte MAIN_LENGTH = 0x15;

	/**
	 * メインドキュメントの読み込みバイト数を通知するパケットです。
	 */
	public static final byte MAIN_READ = 0x16;

	/**
	 * 断片化とは無関係なデータパケットです。
	 */
	public static final byte DATA = 0x17;

	/**
	 * 断片のクローズを通知するパケットです。
	 */
	public static final byte CLOSE_BLOCK = 0x18;

	/**
	 * リソース要求パケットです。
	 */
	public static final byte RESOURCE_REQUEST = 0x21;

	/**
	 * データ終了パケットです。
	 */
	public static final byte EOF = 0x31;

	/**
	 * データ中断パケットです。
	 */
	public static final byte ABORT = 0x32;

	/**
	 * データ継続パケットです。
	 */
	public static final byte NEXT = 0x33;
}

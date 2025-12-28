package jp.cssj.cti2;

import java.io.IOException;

/**
 * ドキュメントの変換を中断したことを示す例外です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: TranscoderException.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class TranscoderException extends IOException {
	private static final long serialVersionUID = 0L;

	private final short code;

	private final String[] args;

	private final byte state;

	/**
	 * 変換結果は不完全ですが、利用可能なデータです。
	 */
	public static final byte STATE_READABLE = 1;

	/**
	 * 変換結果のデータは破壊されています。
	 */
	public static final byte STATE_BROKEN = 2;

	public TranscoderException(byte state, short code, String[] args, String message) {
		super(message);
		this.code = code;
		this.args = args;
		this.state = state;
	}

	public TranscoderException(short code, String[] args, String message) {
		this(STATE_BROKEN, code, args, message);
	}

	/**
	 * 中断の原因となったメッセージコードです。
	 * 
	 * @return メッセージコード。
	 */
	public short getCode() {
		return this.code;
	}

	/**
	 * メッセージに付随する値です。
	 * 
	 * @return メッセージの引数。
	 */
	public String[] getArgs() {
		return this.args;
	}

	/**
	 * 変換後の状態(STATE_XXX定数)を返します。
	 * 
	 * @return 変換後の状態定数。
	 */
	public byte getState() {
		return this.state;
	}
}

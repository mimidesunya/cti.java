package jp.cssj.cti2.helpers;

/**
 * MIME方を解析する補助クラスです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: MimeTypeHelper.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public final class MimeTypeHelper {
	private MimeTypeHelper() {
		// unused
	}

	/**
	 * 2つのMIMEタイプがパラメータを除いて同じがどうかを判別します。
	 * 
	 * @param type1
	 *            MIME型1。
	 * @param type2
	 *            MIME型2。
	 * @return 2つのタイプが一致していればtrue、そうでなければfalse。
	 */
	public static boolean equals(String type1, String type2) {
		if (type2 == null || type1 == null) {
			return false;
		}
		type1 = getTypePart(type1);
		type2 = getTypePart(type2);
		return type1.equals(type2);
	}

	/**
	 * パラメータを除いた部分を返します。
	 * 
	 * @param type
	 *            MIME型。
	 * @return MIME型のパラメータを除いた部分。
	 */
	public static String getTypePart(String type) {
		if (type == null) {
			return null;
		}
		int semi = type.indexOf(';');
		if (semi != -1) {
			return type.substring(0, semi).trim();
		}
		return type.trim();

	}

	/**
	 * Content-Typeヘッダパラメータの値を返します。
	 * 
	 * @param type
	 *            Content-Typeヘッダ値。
	 * @param name
	 *            パラメータ名。
	 * @return パラメータの値。
	 */
	public static String getParameter(String type, String name) {
		int state = 0;
		StringBuffer buff = new StringBuffer();
		String pname = "", value = "";
		;
		for (int i = 0; i < type.length(); ++i) {
			char c = type.charAt(i);
			switch (state) {
			case 0:
				if (c == '=') {
					pname = buff.toString().trim();
					buff = new StringBuffer();
				} else if (c == ';') {
					if (name.equalsIgnoreCase(pname)) {
						return value + buff.toString().trim();
					}
					pname = "";
					value = "";
					buff = new StringBuffer();
				} else if (c == '"') {
					state = 1;
					buff = new StringBuffer(buff.toString().trim());
				} else if (c == '\'') {
					state = 2;
					buff = new StringBuffer(buff.toString().trim());
				} else {
					buff.append(c);
				}
				break;
			case 1:
				if (c == '"') {
					value = buff.toString();
					buff = new StringBuffer();
					state = 0;
				} else {
					buff.append(c);
				}
				break;
			case 2:
				if (c == '\'') {
					value = buff.toString();
					buff = new StringBuffer();
					state = 0;
				} else {
					buff.append(c);
				}
				break;
			}
		}
		if (name.equalsIgnoreCase(pname)) {
			return value + buff.toString().trim();
		}
		return null;
	}
}
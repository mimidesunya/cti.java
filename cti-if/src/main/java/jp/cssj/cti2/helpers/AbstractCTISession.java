package jp.cssj.cti2.helpers;

import jp.cssj.cti2.CTISession;

public abstract class AbstractCTISession implements CTISession {
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}
}

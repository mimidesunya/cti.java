package jp.cssj.cti2.results;

import jp.cssj.resolver.MetaSource;
import jp.cssj.rsr.RandomBuilder;
import jp.cssj.rsr.impl.NopRandomBuilder;

/**
 * 何も出力しないResultsです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: NopResults.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class NopResults implements Results {
	public static final NopResults SHARED_INSTANCE = new NopResults();

	private NopResults() {
		// private
	}

	public boolean hasNext() {
		return true;
	}

	public RandomBuilder nextBuilder(MetaSource metaSource) {
		return NopRandomBuilder.SHARED_INSTANCE;
	}

	public void end() {
		// NOP
	}
}

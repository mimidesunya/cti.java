package jp.cssj.server.acl;

import java.net.InetAddress;

import jp.cssj.plugin.Plugin;

/**
 * アクセス制御リストです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Acl.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public interface Acl extends Plugin<Object> {
	public boolean checkAccess(InetAddress remoteAddress);
}

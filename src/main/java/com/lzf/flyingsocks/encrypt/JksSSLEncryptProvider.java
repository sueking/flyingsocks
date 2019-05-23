package com.lzf.flyingsocks.encrypt;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.Map;

/**
 * 提供基于JKS的SSL加密连接
 */
@SuppressWarnings("unused")
public final class JksSSLEncryptProvider implements EncryptProvider {

    static final String NAME = "JKS";

    JksSSLEncryptProvider() { }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isInboundHandlerSameAsOutboundHandler() {
        return true;
    }

    @Override
    public ChannelInboundHandler decodeHandler(Map<String, Object> params) throws Exception {
        return createSSLHandler(params);
    }

    @Override
    public ChannelOutboundHandler encodeHandler(Map<String, Object> params) throws Exception {
        return createSSLHandler(params);
    }

    private SslHandler createSSLHandler(Map<String, Object> params) throws Exception {
        if(params == null)
            throw new NullPointerException("params should not be null.");

        if(!params.containsKey("password") || !params.containsKey("url") || !params.containsKey("client"))
            throw new IllegalArgumentException("Parameter key jksPass/jksUrl/isClient should not be null");

        boolean cli = (boolean) params.get("client");

        char[] pass = ((String)params.get("password")).toCharArray();

        URL url = new URL((String) params.get("url"));
        try(InputStream is = url.openStream()) {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(is, pass);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, pass);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), null, null);
            SSLEngine sslEngine = context.createSSLEngine();
            sslEngine.setUseClientMode(cli);
            sslEngine.setNeedClientAuth(false);

            return new SslHandler(sslEngine);
        }
    }
}

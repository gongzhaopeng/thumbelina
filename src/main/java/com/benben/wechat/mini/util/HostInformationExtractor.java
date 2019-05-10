package com.benben.wechat.mini.util;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class HostInformationExtractor {

    static public String getHostIp() {

        try (final var socket = new DatagramSocket()) {

            final var FAKED_REMOTE_HOST = "8.8.8.8";
            final var FAKED_REMOTE_PORT = 10002;

            socket.connect(
                    InetAddress.getByName(FAKED_REMOTE_HOST),
                    FAKED_REMOTE_PORT);

            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}

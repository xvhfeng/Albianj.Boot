package org.albianj.framework.boot.servants;

import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public class ConvertServant {
    public static ConvertServant Instance = null;

    static {
        if(null == Instance) {
            Instance = new ConvertServant();
        }
    }

    protected ConvertServant() {

    }

    public boolean toBoolean(String value, boolean def) {
        if (value == null)
            return def;
        String trimmedVal = value.trim();
        if ("true".equalsIgnoreCase(trimmedVal))
            return true;
        if ("false".equalsIgnoreCase(trimmedVal))
            return false;
        return def;
    }

    public int toInt(String value, int dEfault) {
        if (value != null) {
            String s = value.trim();
            try {
                return Integer.valueOf(s).intValue();
            } catch (NumberFormatException e) {
//                LogLog.error("[" + s + "] is not in proper int form.");
                e.printStackTrace();
            }
        }
        return dEfault;
    }

    public long toFileSize(String value, long dEfault) {
        if (value == null)
            return dEfault;

        String s = value.trim().toUpperCase();
        long multiplier = 1;
        int index;

        if ((index = s.indexOf("KB")) != -1) {
            multiplier = 1024;
            s = s.substring(0, index);
        } else if ((index = s.indexOf("MB")) != -1) {
            multiplier = 1024 * 1024;
            s = s.substring(0, index);
        } else if ((index = s.indexOf("GB")) != -1) {
            multiplier = 1024 * 1024 * 1024;
            s = s.substring(0, index);
        }
        if (s != null) {
            try {
                return Long.valueOf(s).longValue() * multiplier;
            } catch (NumberFormatException e) {
            }
        }
        return dEfault;
    }

    public byte[] intToBytes(int n) {
        byte[] buff = new byte[4];
        buff[0] = (byte) ((n >> 24) & 0xFF);
        buff[1] = (byte) ((n >> 16) & 0xFF);
        buff[2] = (byte) ((n >> 8) & 0xFF);
        buff[3] = (byte) (n & 0xFF);
        return buff;
    }

    public byte[] intToBytes(int n, byte[] buff, int offset) {
        buff[offset] = (byte) ((n >> 24) & 0xFF);
        buff[offset + 1] = (byte) ((n >> 16) & 0xFF);
        buff[offset + 2] = (byte) ((n >> 8) & 0xFF);
        buff[offset + 3] = (byte) (n & 0xFF);
        return buff;
    }

    public int bytesToInt(byte[] buff, int offset) {
        int n = (int) (
                ((((int) buff[offset]) & 0xFF) << 24)
                        | ((((int) buff[offset + 1]) & 0xFF) << 16)
                        | ((((int) buff[offset + 2]) & 0xFF) << 8)
                        | ((((int) buff[offset + 3]) & 0xFF)));
        return n;
    }

    public byte[] longToBytes(long n) {
        byte[] buff = new byte[8];
        buff[0] = (byte) ((n >> 56) & 0xFF);
        buff[1] = (byte) ((n >> 48) & 0xFF);
        buff[2] = (byte) ((n >> 40) & 0xFF);
        buff[3] = (byte) ((n >> 32) & 0xFF);
        buff[4] = (byte) ((n >> 24) & 0xFF);
        buff[5] = (byte) ((n >> 16) & 0xFF);
        buff[6] = (byte) ((n >> 8) & 0xFF);
        buff[7] = (byte) (n & 0xFF);
        return buff;
    }

    public byte[] longToBytes(long n, byte[] buff, int offset) {
        buff[offset] = (byte) ((n >> 56) & 0xFF);
        buff[offset + 1] = (byte) ((n >> 48) & 0xFF);
        buff[offset + 2] = (byte) ((n >> 40) & 0xFF);
        buff[offset + 3] = (byte) ((n >> 32) & 0xFF);
        buff[offset + 4] = (byte) ((n >> 24) & 0xFF);
        buff[offset + 5] = (byte) ((n >> 16) & 0xFF);
        buff[offset + 6] = (byte) ((n >> 8) & 0xFF);
        buff[offset + 7] = (byte) (n & 0xFF);
        return buff;
    }

    public long bytesToLong(byte[] buff, int offset) {
        long n = (long) (
                ((((long) buff[offset]) & 0xFF) << 56)
                        | ((((long) buff[offset + 1]) & 0xFF) << 48)
                        | ((((long) buff[offset + 2]) & 0xFF) << 40)
                        | ((((long) buff[offset + 3]) & 0xFF) << 32)
                        | ((((long) buff[offset + 4]) & 0xFF) << 24)
                        | ((((long) buff[offset + 5]) & 0xFF) << 16)
                        | ((((long) buff[offset + 6]) & 0xFF) << 8)
                        | ((((long) buff[offset + 7]) & 0xFF)));
        return n;
    }

    public byte[] intToBytesLE(int n) {
        byte[] buff = new byte[4];
        buff[3] = (byte) ((n >> 24) & 0xFF);
        buff[2] = (byte) ((n >> 16) & 0xFF);
        buff[1] = (byte) ((n >> 8) & 0xFF);
        buff[0] = (byte) (n & 0xFF);
        return buff;
    }

    public byte[] intToBytesLE(int n, byte[] buff, int offset) {
        buff[offset + 3] = (byte) ((n >> 24) & 0xFF);
        buff[offset + 2] = (byte) ((n >> 16) & 0xFF);
        buff[offset + 1] = (byte) ((n >> 8) & 0xFF);
        buff[offset] = (byte) (n & 0xFF);
        return buff;
    }

    public int bytesToIntLE(byte[] buff, int offset) {
        int n = (int) (
                ((((int) buff[offset + 3]) & 0xFF) << 24)
                        | ((((int) buff[offset + 2]) & 0xFF) << 16)
                        | ((((int) buff[offset + 1]) & 0xFF) << 8)
                        | ((((int) buff[offset]) & 0xFF)));
        return n;
    }

    public byte[] longToBytesLE(long n) {
        byte[] buff = new byte[8];
        buff[7] = (byte) ((n >> 56) & 0xFF);
        buff[6] = (byte) ((n >> 48) & 0xFF);
        buff[5] = (byte) ((n >> 40) & 0xFF);
        buff[4] = (byte) ((n >> 32) & 0xFF);
        buff[3] = (byte) ((n >> 24) & 0xFF);
        buff[2] = (byte) ((n >> 16) & 0xFF);
        buff[1] = (byte) ((n >> 8) & 0xFF);
        buff[0] = (byte) (n & 0xFF);
        return buff;
    }

    public byte[] longToBytesLE(long n, byte[] buff, int offset) {
        buff[offset + 7] = (byte) ((n >> 56) & 0xFF);
        buff[offset + 6] = (byte) ((n >> 48) & 0xFF);
        buff[offset + 5] = (byte) ((n >> 40) & 0xFF);
        buff[offset + 4] = (byte) ((n >> 32) & 0xFF);
        buff[offset + 3] = (byte) ((n >> 24) & 0xFF);
        buff[offset + 2] = (byte) ((n >> 16) & 0xFF);
        buff[offset + 1] = (byte) ((n >> 8) & 0xFF);
        buff[offset] = (byte) (n & 0xFF);
        return buff;
    }

    public long bytesToLongLE(byte[] buff, int offset) {
        long n = (long) (
                ((((long) buff[offset + 7]) & 0xFF) << 56)
                        | ((((long) buff[offset + 6]) & 0xFF) << 48)
                        | ((((long) buff[offset + 5]) & 0xFF) << 40)
                        | ((((long) buff[offset + 4]) & 0xFF) << 32)
                        | ((((long) buff[offset + 3]) & 0xFF) << 24)
                        | ((((long) buff[offset + 2]) & 0xFF) << 16)
                        | ((((long) buff[offset + 1]) & 0xFF) << 8)
                        | ((((long) buff[offset]) & 0xFF)));
        return n;
    }

}

package xyz.rodit.snapmod.util;

import java.nio.ByteBuffer;

import xyz.rodit.snapmod.mappings.UUID;

public class UUIDUtil {

    public static String fromSnap(Object snapUUID) {
        return fromByteArray((byte[]) UUID.wrap(snapUUID).getId());
    }

    public static String fromByteArray(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        return new java.util.UUID(high, low).toString();
    }
}

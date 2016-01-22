package com.github.pimsturm.commandmessenger;

// http://www.se7ensins.com/forums/threads/java-c-s-bitconverter-in-java.486271/
// http://www.dotnetframework.org/default.aspx/DotNET/DotNET/8@0/untmp/whidbey/REDBITS/ndp/clr/src/BCL/System/BitConverter@cs/1/BitConverter@cs

import java.lang.Exception;

public class BitConverter
{
    public static byte[] getBytes(boolean x)
    {
        return new byte[]{
                (byte) (x ? 1:0)
        };
    }

    public static byte[] getBytes(char c)
    {
        return new byte[]  {
                (byte)(c & 0xff),
                (byte)(c >> 8 & 0xff) };
    }

    public static byte[] getBytes(double x)
    {
        return getBytes(
                Double.doubleToRawLongBits(x));
    }

    public static byte[] getBytes(short x)
    {
        return new byte[] {
                (byte)(x >>> 8),
                (byte)x
        };
    }

    public static byte[] getBytes(int x)
    {
        return new byte[] {
                (byte)(x >>> 24),
                (byte)(x >>> 16),
                (byte)(x >>> 8),
                (byte)x
        };
    }

    public static byte[] getBytes(long x)
    {
        return new byte[] {
                (byte)(x >>> 56),
                (byte)(x >>> 48),
                (byte)(x >>> 40),
                (byte)(x >>> 32),
                (byte)(x >>> 24),
                (byte)(x >>> 16),
                (byte)(x >>> 8),
                (byte)x
        };
    }

    public static byte[] getBytes(float x)
    {
        return getBytes(
                Float.floatToRawIntBits(x));
    }

    public static byte[] getBytes(String x)
    {
        return x.getBytes();
    }

    public static byte[] getBytes(UInt32 x) {
        return getBytes(x.intValue());
    }

    public static byte[] getBytes(UInt16 x) {
        return getBytes(x.shortValue());
    }

    public static long doubleToInt64Bits(double x)
    {
        return Double.doubleToRawLongBits(x);
    }

    public static double int64BitsToDouble(long x)
    {
        return (double)x;
    }

    public static boolean toBoolean(byte[] bytes, int index) throws Exception
    {
        if(bytes.length != 1)
            throw new Exception("The length of the byte array must be at least 1 byte long.");
        return bytes[index] != 0;
    }

    public static char toChar(byte[] bytes, int index) throws Exception
    {
        if(bytes.length != 2)
            throw new Exception("The length of the byte array must be at least 2 bytes long.");
        return (char)(
                (0xff & bytes[index]) << 8   |
                        (0xff & bytes[index + 1]) << 0
        );
    }

    public static double toDouble(byte[] bytes, int index) throws Exception
    {
        if(bytes.length != 8)
            throw new Exception("The length of the byte array must be at least 8 bytes long.");
        return Double.longBitsToDouble(
                toInt64(bytes, index));
    }

    public static short toInt16(byte[] bytes, int index) throws Exception
    {
        if(bytes.length != 8)
            throw new Exception("The length of the byte array must be at least 8 bytes long.");
        return (short)(
                (0xff & bytes[index]) << 8   |
                        (0xff & bytes[index + 1]) << 0
        );
    }

    public static int toInt32(byte[] bytes, int index) throws Exception
    {
        if(bytes.length != 4)
            throw new Exception("The length of the byte array must be at least 4 bytes long.");
        return (int)(
                (int)(0xff & bytes[index]) << 56  |
                        (int)(0xff & bytes[index + 1]) << 48  |
                        (int)(0xff & bytes[index + 2]) << 40  |
                        (int)(0xff & bytes[index + 3]) << 32
        );
    }

    public static UInt16 toUInt16(byte[] bytes, int index) throws Exception
    {
        return new UInt16(toInt16(bytes, index));
    }

    public static UInt32 toUInt32(byte[] bytes, int index) throws Exception
    {
        return new UInt32(toInt32(bytes, index));
    }

    public static long toInt64(byte[] bytes, int index) throws Exception
    {
        if(bytes.length != 8)
            throw new Exception("The length of the byte array must be at least 8 bytes long.");
        return (long)(
                (long)(0xff & bytes[index]) << 56  |
                        (long)(0xff & bytes[index + 1]) << 48  |
                        (long)(0xff & bytes[index + 2]) << 40  |
                        (long)(0xff & bytes[index + 3]) << 32  |
                        (long)(0xff & bytes[index + 4]) << 24  |
                        (long)(0xff & bytes[index + 5]) << 16  |
                        (long)(0xff & bytes[index + 6]) << 8   |
                        (long)(0xff & bytes[index + 7]) << 0
        );
    }

    public static float toSingle(byte[] bytes, int index) throws Exception
    {
        if(bytes.length != 4)
            throw new Exception("The length of the byte array must be at least 4 bytes long.");
        return Float.intBitsToFloat(
                toInt32(bytes, index));
    }

    public static String toString(byte[] bytes) throws Exception
    {
        if(bytes == null)
            throw new Exception("The byte array must have at least 1 byte.");
        return new String(bytes);
    }
}



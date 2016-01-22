package com.github.pimsturm.commandmessenger;

import android.util.Log;

import java.io.UnsupportedEncodingException;

public class BinaryConverter {
    private static final String TAG = "BinaryConverter";

    /***** from binary value to string ****/

    /**
     * Convert a float into a string representation.
     * @param value The value to be converted.
     * @return A string representation of this object.
     */
    public static String toString(float value)
    {
        try
        {
            byte[] byteArray = BitConverter.getBytes(value);
            return bytesToEscapedString(byteArray);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Convert a Double into a string representation.
     * @param value The value to be converted.
     * @return A string representation of this object.
     */
    public static String toString(Double value)
    {
        try
        {
            byte[] byteArray = BitConverter.getBytes(value);
            return bytesToEscapedString(byteArray);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Convert an int into a string representation.
     * @param value The value to be converted.
     * @return A string representation of this object.
     */
    public static String toString(int value)
    {
        try
        {
            byte[] byteArray = BitConverter.getBytes(value);
            return bytesToEscapedString(byteArray);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Convert an unsigned int into a string representation.
     * @param value The value to be converted.
     * @return A string representation of this object.
     */
    public static String toString(UInt32 value)     // uint
    {
        try
        {
            byte[] byteArray = BitConverter.getBytes(value);
            return bytesToEscapedString(byteArray);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Convert a short into a string representation.
     * @param value The value to be converted.
     * @return A string representation of this object.
     */
    public static String toString(short value)
    {
        try
        {
            byte[] byteArray = BitConverter.getBytes(value);
            return bytesToEscapedString(byteArray);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Convert an unsigned an unsigned short into a string representation.
     * @param value The value to be converted.
     * @return A string representation of this object.
     */
    public static String toString(UInt16 value)     //ushort
    {
        try
        {
            byte[] byteArray = BitConverter.getBytes(value);
            return bytesToEscapedString(byteArray);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Convert a byte into a string representation.
     * @param value The value to be converted.
     * @return A string representation of this object.
     */
    public static String toString(byte value)
    {
        try
        {
            return bytesToEscapedString(new byte[]{value});
        }
        catch (Exception e)
        {
            return null;
        }
    }


    /***** from string to binary value ****/

    /**
     * Converts a string to a float.
     * @param value The value to be converted.
     * @return Input string as a float?
     */
    public static Float toFloat(String value)
    {
        try
        {
            byte[] bytes = escapedStringToBytes(value);
            if (bytes.length < 4)
            {
                return null;
            }
            return BitConverter.toSingle(bytes, 0);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Converts a string representation to a double.
     * @param value The value to be converted.
     * @return Input string as a Double?
     */
    public static Double toDouble(String value)
    {
        try
        {
            byte[] bytes = escapedStringToBytes(value);
            return BitConverter.toDouble(bytes, 0);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Converts a string representation to an int 32.
     * @param value The value to be converted.
     * @return This object as an Int32?
     */
    public static Integer toInt32(String value)
    {
        try
        {
            byte[] bytes = escapedStringToBytes(value);
            return BitConverter.toInt32(bytes, 0);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Converts a string representation to a u int 32.
     * @param value The value to be converted.
     * @return Input string as a nullable UInt32
     */
    public static UInt32 toUInt32(String value)
    {
        try
        {
            byte[] bytes = escapedStringToBytes(value);
            return BitConverter.toUInt32(bytes, 0);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Converts a string representation to a u int 16.
     * @param value The value to be converted.
     * @return Input string as a nullable UInt16
     */
    public static UInt16 toUInt16(String value)
    {
        try
        {
            byte[] bytes = escapedStringToBytes(value);
            return BitConverter.toUInt16(bytes, 0);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Converts a string representation to an int 16.
     * @param value The value to be converted.
     * @return This object as an nullable Int16
     */
    public static Short toInt16(String value)
    {
        try
        {
            byte[] bytes = escapedStringToBytes(value);
            return BitConverter.toInt16(bytes, 0);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Converts a string representation to a byte.
     * @param value The value to be converted.
     * @return Input string as a nullable byte
     */
    public static Byte toByte(String value)
    {
        try
        {
            byte[] bytes = escapedStringToBytes(value);
            return bytes[0];
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /***** conversion functions ****/

    /**
     * Converts a byte array to escaped string.
     * @param byteArray Array of bytes.
     * @return input value as an escaped string.
     */
    private static String bytesToEscapedString(byte[] byteArray)
    {
        try
        {
            String stringValue = byteArray.toString();
            return Escaping.escape(stringValue);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Converts an escaped string to a bytes array.
     * @param value The value to be converted.
     * @return input value as an escaped string.
     */
    public static byte[] escapedStringToBytes(String value)
    {
        try
        {
            String unEscapedValue = Escaping.unescape(value);
            return unEscapedValue.getBytes("ISO-8859-1");
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Converts a string to a bytes array.
     * @param value The value to be converted.
     * @return input value as a byte array.
     */
    public static byte[] stringToBytes(String value)
    {
        try {
            return value.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Converts a char array to a bytes array.
     * @param value The value to be converted.
     * @return input value as a byte array.
     */
    public static byte[] charsToBytes(char[] value)
    {
        try {
            return value.toString().getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return null;
    }
}

package com.icplaza.mapping.utils;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Locale;

public class Bech32 {
    private static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";
    private static final byte[] CHARSET_REV = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 15, -1, 10, 17, 21, 20, 26, 30, 7, 5, -1, -1, -1, -1, -1, -1, -1, 29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1, 1, 0, 3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1, -1, 29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1, 1, 0, 3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1};

    public Bech32() {
    }

    private static int polymod(byte[] values) {
        int c = 1;
        byte[] var2 = values;
        int var3 = values.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte v_i = var2[var4];
            int c0 = c >>> 25 & 255;
            c = (c & 33554431) << 5 ^ v_i & 255;
            if ((c0 & 1) != 0) {
                c ^= 996825010;
            }

            if ((c0 & 2) != 0) {
                c ^= 642813549;
            }

            if ((c0 & 4) != 0) {
                c ^= 513874426;
            }

            if ((c0 & 8) != 0) {
                c ^= 1027748829;
            }

            if ((c0 & 16) != 0) {
                c ^= 705979059;
            }
        }

        return c;
    }

    private static byte[] expandHrp(String hrp) {
        int hrpLength = hrp.length();
        byte[] ret = new byte[hrpLength * 2 + 1];

        for(int i = 0; i < hrpLength; ++i) {
            int c = hrp.charAt(i) & 127;
            ret[i] = (byte)(c >>> 5 & 7);
            ret[i + hrpLength + 1] = (byte)(c & 31);
        }

        ret[hrpLength] = 0;
        return ret;
    }

    private static boolean verifyChecksum(String hrp, byte[] values) {
        byte[] hrpExpanded = expandHrp(hrp);
        byte[] combined = new byte[hrpExpanded.length + values.length];
        System.arraycopy(hrpExpanded, 0, combined, 0, hrpExpanded.length);
        System.arraycopy(values, 0, combined, hrpExpanded.length, values.length);
        return polymod(combined) == 1;
    }

    private static byte[] createChecksum(String hrp, byte[] values) {
        byte[] hrpExpanded = expandHrp(hrp);
        byte[] enc = new byte[hrpExpanded.length + values.length + 6];
        System.arraycopy(hrpExpanded, 0, enc, 0, hrpExpanded.length);
        System.arraycopy(values, 0, enc, hrpExpanded.length, values.length);
        int mod = polymod(enc) ^ 1;
        byte[] ret = new byte[6];

        for(int i = 0; i < 6; ++i) {
            ret[i] = (byte)(mod >>> 5 * (5 - i) & 31);
        }

        return ret;
    }

    public static String encode(Bech32.Bech32Data bech32) {
        return encode(bech32.hrp, bech32.data);
    }

    public static String encode(String hrp, byte[] values) {
        Preconditions.checkArgument(hrp.length() >= 1, "Human-readable part is too short");
        Preconditions.checkArgument(hrp.length() <= 83, "Human-readable part is too long");
        hrp = hrp.toLowerCase(Locale.ROOT);
        byte[] checksum = createChecksum(hrp, values);
        byte[] combined = new byte[values.length + checksum.length];
        System.arraycopy(values, 0, combined, 0, values.length);
        System.arraycopy(checksum, 0, combined, values.length, checksum.length);
        StringBuilder sb = new StringBuilder(hrp.length() + 1 + combined.length);
        sb.append(hrp);
        sb.append('1');
        byte[] var5 = combined;
        int var6 = combined.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            byte b = var5[var7];
            sb.append("qpzry9x8gf2tvdw0s3jn54khce6mua7l".charAt(b));
        }

        return sb.toString();
    }

    public static Bech32.Bech32Data decode(String str) throws Exception {
        boolean lower = false;
        boolean upper = false;
        if (str.length() < 8) {
            throw new Exception("Input too short: " + str.length());
        } else if (str.length() > 90) {
            throw new Exception("Input too long: " + str.length());
        } else {
            int pos;
            for(pos = 0; pos < str.length(); ++pos) {
                char c = str.charAt(pos);
                if (c < '!' || c > '~') {
                    throw new Exception(String.format("%d %d", c, pos));
                }

                if (c >= 'a' && c <= 'z') {
                    if (upper) {
                        throw new Exception(String.format("%d %d", c, pos));
                    }

                    lower = true;
                }

                if (c >= 'A' && c <= 'Z') {
                    if (lower) {
                        throw new Exception(String.format("%d %d", c, pos));
                    }

                    upper = true;
                }
            }

            pos = str.lastIndexOf(49);
            if (pos < 1) {
                throw new Exception("Missing human-readable part");
            } else {
                int dataPartLength = str.length() - 1 - pos;
                if (dataPartLength < 6) {
                    throw new Exception("Data part too short: " + dataPartLength);
                } else {
                    byte[] values = new byte[dataPartLength];

                    for(int i = 0; i < dataPartLength; ++i) {
                        char c = str.charAt(i + pos + 1);
                        if (CHARSET_REV[c] == -1) {
                            throw new Exception(String.format("%d %d", c, i + pos + 1));
                        }

                        values[i] = CHARSET_REV[c];
                    }

                    String hrp = str.substring(0, pos).toLowerCase(Locale.ROOT);
                    if (!verifyChecksum(hrp, values)) {
                        throw new Exception();
                    } else {
                        return new Bech32.Bech32Data(hrp, Arrays.copyOfRange(values, 0, values.length - 6));
                    }
                }
            }
        }
    }

    public boolean isValidAddress(String str) {
        boolean lower = false;
        boolean upper = false;
        if (str.length() >= 8 && str.length() <= 90) {
            int pos;
            for(pos = 0; pos < str.length(); ++pos) {
                char c = str.charAt(pos);
                if (c < '!' || c > '~') {
                    return false;
                }

                if (c >= 'a' && c <= 'z') {
                    if (upper) {
                        return false;
                    }

                    lower = true;
                }

                if (c >= 'A' && c <= 'Z') {
                    if (lower) {
                        return false;
                    }

                    upper = true;
                }
            }

            pos = str.lastIndexOf(49);
            if (pos < 1) {
                return false;
            } else {
                int dataPartLength = str.length() - 1 - pos;
                if (dataPartLength < 6) {
                    return false;
                } else {
                    byte[] values = new byte[dataPartLength];

                    for(int i = 0; i < dataPartLength; ++i) {
                        char c = str.charAt(i + pos + 1);
                        if (CHARSET_REV[c] == -1) {
                            return false;
                        }

                        values[i] = CHARSET_REV[c];
                    }

                    String hrp = str.substring(0, pos).toLowerCase(Locale.ROOT);
                    if (!verifyChecksum(hrp, values)) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
    }

    public static class Bech32Data {
        final String hrp;
        final byte[] data;

        private Bech32Data(String hrp, byte[] data) {
            this.hrp = hrp;
            this.data = data;
        }

        public byte[] getData() {
            return this.data;
        }
    }
}
